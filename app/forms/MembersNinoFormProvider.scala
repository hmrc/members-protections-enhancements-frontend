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

  private val validCharsRegex = "^[a-zA-Z\\d]+$"

  private val identifierRegex: String = """^((([ACEHJLMOPRSWXY][A-CEGHJ-NPR-TW-Z]|B[A-CEHJ-NPR-TW-Z]|G[ACEGHJ-NPR-TW-Z]|[KT]" +
    "[A-CEGHJ-MPR-TW-Z]|N[A-CEGHJL-NPR-SW-Z]|Z[A-CEGHJ-NPR-TW-Y])[0-9]{6})[A-D]?|([0-9]{2}[A-Z]{1}[0-9]{5}))$"""

  private val nino = "nino"

  def apply(): Form[MembersNino] =
    Form(
      mapping(
        nino -> text("membersNino.error.required")
          .transform[String](_.filterNot(_.isWhitespace), identity)
          .verifying(
            "membersNino.error.invalid.characters", value => value.matches(validCharsRegex)
          )
          .verifying(
            "membersNino.error.invalid.format", value => value.matches(identifierRegex)
          )
      )(MembersNino.apply)(MembersNino.unapply)
    )
}
