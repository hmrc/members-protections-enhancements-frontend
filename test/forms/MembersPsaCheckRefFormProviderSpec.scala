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
        errorMessage = "membersPsaCheckRef.error.invalid"
      )
    )
  }
}
