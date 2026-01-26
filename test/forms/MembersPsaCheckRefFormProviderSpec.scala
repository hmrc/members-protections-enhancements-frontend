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

import forms.behaviours.FieldBehaviours
import models.MembersPsaCheckRef
import play.api.data.{Form, FormError}

class MembersPsaCheckRefFormProviderSpec extends FieldBehaviours {

  private val formProvider = new MembersPsaCheckRefFormProvider()

  val form: Form[MembersPsaCheckRef] = formProvider()

  ".psaCheckRef" must {
    behave.like(mandatoryField(form, "psaCheckRef", FormError("psaCheckRef", List("membersPsaCheckRef.error.required"))))

    behave.like(
      invalidAlphaField(
        form,
        fieldName = "psaCheckRef",
        errorMessage = "membersPsaCheckRef.error.format",
        args = List(formProvider.psaCheckRefRegex)
      )
    )

    "accept a valid psaCheckRef with lowercase letters" in {
      val result: Form[MembersPsaCheckRef] = form.bind(
        Map("psaCheckRef" -> "psa 12 34 56 78 a")
      )

      result.value mustBe Some(MembersPsaCheckRef("PSA12345678A"))
    }

    "accept a valid psaCheckRef with spaces" in {
      val result: Form[MembersPsaCheckRef] = form.bind(
        Map("psaCheckRef" -> "   PSA 12 3 4 5 6 78 a    ")
      )

      result.value mustBe Some(MembersPsaCheckRef("PSA12345678A"))
    }

    "return the expected error when a non-supported character is present" in {
      val result: Form[MembersPsaCheckRef] = form.bind(
        Map("psaCheckRef" -> "!@£$%")
      )

      result.errors must have length 1
      result.errors.head.key mustBe "psaCheckRef"
      result.errors.head.message mustBe "membersPsaCheckRef.error.invalidCharacters"
    }
  }
}
