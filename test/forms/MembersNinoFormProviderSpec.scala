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

    behave.like(
      invalidAlphaField(
        form,
        fieldName = "nino",
        errorMessage = "membersNino.error.invalid"
      )
    )
  }
}
