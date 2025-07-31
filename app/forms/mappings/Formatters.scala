/*
 * Copyright 2025 HM Revenue & Customs
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

import forms.mappings.Formatters.{validateInt, validateMonth}
import play.api.data.FormError
import play.api.data.format.Formatter
import utils.DateFieldFormats._
import utils.DateTimeFormats.{longMonthFormat, shortMonthFormat}

import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoField
import scala.util.control.Exception.nonFatalCatch

trait Formatters {
  private[mappings] def stringFormatter(errorKey: String,
                                        args: Seq[String] = Seq.empty): Formatter[String] = new Formatter[String] {
    override def bind(key: String, data: Map[String, String]): ValidationResult[String] =
      data.get(key) match {
        case None                      => Left(Seq(FormError(key, errorKey, args)))
        case Some(s) if s.trim.isEmpty => Left(Seq(FormError(key, errorKey, args)))
        case Some(s)                   => Right(s)
      }

    override def unbind(key: String, value: String): Map[String, String] =
      Map(key -> value)
  }

  private[mappings] def intFormatter(requiredKey: String,
                                     wholeNumberKey: String,
                                     nonNumericKey: String,
                                     args: Seq[String] = Seq.empty): Formatter[Int] = new Formatter[Int] {
    private val baseFormatter: Formatter[String] = stringFormatter(requiredKey, args)

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Int] =
      baseFormatter
        .bind(key, data)
        .map(_.filterNot(_.isWhitespace).replace(",", ""))
        .flatMap(str => validateInt(str, key, wholeNumberKey, nonNumericKey))

    override def unbind(key: String, value: Int): Map[String, String] =
      baseFormatter.unbind(key, value.toString)
  }

  private[mappings] def monthFormatter(requiredKey: String,
                                       wholeNumberKey: String,
                                       nonNumericKey: String,
                                       invalidMonthKey: String,
                                       args: Seq[String] = Seq.empty): Formatter[Int] = new Formatter[Int] {
    private val baseFormatter: Formatter[String] = stringFormatter(requiredKey, args)

    override def bind(key: String, data: Map[String, String]): Either[Seq[FormError], Int] =
      baseFormatter
        .bind(key, data)
        .map(_.filterNot(_.isWhitespace).replace(",", ""))
        .flatMap(str =>
          if (str.matches(numericRegexp)){
            validateInt(str, key, wholeNumberKey, nonNumericKey)
          }
          else {
            validateMonth(str, key, invalidMonthKey)
          }
        )

    override def unbind(key: String, value: Int): Map[String, String] =
      baseFormatter.unbind(key, value.toString)
  }

}

object Formatters {
  def validateInt(str: String,
                  key: String,
                  wholeNumberKey: String,
                  nonNumericKey: String): ValidationResult[Int] = str.filterNot(_.isWhitespace) match {
    case decimalString if decimalString.matches(decimalRegexp) =>
      Left(Seq(FormError(key, wholeNumberKey)))
    case integerString if integerString.matches(integerRegexp) =>
      Right(integerString.toInt)
    case _ =>
      Left(Seq(FormError(key, nonNumericKey)))
  }

  def validateMonth(str: String,
                    key: String,
                    invalidMonthKey: String): ValidationResult[Int] = {
    def checkWithPattern(pattern: DateTimeFormatter): Option[Int] =
      nonFatalCatch.opt(
        pattern
          .parse(str)
          .get(ChronoField.MONTH_OF_YEAR)
      )

    (checkWithPattern(shortMonthFormat) orElse checkWithPattern(longMonthFormat))
      .map(Right(_))
      .getOrElse(Left(Seq[FormError](FormError(key, invalidMonthKey))))
  }
}
