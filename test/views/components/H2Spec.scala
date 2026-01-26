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

package views.components

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.i18n.{Messages, MessagesApi}
import play.twirl.api.Html
import views.html.components.h2

class H2Spec extends SpecBase {

  "H2 component" - {
    "display correct contents" in {
      val app = applicationBuilder(emptyUserAnswers).build()
      implicit lazy val messages: Messages = app.injector.instanceOf[MessagesApi].preferred(Seq.empty)

      val view: Html = new h2()("test-id", Some("Test heading"))

      val doc: Document = Jsoup.parse(view.toString)

      val element = doc.select("h2")
      element.size mustBe 1

      element.attr("class").split(" ") must contain.allOf("govuk-heading-m", "govuk-!-margin-bottom-12")
      element.text() mustBe "test-id"
    }
  }

}
