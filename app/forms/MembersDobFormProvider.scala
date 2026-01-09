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
import providers.DateTimeProvider

import java.time.LocalDate

class MembersDobFormProvider @Inject()(dateTimeProvider: DateTimeProvider) extends Mappings {

  def apply(): Form[MembersDob] = Form[MembersDob](
    "dateOfBirth" -> localDate(
      dayInvalidKey = "membersDob.error.invalid.day",
      monthInvalidKey = "membersDob.error.invalid.month",
      yearInvalidKey = "membersDob.error.invalid.year",
      monthTextInvalidKey = "membersDob.error.invalid.month.nonNumeric",
      oneRequiredKey = "membersDob.error.missing.one",
      twoRequiredKey = "membersDob.error.missing.two",
      allRequiredKey = "membersDob.error.missing.all",
      realDateKey = "membersDob.error.missing.real"
    ).verifying(
      minDate(MembersDobFormProvider.minDate, "membersDob.error.missing.past"),
      maxDate(dateTimeProvider.now().toLocalDate, "membersDob.error.missing.future")
    ).transform[MembersDob](
      MembersDob(_),
      _.dateOfBirth
    )
  )
}

object MembersDobFormProvider {
  val minDate: LocalDate = LocalDate.of(1900, 1, 1)
}

