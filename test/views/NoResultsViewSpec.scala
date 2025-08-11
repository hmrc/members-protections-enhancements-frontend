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
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.NoResultsView

class NoResultsViewSpec extends SpecBase {

  "view" - {
    "display correctly" - {
      "with correct top level header and title" in new Setup {
        view.html must include (messages(app)("noResults.title"))
        view.getElementsByTag("h1").text() mustBe messages(app)("noResults.heading")
      }
      "with correct breadcrumbs" in new Setup {
        view.getElementsByClass("govuk-breadcrumbs__link").first().text() mustBe messages(app)("results.breadcrumbs.mps")
        view.getElementsByClass("govuk-breadcrumbs__link").last().text() mustBe messages(app)("results.breadcrumbs.mpe")
      }
      "with expected user details section" in new Setup {
        view.html must include (messages(app)("results.memberDetails.heading"))
        view.html must include (messages(app)("membersName.name"))
        view.html must include (messages(app)("membersDob.dob"))
        view.html must include (messages(app)("membersNino.nino"))
        view.html must include (messages(app)("membersPsaCheckRef.pensionSchemeAdminCheckRef"))
      }

      "with expected footer content" in new Setup {
        view.html must include (messages(app)("results.checkedOn"))
        view.html must include (messages(app)("site.print"))
        view.html must include (messages(app)("noResults.lh"))
        view.html must include (messages(app)("noResults.li.1"))
        view.html must include (messages(app)("noResults.li.2"))
        view.html must include (messages(app)("noResults.checkInformation"))
        view.html must include (messages(app)("noResults.tryAgain"))
      }
    }
  }

  trait Setup {
    val app: Application = applicationBuilder(emptyUserAnswers).build()
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val memberDetails: MemberDetails = MemberDetails("Pearl", "Harvey")
    val membersDob: MembersDob = MembersDob(1, 1, 2022)
    val membersNino: MembersNino = MembersNino("AB123456A")
    val membersPsaCheckRef: MembersPsaCheckRef = MembersPsaCheckRef("PSA12345678A")

    val backLinkUrl: String = routes.MembersPsaCheckRefController.onSubmit(NormalMode).url

    val localDateTime: String = "02 April 2025 at 15:12"

    val view: Document = Jsoup.parse(
      app.injector.instanceOf[NoResultsView].apply(
        memberDetails = memberDetails,
        membersDob = membersDob,
        membersNino = membersNino,
        membersPsaCheckRef = membersPsaCheckRef,
        formattedTimestamp = localDateTime
      ).body
    )
  }

}
