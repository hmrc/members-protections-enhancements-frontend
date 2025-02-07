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
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import viewmodels.models.{FormPageViewModel, MemberDetails}
import views.html.WhatIsTheMembersNameView

class WhatIsTheMembersNameViewSpec extends SpecBase {

  "view" - {
    "display correct guidance and text" in new Setup {

      view.getElementsByTag("h1").text() mustBe messages(app)("member.name.heading")

      view.html.contains(messages(app)("member.name.title"))
      view.text.contains(messages(app)("member.name.firstName"))
      view.text.contains(messages(app)("member.name.lastName"))
    }
  }


  trait Setup {

    val app: Application = applicationBuilder().build()
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val form =  ???
    val viewModel =  ???
    val view: Document =
      Jsoup.parse(app.injector.instanceOf[WhatIsTheMembersNameView].apply(form, viewModel).body
      )
  }

}
