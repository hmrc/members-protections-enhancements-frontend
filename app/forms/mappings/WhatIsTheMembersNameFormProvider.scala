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

import com.google.inject.Inject
import forms.mappings.Mappings
import play.api.data.Form
import play.api.data.Forms.mapping
import viewmodels.models.NameViewModel

class WhatIsTheMembersNameFormProvider @Inject extends Mappings {

  private val nameMaxLength = 35
  private val nameRegex = "^[a-zA-Z\\-' ]+$"

  val firstName = "firstName"
  val lastName = "lastName"

  def apply(): Form[NameViewModel] =
    Form(
      mapping(
        firstName -> text("member.name.firstName.error.required").verifying(
        firstError(
          regexp(nameRegex, "memberDetails.firstName.error.invalid"),
          maxLength(nameMaxLength, "memberDetails.firstName.error.length")
        )
      ),
        lastName -> text("member.name.lastName.error.required").verifying(
          firstError(
            regexp(nameRegex, "memberDetails.lastName.error.invalid"),
            maxLength(nameMaxLength, "memberDetails.lastName.error.length")
          )
        ),
      )(NameViewModel.apply)(NameViewModel.unapply)
    )

}
