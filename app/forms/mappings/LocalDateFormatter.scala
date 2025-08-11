/*
 * Copyright 2024 HM Revenue & Customs
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package forms.mappings

import play.api.data.FormError
import play.api.data.format.Formatter
import utils.DateFieldFormats.numericRegexp
import utils.DateTimeFormats.{longMonthFormat, shortMonthFormat}

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import scala.util.control.Exception.nonFatalCatch
import scala.util.{Failure, Success, Try}

private[mappings] class LocalDateFormatter(
  dayInvalidKey: String,
  monthInvalidKey: String,
  yearInvalidKey: String,
  monthTextInvalidKey: String,
  multipleInvalidKey: String,
  oneRequiredKey: String,
  twoRequiredKey: String,
  allRequiredKey: String,
  realDateKey: String
) extends Formatter[LocalDate]
    with Formatters {

  private val dayText   = "day"
  private val monthText = "month"
  private val yearText  = "year"

  private[mappings] def toDate(key: String, day: Int, month: Int, year: Int): Either[Seq[FormError], LocalDate] =
    Try(LocalDate.of(year, month, day)) match {
      case Success(date) =>
        Right(date)
      case Failure(_)    =>
        Left(Seq(FormError(key, realDateKey)))
    }

  private[mappings] def validateDayMonthYear(
    key: String,
    day: Option[String],
    month: Option[String],
    year: Option[String],
    hasMonthError: Boolean = false
  ): Seq[FormError] = {

    def validateDay: Boolean   = Try(day.get.toInt).toOption.exists(value => 1 to 31 contains value)
    def validateMonth: Boolean = getMonth(month.get).exists(value => 1 to 12 contains value)
    def validateYear: Boolean  = Try(year.get.toInt).toOption.exists(value => 1900 to LocalDate.now().getYear contains value)

    val monthKey = if(hasMonthError) monthTextInvalidKey else monthInvalidKey

    (day, month, year) match {
      case (Some(_), Some(_), Some(_)) =>
        (validateDay, validateMonth, validateYear) match {
          case (true, true, true)    => Nil
          case (false, true, true)   => Seq(FormError(s"$key.day", dayInvalidKey, Seq(dayText)))
          case (true, false, true)  => Seq(FormError(s"$key.month", monthKey, Seq(monthText)))
          case (true, true, false)   => Seq(FormError(s"$key.year", yearInvalidKey, Seq(yearText)))
          case (true, false, false)  =>
            Seq(
              FormError(s"$key.month", monthKey, Seq(monthText, yearText)),
              FormError(s"$key.year", yearInvalidKey, Seq(monthText, yearText))
            )
          case (false, true, false)  =>
            Seq(
              FormError(s"$key.day", dayInvalidKey, Seq(dayText, yearText)),
              FormError(s"$key.year", yearInvalidKey, Seq(dayText, yearText))
            )
          case (false, false, true)  =>
            Seq(
              FormError(s"$key.day", dayInvalidKey, Seq(dayText, monthText)),
              FormError(s"$key.month", monthKey, Seq(dayText, monthText))
            )
          case (false, false, false) =>
            Seq(
              FormError(s"$key.day", dayInvalidKey, Seq(dayText, monthText, yearText)),
              FormError(s"$key.month", monthKey, Seq(dayText, monthText, yearText)),
              FormError(s"$key.year", yearInvalidKey, Seq(dayText, monthText, yearText))
            )
        }
      case (None, Some(_), Some(_))    => Seq(FormError(s"$key.day", oneRequiredKey, Seq(dayText)))
      case (Some(_), None, Some(_))    => Seq(FormError(s"$key.month", oneRequiredKey, Seq(monthText)))
      case (Some(_), Some(_), None)    => Seq(FormError(s"$key.year", oneRequiredKey, Seq(yearText)))
      case (Some(_), None, None)       =>
        Seq(
          FormError(s"$key.month", twoRequiredKey, Seq(monthText, yearText)),
          FormError(s"$key.year", twoRequiredKey, Seq(monthText, yearText))
        )
      case (None, Some(_), None)       =>
        Seq(
          FormError(s"$key.day", twoRequiredKey, Seq(dayText, yearText)),
          FormError(s"$key.year", twoRequiredKey, Seq(dayText, yearText))
        )
      case (None, None, Some(_))       =>
        Seq(
          FormError(s"$key.day", twoRequiredKey, Seq(dayText, monthText)),
          FormError(s"$key.month", twoRequiredKey, Seq(dayText, monthText))
        )
      case _                           => Seq(FormError(key, allRequiredKey))
    }
  }

  override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], LocalDate] = {
    val dayKey: String   = s"$key.day"
    val monthKey: String = s"$key.month"
    val yearKey: String  = s"$key.year"

    val dayValue: Option[String]   = data.get(dayKey).map(textWhitespaceRemove).filter(_.nonEmpty)
    val monthValue: Option[String] = data.get(monthKey).map(textWhitespaceRemove).filter(_.nonEmpty)
    val yearValue: Option[String]  = data.get(yearKey).map(textWhitespaceRemove).filter(_.nonEmpty)

    lazy val hasMonthError :Boolean = monthValue match {
      case None => false
      case Some(value) if value.matches(numericRegexp) => false
      case Some(value) => (checkWithPattern(value, shortMonthFormat) orElse checkWithPattern(value, longMonthFormat)).isEmpty
    }

    val errors = validateDayMonthYear(key, dayValue, monthValue, yearValue, hasMonthError)

    errors match {
      case Nil => toDate(key, dayValue.get.toInt,
        getMonth(monthValue.get).get,
          yearValue.get.toInt)
      case _   => Left(errors)
    }
  }

  private def textWhitespaceRemove(s: String): String = s.replaceAll(" ", "").trim

  override def unbind(key: String, value: LocalDate): Map[String, String] =
    Map(
      s"$key.day"   -> value.getDayOfMonth.toString,
      s"$key.month" -> value.getMonthValue.toString,
      s"$key.year"  -> value.getYear.toString
    )

  lazy val getMonth: String => Option[Int] = month => if(month.matches(numericRegexp)){
    month.toIntOption
  } else {
    checkWithPattern(month, shortMonthFormat) orElse checkWithPattern(month, longMonthFormat)
  }

  def checkWithPattern(month: String, pattern: DateTimeFormatter): Option[Int] =
    nonFatalCatch.opt(
      pattern
        .parse(month.toLowerCase.capitalize)
        .get(ChronoField.MONTH_OF_YEAR)
    )

}
