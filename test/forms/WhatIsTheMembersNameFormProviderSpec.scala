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
    behave.like(
      mandatoryField(
        form = form,
        fieldName = "firstName",
        requiredError = FormError(key = "firstName", messages = List("membersName.error.required.firstName"))
      )
    )

    behave.like(
      invalidAlphaField(
        form,
        fieldName = "firstName",
        errorMessage = "membersName.error.invalid.firstName",
        args = List(nameRegex)
      )
    )
  }

  ".lastName" must {
    behave.like(
      mandatoryField(
        form = form,
        fieldName = "lastName",
        requiredError = FormError("lastName", List("membersName.error.required.lastName"))
      )
    )

    behave.like(
      invalidAlphaField(
        form,
        fieldName = "lastName",
        errorMessage = "membersName.error.invalid.lastName",
        args = List(nameRegex)
      )
    )

    "return an error for a last name which is shorter then 2 characters" in {
      val result: Form[MemberDetails] = form.bind(Map(
        "firstName" -> "Valid",
        "lastName" -> "A"
      ))

      result.errors must have length 1
      result.errors.map(_.message) must contain("membersName.error.tooShort.lastName")
    }
  }

}
