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

package viewmodels.govuk

import play.api.data.Field
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.hint.Hint
import uk.gov.hmrc.govukfrontend.views.viewmodels.input.Input
import uk.gov.hmrc.govukfrontend.views.viewmodels.label.Label
import viewmodels.{ErrorMessageAwareness, InputWidth}

trait InputFluency {
  object InputViewModel extends ErrorMessageAwareness {
    def apply(field: Field, label: Label)(implicit messages: Messages): Input =
      Input(
        id = field.id,
        name = field.name,
        value = field.value,
        label = label,
        errorMessage = errorMessage(field)
      )
  }

  implicit class FluentInput(input: Input) {
    def withHint(hint: Hint): Input =
      input.copy(hint = Some(hint))

    def withCssClass(newClass: String): Input =
      input.copy(classes = s"${input.classes.strip()} $newClass")

    def withAutocomplete(value: String): Input =
      input.copy(autocomplete = Some(value))

    def withWidth(inputWidth: InputWidth): Input =
      input.withCssClass(inputWidth.toString)
  }
}
