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

class MembersNinoFormProvider @Inject() extends Mappings {

  private val ninoRegex: String = """[A-Za-z]{2}[0-9]{6}[A-Za-z]{1}"""
  private val trnRegex: String = """^[0-9]{2}[A-Za-z]{1}[0-9]{5}$"""
  private val nino = "nino"

  def apply(): Form[MembersNino] =
    Form(
      mapping(
        nino -> text("membersNino.error.required").verifying(
          "membersNino.error.invalid", value => value.matches(ninoRegex) || value.matches(trnRegex)
        )
      )(MembersNino.apply)(MembersNino.unapply)
    )
}
