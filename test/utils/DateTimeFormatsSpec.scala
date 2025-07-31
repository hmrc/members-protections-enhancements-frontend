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

package utils

import com.github.tomakehurst.wiremock.common.DateTimeParser
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Lang
import utils.DateTimeFormats.{dateTimeFormat, getCurrentDateTimestamp, longMonthFormat, shortMonthFormat}

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
      nonFatalCatch.opt(parser.parse(monthStr)).map(
        _.get(ChronoField.MONTH_OF_YEAR)
      ) mustBe Some(monthInt)
    }

  ".shortMonthFormat" - {
    val validValues: Seq[(String, Int)] = Seq(
      "Jan" -> 1,
      "Feb" -> 2,
      "Mar" -> 3,
      "Apr" -> 4,
      "May" -> 5,
      "Jun" -> 6,
      "Jul" -> 7,
      "Aug" -> 8,
      "Sept" -> 9,
      "Oct" -> 10,
      "Nov" -> 11,
      "Dec" -> 12
    )

    validValues.foreach(scenario => testForMonthString(scenario._1, scenario._2, shortMonthFormat))

    "should not parse any incorrect string" in {
      assertThrows[DateTimeParseException](shortMonthFormat.parse("incorrect"))
    }
  }

  ".longMonthFormat" - {
    val validValues: Seq[(String, Int)] = Seq(
      "January" -> 1,
      "February" -> 2,
      "March" -> 3,
      "April" -> 4,
      "May" -> 5,
      "June" -> 6,
      "July" -> 7,
      "August" -> 8,
      "September" -> 9,
      "October" -> 10,
      "November" -> 11,
      "December" -> 12
    )

    validValues.foreach(scenario => testForMonthString(scenario._1, scenario._2, longMonthFormat))

    "should not parse any incorrect string" in {
      assertThrows[DateTimeParseException](longMonthFormat.parse("incorrect"))
    }
  }
}
