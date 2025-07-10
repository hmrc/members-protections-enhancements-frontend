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
import controllers.routes
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.WhatYouWillNeedView

class WhatYouWillNeedViewSpec extends SpecBase {

  "view" - {

    "with correct Mpe gov banner" in new Setup {
      view.getElementsByClass("govuk-header__link govuk-header__service-name").text() mustBe messages(app)("service.name")
      view.getElementsByClass("govuk-link hmrc-sign-out-nav__link").attr("href") mustBe
        "/members-protections-and-enhancements/account/sign-out-survey"
    }

    "with correct breadcrumbs" in new Setup {
      view.getElementsByClass("govuk-breadcrumbs__link").first().text() mustBe messages(app)("results.breadcrumbs.mps")
      view.getElementsByClass("govuk-breadcrumbs__link").last().text() mustBe messages(app)("results.breadcrumbs.mpe")
    }

    "display correct guidance and text" in new Setup {

      view.getElementsByTag("h1").text() mustBe messages(app)("whatYouWillNeed.heading")

      view.html.contains(messages(app)("whatYouWillNeed.p1"))
      view.text.contains(messages(app)("whatYouWillNeed.full-name"))
      view.text.contains(messages(app)("whatYouWillNeed.dob"))
      view.text.contains(messages(app)("whatYouWillNeed.nino"))
      view.text.contains(messages(app)("whatYouWillNeed.pension-scheme-admin-check-ref"))

      view.text.contains(messages(app)("whatYouWillNeed.guidance.p1"))
      view.text.contains(messages(app)("whatYouWillNeed.guidance.p2"))
      view.text.contains(messages(app)("whatYouWillNeed.guidance.li.1"))

      view.getElementsByClass("govuk-list govuk-list--bullet govuk-!-margin-bottom-6").last().getElementsByTag("a").text() mustBe
        messages(app)("whatYouWillNeed.guidance.li.2.linkText")
      view.getElementsByClass("govuk-list govuk-list--bullet govuk-!-margin-bottom-6").last().getElementsByTag("a").attr("href") mustBe
        "https://www.gov.uk/guidance/pension-schemes-protect-your-lifetime-allowance"
    }
  }


  trait Setup {

    val app: Application = applicationBuilder(emptyUserAnswers).build()
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val backLinkUrl: String = routes.MpsDashboardController.redirectToMps().url

    val view: Document =
      Jsoup.parse(app.injector.instanceOf[WhatYouWillNeedView].apply(Some(backLinkUrl)).body)
  }

}
