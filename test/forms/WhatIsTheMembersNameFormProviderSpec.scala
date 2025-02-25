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
import models.MemberDetails
import play.api.data.{Form, FormError}

class WhatIsTheMembersNameFormProviderSpec extends FieldBehaviours {

  private val formProvider = new WhatIsTheMembersNameFormProvider()

  import formProvider._

  val form: Form[MemberDetails] = formProvider()

  ".firstName" must {
    behave.like(mandatoryField(form, "firstName", FormError("firstName", List("membersName.firstName.error.required"))))

    behave.like(
      invalidAlphaField(
        form,
        fieldName = "firstName",
        errorMessage = "membersName.firstName.error.invalid",
        args = List(nameRegex)
      )
    )
  }

  ".lastName" must {
    behave.like(mandatoryField(form, "lastName", FormError("lastName", List("membersName.lastName.error.required"))))
    behave.like(
      invalidAlphaField(
        form,
        fieldName = "lastName",
        errorMessage = "membersName.lastName.error.invalid",
        args = List(nameRegex)
      )
    )
  }

}
