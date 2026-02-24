/*
 * Copyright 2026 HM Revenue & Customs
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

package utils

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Lang
import utils.DateTimeFormats.{dateTimeFormat, getCurrentDateTimestamp, longMonthFormat, shortMonthFormat}

import java.time.Month.*
import java.time.format.{DateTimeFormatter, DateTimeParseException}
import java.time.temporal.ChronoField
import java.time.{LocalDate, ZoneId, ZonedDateTime}
import scala.util.control.Exception.nonFatalCatch

class DateTimeFormatsSpec extends AnyFreeSpec with Matchers {

  ".dateTimeFormat" - {
    "must format dates in English" in {
      val formatter = dateTimeFormat()(Lang("en"))
      val result = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 January 2023"
    }

    "must format dates in Welsh" in {
      val formatter = dateTimeFormat()(Lang("cy"))
      val result = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 Ionawr 2023"
    }

    "must default to English format" in {
      val formatter = dateTimeFormat()(Lang("de"))
      val result = LocalDate.of(2023, 1, 1).format(formatter)
      result mustEqual "1 January 2023"
    }
  }

  ".getCurrentDateTimestamp" - {
    "must format dates in English" in {
      val result: String = getCurrentDateTimestamp(
        ZonedDateTime.of(2025, 11, 11, 11, 11, 11, 11, ZoneId.of("Europe/London"))
      )
      result mustEqual "11 November 2025 at 11:11am"
    }
  }

  def testForMonthString(monthStr: String, monthInt: Int, parser: DateTimeFormatter): Unit =
    s"should convert the string: $monthStr to the int: $monthInt" in {
      nonFatalCatch
        .opt(parser.parse(monthStr))
        .map(
          _.get(ChronoField.MONTH_OF_YEAR)
        ) mustBe Some(monthInt)
    }

  ".shortMonthFormat" - {
    val sept = LocalDate.of(2021, 9, 1).format(shortMonthFormat)

    val validValues: Seq[(String, Int)] = Seq(
      "Jan" -> JANUARY.getValue,
      "Feb" -> FEBRUARY.getValue,
      "Mar" -> MARCH.getValue,
      "Apr" -> APRIL.getValue,
      "May" -> MAY.getValue,
      "Jun" -> JUNE.getValue,
      "Jul" -> JULY.getValue,
      "Aug" -> AUGUST.getValue,
      sept -> SEPTEMBER.getValue,
      "Oct" -> OCTOBER.getValue,
      "Nov" -> NOVEMBER.getValue,
      "Dec" -> DECEMBER.getValue
    )

    validValues.foreach(scenario => testForMonthString(scenario._1, scenario._2, shortMonthFormat))

    "should not parse any incorrect string" in {
      assertThrows[DateTimeParseException](shortMonthFormat.parse("incorrect"))
    }
  }

  ".longMonthFormat" - {
    val validValues: Seq[(String, Int)] = Seq(
      "January" -> JANUARY.getValue,
      "February" -> FEBRUARY.getValue,
      "March" -> MARCH.getValue,
      "April" -> APRIL.getValue,
      "May" -> MAY.getValue,
      "June" -> JUNE.getValue,
      "July" -> JULY.getValue,
      "August" -> AUGUST.getValue,
      "September" -> SEPTEMBER.getValue,
      "October" -> OCTOBER.getValue,
      "November" -> NOVEMBER.getValue,
      "December" -> DECEMBER.getValue
    )

    validValues.foreach(scenario => testForMonthString(scenario._1, scenario._2, longMonthFormat))

    "should not parse any incorrect string" in {
      assertThrows[DateTimeParseException](longMonthFormat.parse("incorrect"))
    }
  }
}
