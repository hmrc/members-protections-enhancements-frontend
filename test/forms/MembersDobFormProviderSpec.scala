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

package forms

import forms.behaviours.DateBehaviours
import models.MembersDob
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.data.{Form, FormError}
import play.api.i18n.Messages
import play.api.test.{FakeRequest, Helpers}

import java.time.LocalDate
import scala.collection.mutable

class MembersDobFormProviderSpec extends DateBehaviours {

  private val formProvider = new MembersDobFormProvider()
  private val form: Form[MembersDob] = formProvider()

  val messages: Messages = Helpers.stubMessagesApi().preferred(FakeRequest())

  private val formField = "dateOfBirth"

  private val minDate = LocalDate.of(1900, 1, 1)
  private val maxDate = LocalDate.now()

  ".dateOfBirth" must {
    "bind valid data" in {
      forAll(datesBetween(minDate, maxDate)) { date =>
        val data = Map(
          s"$formField.day" -> date.getDayOfMonth.toString,
          s"$formField.month" -> date.getMonthValue.toString,
          s"$formField.year" -> date.getYear.toString
        )
        val result = form.bind(data)
        result.value.value shouldEqual MembersDob(
          date.getDayOfMonth.toString,
          date.getMonthValue.toString,
          date.getYear.toString)
      }
    }

    "fail to bind" when {
      val fields = List("day", "month", "year")

      fields foreach { field =>
        s"$field is blank" in {
          forAll(datesBetween(minDate, maxDate)) { date =>
            val data = mutable.Map(
              s"$formField.day" -> date.getDayOfMonth.toString,
              s"$formField.month" -> date.getMonthValue.toString,
              s"$formField.year" -> date.getYear.toString
            )
            data(s"$formField.$field") = ""
            val result = form.bind(data.toMap)

            result.errors.headOption shouldEqual Some(
              FormError(s"$formField.$field", messages(s"membersDob.error.required.$field"))
            )
          }
        }
      }
    }
  }
}
