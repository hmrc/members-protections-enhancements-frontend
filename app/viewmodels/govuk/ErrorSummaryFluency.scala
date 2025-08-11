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

import play.api.data.Form
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.errorsummary.{ErrorLink, ErrorSummary}

trait ErrorSummaryFluency {
  object ErrorSummaryViewModel {
    def apply(form: Form[_], formId: String = "", errorLinkOverrides: Map[String, String] = Map.empty)
             (implicit messages: Messages): ErrorSummary = {

      val errors: Seq[ErrorLink] = form.errors.foldLeft(Seq.empty[ErrorLink])((errorLinks, error) => {
          if(error.message.contains("missing")){
            Seq(ErrorLink(
              href = if (error.key == formId) Some(s"#${formId}.day") else Some(s"#${error.key}"),
              content = Text(messages(error.message, error.args: _*))
            ))
          }
        else{
          errorLinks :+ ErrorLink(
            href = if (error.key == formId) Some(s"#${formId}.day") else Some(s"#${error.key}"),
            content = Text(messages(error.message, error.args: _*))
          )
        }
        })

      ErrorSummary(
        errorList = errors,
        title     = Text(messages("error.summary.title"))
      )
    }
  }
}
