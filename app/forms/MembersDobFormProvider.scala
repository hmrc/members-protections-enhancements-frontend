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
import models._
import play.api.data.{Form, FormError}
import providers.DateTimeProvider

class MembersDobFormProvider @Inject()(dateTimeProvider: DateTimeProvider) extends Mappings {

  def apply(): Form[MembersDob] = Form(
      "dateOfBirth" -> dateOfBirth(dateTimeProvider)
        .verifying(
          firstError(
            validDate("membersDob.error.invalidDate"),
            futureDate("membersDob.error.futureDate", dateTimeProvider)
          )
        )
    )
}

object MembersDobFormProvider {
  def combineMissingErrors(form: Form[MembersDob]): Form[MembersDob] = {
    val errToMessageSuffix: FormError => String = (err: FormError) => err.message.split("\\.").last

    val dateOfBirthError: String = "dateOfBirth"
    val missingError = "membersDob.error.missing"

    form.errors.filter(_.message.contains("missing")) match {
      case head :: Nil =>
        form.copy(
          errors = Seq(FormError(
            key = head.key,
            message = s"$missingError.single",
            args = Seq(errToMessageSuffix(head))
          ))
        )
      case head :: head2 :: Nil =>
        form.copy(
          errors = Seq(FormError(
            key = dateOfBirthError,
            message = s"$missingError.double",
            args = Seq(errToMessageSuffix(head), errToMessageSuffix(head2))
          ))
        )
      case _ :: _ :: _ =>
        form.copy(
          errors = Seq(FormError(
            key = dateOfBirthError,
            message = missingError
          )
        ))
      case _ => form
    }
  }
}

