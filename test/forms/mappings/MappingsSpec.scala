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

import models.MembersDob
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.data.{Form, FormError}
import providers.DateTimeProvider

import java.time.{ZoneId, ZonedDateTime}

class MappingsSpec extends AnyFreeSpec with Matchers with OptionValues with Mappings {
  "text" - {
    val testForm: Form[String] =
      Form(
        "value" -> text()
      )

    "must bind a valid string" in {
      val result = testForm.bind(Map("value" -> "foobar"))
      result.get mustEqual "foobar"
    }

    "must not bind an empty string" in {
      val result = testForm.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind a string of whitespace only" in {
      val result = testForm.bind(Map("value" -> " \t"))
      result.errors must contain(FormError("value", "error.required"))
    }

    "must not bind an empty map" in {
      val result = testForm.bind(Map.empty[String, String])
      result.errors must contain(FormError("value", "error.required"))
    }

    "must return a custom error message" in {
      val form = Form("value" -> text("custom.error"))
      val result = form.bind(Map("value" -> ""))
      result.errors must contain(FormError("value", "custom.error"))
    }

    "must unbind a valid value" in {
      val result = testForm.fill("foobar")
      result.apply("value").value.value mustEqual "foobar"
    }
  }

  "dateOfBirth" - {
    trait DateOfBirthTest {
      val mockDateTimeProvider: DateTimeProvider = mock[DateTimeProvider]

      val mockYear: Int = 2025
      val mockDateTimeVal: Int = 12

      when(mockDateTimeProvider.now(any())).thenReturn(
        ZonedDateTime.of(
          mockYear,
          mockDateTimeVal,
          mockDateTimeVal,
          mockDateTimeVal,
          mockDateTimeVal,
          mockDateTimeVal,
          mockDateTimeVal,
          ZoneId.of("Europe/London")
        )
      )

      val form: Form[MembersDob] = Form(
        "dateOfBirth" -> dateOfBirth(mockDateTimeProvider)
      )

      val dayString: Option[String] = None
      val monthString: Option[String] = None
      val yearString: Option[String] = None

      def valToMapOpt(valName: String, valOpt: Option[String]): Map[String, String] = valOpt.fold(Map.empty[String, String])(
        value => Map(valName -> value)
      )

      lazy val boundForm: Form[MembersDob] = form.bind(
        valToMapOpt("dateOfBirth.day", dayString) ++
          valToMapOpt("dateOfBirth.month", monthString) ++
          valToMapOpt("dateOfBirth.year", yearString)
      )
    }

    "must bind a valid submission" in new DateOfBirthTest {
      val day: Int = 30
      val month: Int = 1
      val year: Int = 1902

      override val dayString: Option[String] = Some(day.toString)
      override val monthString: Option[String] = Some(month.toString)
      override val yearString: Option[String] = Some(year.toString)

      boundForm.errors must have length 0
      boundForm.value mustBe Some(MembersDob(day, month, year))
    }

    "must return errors when fields are missing" in new DateOfBirthTest {
      boundForm.errors must have length 3
      boundForm.errors.flatMap(_.messages) mustBe Seq(
        "membersDob.error.missing.day",
        "membersDob.error.missing.month",
        "membersDob.error.missing.year"
      )
    }

    "must return errors when fields are present and invalid" in new DateOfBirthTest {
      override val dayString: Option[String] = Some("32")
      override val monthString: Option[String] = Some("13")
      override val yearString: Option[String] = Some("1800")

      boundForm.errors must have length 3
      boundForm.errors.flatMap(_.messages) mustBe Seq(
        "membersDob.error.invalid.day",
        "membersDob.error.invalid.month",
        "membersDob.error.invalid.year"
      )
    }

    "must enforce current year maximum" in new DateOfBirthTest {
      override val dayString: Option[String] = Some("30")
      override val monthString: Option[String] = Some("1")
      override val yearString: Option[String] = Some("2026")

      boundForm.errors must have length 1
      boundForm.errors.flatMap(_.messages) mustBe Seq(
        "membersDob.error.invalid.year"
      )
    }
  }
}
