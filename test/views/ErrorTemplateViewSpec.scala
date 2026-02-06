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

package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.ErrorTemplate

class ErrorTemplateViewSpec extends SpecBase {

  "view" - {
    "display correct error information" in new Setup {

      view.getElementsByTag("h1").text() mustBe messages(app)("journeyRecovery.continue.heading")
      view.getElementsByClass("govuk-body").text().contains(messages(app)("error.try.again"))
      view.getElementsByClass("govuk-body").text().contains(messages(app)("error.contact"))

    }
  }


  trait Setup {

    val app: Application = applicationBuilder(emptyUserAnswers).build()
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val view: Document =
      Jsoup.parse(app.injector.instanceOf[ErrorTemplate].apply("journeyRecovery.continue.title", "journeyRecovery.continue.heading").body)
  }

}
