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
import play.api.data.Form
import play.api.data.Forms.mapping

import java.time.LocalDate
import scala.util.Try

class MembersDobFormProvider @Inject() extends Mappings {

  private val maxDate: LocalDate = LocalDate.now()
  private val minDate: LocalDate = maxDate.minusYears(130)

   val dateDayRegex = "([0-9]{1,2})"
   val dateMonthRegex = "([0-9]{1,2})"
   val dateYearRegex = "([0-9]{4})"

  def apply(): Form[MembersDob] =
    Form(
      "date" -> mapping(
        "day" -> text("memberDob.error.required.day").verifying(
          firstError(
            regexp(dateDayRegex, "memberDob.error.invalid"),
          )
        ),
        "month" -> text("memberDob.error.required.month").verifying(
          firstError(
            regexp(dateMonthRegex, "memberDob.error.invalid")
          )
        ),
        "year" -> text("memberDob.error.required.year").verifying(
          firstError(
            regexp(dateYearRegex, "memberDob.error.invalid")
          )
        )
      )(MembersDob.apply)(MembersDob.unapply)
        .verifying("memberDob.error.invalid", dob => isValidDate(dob))
        .verifying("memberDob.error.dateOfBirth.maxDate", dob => isWithinDateRange(dob))
    )

  private def isValidDate(dob: MembersDob): Boolean = {
    Try(LocalDate.of(dob.year.toInt, dob.month.toInt, dob.day.toInt)).isSuccess
  }

  private def isWithinDateRange(dob: MembersDob): Boolean = {
    Try {
      val date = LocalDate.of(dob.year.toInt, dob.month.toInt, dob.day.toInt)
      !date.isAfter(maxDate) && !date.isBefore(minDate)
    }.getOrElse(false)
  }
}
