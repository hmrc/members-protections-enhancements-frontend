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
import models.MemberDetails
import play.api.data.Form
import play.api.data.Forms.mapping

class WhatIsTheMembersNameFormProvider @Inject() extends Mappings {
  private val nameMaxLength: Int = 35
  private val lastNameMinLength: Int = 2

  val nameRegex = "^[a-zA-Z\\-' ]+$"

  val firstName = "firstName"
  val lastName = "lastName"

  def apply(): Form[MemberDetails] =
    Form(
      mapping(
        firstName -> text("membersName.error.required.firstName").verifying(
          firstError(
            regexp(nameRegex, "membersName.error.invalid.firstName"),
            maxLength(nameMaxLength, "membersName.error.tooLong.firstName")
          )
        ),
        lastName -> text("membersName.error.required.lastName").verifying(
          firstError(
            regexp(nameRegex, "membersName.error.invalid.lastName"),
            minLength(lastNameMinLength, "membersName.error.tooShort.lastName"),
            maxLength(nameMaxLength, "membersName.error.tooLong.lastName")
          )
        ),
      )(MemberDetails.apply)(MemberDetails.unapply)
    )

}
