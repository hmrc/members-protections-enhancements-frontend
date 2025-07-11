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

import forms.behaviours.FieldBehaviours
import models.MembersNino
import play.api.data.{Form, FormError}

class MembersNinoFormProviderSpec extends FieldBehaviours {
  private val formProvider = new MembersNinoFormProvider()
  val form: Form[MembersNino] = formProvider()

  ".nino" must {
    behave.like(mandatoryField(form, "nino", FormError("nino", List("membersNino.error.required"))))

    "return an error when invalid characters are present" in {
      val result = form.bind(Map("nino" -> "A!£$%^&*1")).errors.head
      result.key mustBe "nino"
      result.message mustBe "membersNino.error.invalid.characters"
    }

    "return an error when format is invalid" in {
      val result = form.bind(Map("nino" -> "AAAAAAAAAA")).errors.head
      result.key mustBe "nino"
      result.message mustBe "membersNino.error.invalid.format"
    }

    "return a success for a TRN" in {
      val result = form.bind(Map("nino" -> "12A12345")).value
      result mustBe Some(MembersNino("12A12345"))
    }

    "return a success for a TRN removing spaces" in {
      val result = form.bind(Map("nino" -> "12A12345")).value
      result mustBe Some(MembersNino("  12A  12 345   "))
    }

    "return a success for a NINO" in {
      val result = form.bind(Map("nino" -> "AA123456A")).value
      result mustBe Some(MembersNino("AA123456A"))
    }

    "return a success for a NINO removing spaces" in {
      val result = form.bind(Map("nino" -> "  AA 1234 56A  ")).value
      result mustBe Some(MembersNino("AA123456A"))
    }

    "return a success for a lower case NINO" in {
      val result = form.bind(Map("nino" -> "aa123456a")).value
      result mustBe Some(MembersNino("AA123456A"))
    }
  }
}
