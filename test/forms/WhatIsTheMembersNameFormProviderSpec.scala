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
import org.scalacheck.Gen.alphaChar
import play.api.data.{Form, FormError}
import viewmodels.models.MemberDetails

class WhatIsTheMembersNameFormProviderSpec extends FieldBehaviours {

  private val formProvider = new WhatIsTheMembersNameFormProvider()

  import formProvider._

  val form: Form[MemberDetails] = formProvider()

  ".firstName" - {
    behave.like(mandatoryField(form, "firstName", FormError("firstName", List("member.name.firstName.error.required"))))

    behave.like(
      invalidAlphaField(
        form,
        fieldName = "firstName",
        errorMessage = "member.name.firstName.error.invalid",
        args = List(nameRegex)
      )
    )
  }

  ".lastName" - {
    behave.like(mandatoryField(form, "lastName", FormError("lastName", List("member.name.lastName.error.required"))))
    behave.like(
      invalidAlphaField(
        form,
        fieldName = "lastName",
        errorMessage = "member.name.lastName.error.invalid",
        args = List(nameRegex)
      )
    )
  }

}
