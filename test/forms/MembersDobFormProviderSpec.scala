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

package forms

import forms.behaviours.DateBehaviours
import models.MembersDob
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import org.scalatest.matchers.should.Matchers.{shouldBe, shouldEqual}
import org.scalatestplus.mockito.MockitoSugar.mock
import play.api.data.Form
import play.api.i18n.Messages
import play.api.test.{FakeRequest, Helpers}
import providers.DateTimeProvider

import java.time.temporal.ChronoField
import java.time.{LocalDate, ZoneId, ZonedDateTime}

class MembersDobFormProviderSpec extends DateBehaviours {
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

  private val formProvider = new MembersDobFormProvider(mockDateTimeProvider)
  private val form: Form[MembersDob] = formProvider()

  val messages: Messages = Helpers.stubMessagesApi().preferred(FakeRequest())

  private val formField = "dateOfBirth"

  private val minDate = LocalDate.of(1900, 1, 1)
  private val maxDate = LocalDate.now()

  ".dateOfBirth" must {
    "bind valid data with numeric month values" in {
      println()
      forAll(datesBetween(minDate, maxDate)) { date =>
        val data = Map(
          s"$formField.day" -> date.getDayOfMonth.toString,
          s"$formField.month" -> date.getMonthValue.toString,
          s"$formField.year" -> date.getYear.toString
        )
        val result = form.bind(data)
        result.value.value shouldEqual MembersDob(
          LocalDate.of(date.getYear,
          date.getMonthValue, date.getDayOfMonth))
      }
    }

    "bind valid data with non-numeric month values" in {
      forAll(datesBetween(minDate, maxDate)) { date =>
        val data = Map(
          s"$formField.day" -> date.getDayOfMonth.toString,
          s"$formField.month" -> ChronoField.MONTH_OF_YEAR.getFrom(date).toString,
          s"$formField.year" -> date.getYear.toString
        )
        val result = form.bind(data)
        result.value.value shouldEqual MembersDob(
          LocalDate.of(date.getYear,
            date.getMonthValue, date.getDayOfMonth))
      }
    }

    "bind data having spaces" in {
      val day = 11
      val month = 11
      val year = 2010
        val data = Map(
          s"$formField.day" -> "1 1",
          s"$formField.month" -> "1 1",
          s"$formField.year" -> "2 0 1 0"
        )
        val result = form.bind(data)
        result.value.value shouldEqual MembersDob(LocalDate.of(year, month, day))
    }

    "fail to bind" when {
      "invalid values are submitted" in {
        val day = 33
        val month = 23
        val year = 1

        val data = Map(
          s"$formField.day" -> day.toString,
          s"$formField.month" -> month.toString,
          s"$formField.year" -> year.toString
        )
        val result = form.bind(data)
        result.errors must have length 3
        result.errors.flatMap(_.messages) mustBe Seq(
          "membersDob.error.invalid.day",
          "membersDob.error.invalid.month",
          "membersDob.error.invalid.year"
        )
      }

      "decimal values are submitted" in {
        val day = 3.33
        val month = 2.22
        val year = 1.11

        val data = Map(
          s"$formField.day" -> day.toString,
          s"$formField.month" -> month.toString,
          s"$formField.year" -> year.toString
        )
        val result = form.bind(data)
        result.errors must have length 3
        result.errors.flatMap(_.messages) mustBe Seq(
          "membersDob.error.invalid.day",
          "membersDob.error.invalid.month",
          "membersDob.error.invalid.year"
        )
      }

      "non-numeric values are submitted" in {
        val day = "a"
        val month = "b"
        val year = "c"

        val data = Map(
          s"$formField.day" -> day,
          s"$formField.month" -> month,
          s"$formField.year" -> year
        )
        val result = form.bind(data)
        result.errors must have length 3
        result.errors.flatMap(_.messages) mustBe Seq(
          "membersDob.error.invalid.day",
          "membersDob.error.invalid.month.nonNumeric",
          "membersDob.error.invalid.year"
        )
      }

      "fields are missing or empty" in {
        val data = Map(
          s"$formField.day" -> " ",
          s"$formField.month" -> "",
        )
        val result = form.bind(data)
        result.errors must have length 1
        result.errors.flatMap(_.messages) mustBe Seq(
          "membersDob.error.missing.all"
        )
      }

      "supplied data represents a non-valid date" in {
        val day = 30
        val month = 2
        val year = 2010

        val data = Map(
          s"$formField.day" -> day.toString,
          s"$formField.month" -> month.toString,
          s"$formField.year" -> year.toString
        )
        val result = form.bind(data)
        result.errors must have length 1
        result.errors.flatMap(_.messages) must contain("membersDob.error.missing.real")
      }

      "supplied data represents a future date" in {
        val futureDate: LocalDate = mockCurrentDate.plusDays(1)
        val (day, month, year) = (futureDate.getDayOfMonth, futureDate.getMonthValue, futureDate.getYear)

        val data = Map(
          s"$formField.day" -> day.toString,
          s"$formField.month" -> month.toString,
          s"$formField.year" -> year.toString
        )
        val result = form.bind(data)
        result.errors must have length 1
        result.errors.flatMap(_.messages) must contain(messages("membersDob.error.missing.future"))
      }

      "supplied data represents a less than min date" in {
        val futureDate: LocalDate = LocalDate.of(1900, 1, 1).minusDays(1)
        val (day, month, year) = (futureDate.getDayOfMonth, futureDate.getMonthValue, futureDate.getYear)

        val data = Map(
          s"$formField.day" -> day.toString,
          s"$formField.month" -> month.toString,
          s"$formField.year" -> year.toString
        )
        val result = form.bind(data)
        result.errors must have length 1
        result.errors.flatMap(_.messages) must contain(messages("membersDob.error.invalid.year"))
      }
    }
  }
}
