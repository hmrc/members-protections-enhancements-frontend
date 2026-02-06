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

import forms.behaviours.StringFieldBehaviours
import models.MemberDetails
import play.api.data.{Form, FormError}

class WhatIsTheMembersNameFormProviderSpec extends StringFieldBehaviours {

  private val formProvider = new WhatIsTheMembersNameFormProvider()

  import formProvider.*

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

    behave.like(
      fieldWithRegex(
        form,
        fieldName = "firstName",
        invalidString = "!ValidName",
        error = "membersName.error.invalid.firstName"
      )
    )

    behave.like(
      fieldWithMaxLength(
        form,
        fieldName = "firstName",
        maxLength = 35,
        "membersName.error.tooLong.firstName"
      )
    )

    "strip whitespace from the user's submission" in {
      val result: Form[MemberDetails] = form.bind(Map(
        "firstName" -> "     First       Name   ",
        "lastName" -> "Last Name"
      ))

      result.value mustBe Some(MemberDetails("First Name", "Last Name"))
    }
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

    behave.like(
      fieldWithRegex(
        form,
        fieldName = "lastName",
        invalidString = "!ValidName",
        error = "membersName.error.invalid.lastName"
      )
    )

    behave.like(
      fieldWithMaxLength(
        form,
        fieldName = "lastName",
        maxLength = 35,
        "membersName.error.tooLong.lastName"
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

    "strip whitespace from the user's submission" in {
      val result: Form[MemberDetails] = form.bind(Map(
        "firstName" -> "First Name",
        "lastName" -> "       Last       Name       "
      ))

      result.value mustBe Some(MemberDetails("First Name", "Last Name"))
    }
  }

  "stripWhitespace" must {
    "return the same string when an input has no whitespace" in {
      stripWhitespace("Word") mustBe "Word"
    }

    "remove any leading spaces from the input" in {
      stripWhitespace("    Word") mustBe "Word"
    }

    "remove any trailing spaces from the input" in {
      stripWhitespace("Word     ") mustBe "Word"
    }

    "remove any double spaces within the input" in {
      stripWhitespace("Word  Word2") mustBe "Word Word2"
    }

    "strip all invalid whitespace" in {
      stripWhitespace("    Word  Word2    ") mustBe "Word Word2"
    }
  }
}
