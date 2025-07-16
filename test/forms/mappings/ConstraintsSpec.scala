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

import generators.Generators
import models.MembersDob
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalacheck.Gen
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.data.validation.{Invalid, Valid, ValidationResult}
import providers.DateTimeProvider

import java.time.{LocalDate, ZoneId, ZonedDateTime}

class ConstraintsSpec extends AnyFreeSpec with Matchers with ScalaCheckPropertyChecks with Generators with Constraints {

  private def toMembersDob(input: LocalDate): MembersDob = {
    MembersDob(input.getDayOfMonth, input.getMonthValue, input.getYear)
  }

  "firstError" - {
    "must return Valid when all constraints pass" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("foo")
      result mustEqual Valid
    }

    "must return Invalid when the first constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }

    "must return Invalid when the second constraint fails" in {
      val result = firstError(maxLength(10, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result mustEqual Invalid("error.regexp", """^\w+$""")
    }

    "must return Invalid for the first error when both constraints fail" in {
      val result = firstError(maxLength(-1, "error.length"), regexp("""^\w+$""", "error.regexp"))("")
      result mustEqual Invalid("error.length", -1)
    }
  }

  "regexp" - {
    "must return Valid for an input that matches the expression" in {
      val result = regexp("""^\w+$""", "error.invalid")("foo")
      result mustEqual Valid
    }

    "must return Invalid for an input that does not match the expression" in {
      val result = regexp("""^\d+$""", "error.invalid")("foo")
      result mustEqual Invalid("error.invalid", """^\d+$""")
    }
  }

  "minLength" - {
    "must return Valid for a string longer than the minimum length" in {
      val result = minLength(8, "error.length")("a" * 9)
      result mustEqual Valid
    }

    "must return Invalid for an empty string" in {
      val result = minLength(10, "error.length")("")
      result mustEqual Invalid("error.length", 10)
    }

    "must return Valid for a string equal to the minimum length" in {
      val result = minLength(10, "error.length")("a" * 10)
      result mustEqual Valid
    }

    "must return Invalid for a string shorter than the minimum length" in {
      val result = minLength(11, "error.length")("a" * 10)
      result mustEqual Invalid("error.length", 11)
    }
  }

  "maxLength" - {
    "must return Valid for a string shorter than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 9)
      result mustEqual Valid
    }

    "must return Valid for an empty string" in {
      val result = maxLength(10, "error.length")("")
      result mustEqual Valid
    }

    "must return Valid for a string equal to the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 10)
      result mustEqual Valid
    }

    "must return Invalid for a string longer than the allowed length" in {
      val result = maxLength(10, "error.length")("a" * 11)
      result mustEqual Invalid("error.length", 10)
    }
  }

  "validDate" - {
    "should return Valid for a valid date" in {
      val (day, month, year) = (1, 1, 2025)
      val result: ValidationResult = validDate("some.error")(MembersDob(day, month, year))
      result mustBe Valid
    }

    "should return Invalid for an invalid date" in {
      val (day, month, year) = (30, 2, 2025)
      val result: ValidationResult = validDate("some.error")(MembersDob(day, month, year))
      result mustBe a[Invalid]
    }
  }

  "futureDate" - {
    val mockDateTimeProvider: DateTimeProvider = mock[DateTimeProvider]

    val mockYear: Int = 2025
    val mockDateTimeVal: Int = 12
    val mockCurrentDate: LocalDate = LocalDate.of(mockYear, mockDateTimeVal, mockDateTimeVal)

    when(mockDateTimeProvider.now(any())).thenReturn(
      ZonedDateTime.of(
        mockCurrentDate.atStartOfDay(),
        ZoneId.of("Europe/London")
      )
    )

    "must return Valid for a date before or equal to the maximum" in {
      val gen: Gen[LocalDate] = for {
        date <- datesBetween(LocalDate.of(2000, 1, 1), mockCurrentDate)
      } yield date

      forAll(gen) {
        date =>

          val result = futureDate("error.future", mockDateTimeProvider)(toMembersDob(date))
          result mustEqual Valid
      }
    }

    "must return Invalid for a date after the maximum" in {
      val gen: Gen[LocalDate] = for {
        date <- datesBetween(mockCurrentDate.plusDays(1), LocalDate.of(3000, 1, 2))
      } yield date

      forAll(gen) {
        date =>
          val result = futureDate("error.future", mockDateTimeProvider, "foo")(toMembersDob(date))
          result mustEqual Invalid("error.future", "foo")
      }
    }
  }

}
