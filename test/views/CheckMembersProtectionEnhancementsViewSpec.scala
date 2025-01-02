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

package views

import base.SpecBase
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.CheckMembersProtectionEnhancementsView

class CheckMembersProtectionEnhancementsViewSpec extends SpecBase {

  "view" - {
    "display correct guidance and text" in new Setup {

      view.getElementsByTag("h1").text() mustBe messages(app)("check.members.protection.enhancements.heading")

      view.html.contains(messages(app)("check.members.protection.enhancements.p1"))
      view.text.contains(messages(app)("check.members.protection.enhancements.full-name"))
      view.text.contains(messages(app)("check.members.protection.enhancements.dob"))
      view.text.contains(messages(app)("check.members.protection.enhancements.nino"))
      view.text.contains(messages(app)("check.members.protection.enhancements.pension-scheme-admin-check-ref"))
    }
  }


  trait Setup {

    val app: Application = applicationBuilder().build()
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val view: Document =
      Jsoup.parse(app.injector.instanceOf[CheckMembersProtectionEnhancementsView].apply().body
      )
  }

}