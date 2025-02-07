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
import viewmodels.models.NameViewModel


class WhatIsTheMembersNameFormProviderSpec extends FieldBehaviours {

  private val formProvider = new WhatIsTheMembersNameFormProvider()

  import formProvider._

  val form: Form[NameViewModel] = formProvider()

  ".firstName" - {
//    behave.like(fieldThatBindsValidData(form, "firstName", stringsWithMaxLength(nameMaxLength)))
    behave.like(mandatoryField(form, "firstName", FormError("firstName", List("member.name.firstName.error.required"))))
//    val lengthUpperLimit = 35
//    val lengthFormError = FormError("firstName", "firstName.error.length", List(nameMaxLength))
//    behave.like(fieldLengthError(form, "firstName", lengthFormError, nameMaxLength + 1, lengthUpperLimit, alphaChar))

//    behave.like(
//      invalidAlphaField(
//        form,
//        fieldName = "firstName",
//        errorMessage = "firstName.error.invalid",
//        args = List(nameRegex)
//      )
//    )
  }

  ".lastName" - {
//    behave.like(fieldThatBindsValidData(form, "lastName", stringsWithMaxLength(nameMaxLength)))
    behave.like(mandatoryField(form, "lastName", FormError("lastName", List("member.name.lastName.error.required"))))
//    val lengthUpperLimit = 35
//    val lengthFormError = FormError("lastName", "lastName.error.length", List(nameMaxLength))
//    behave.like(fieldLengthError(form, "lastName", lengthFormError, nameMaxLength + 1, lengthUpperLimit, alphaChar))

  }

}
