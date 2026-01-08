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
import controllers.routes
import models._
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkYourAnswers.CheckYourAnswersSummary._
import views.html.CheckYourAnswersView

import java.time.LocalDate

class CheckYourAnswersViewSpec extends SpecBase {

  "view" - {
    "display correct guidance and text" in new Setup {

      view.getElementsByTag("h1").text() mustBe messages(app)("Check your answers")

      view.html.contains(messages(app)("checkYourAnswers.title"))
      view.html.contains(messages(app)("membersName.firstName"))
      view.html.contains(messages(app)("membersName.lastName"))
      view.html.contains(messages(app)("membersDob.dob"))
      view.html.contains(messages(app)("membersNino.nino"))
      view.html.contains(messages(app)("membersPsaCheckRef.pensionSchemeAdminCheckRef"))
      view.html.contains(messages(app)("site.acceptAndSubmit"))
    }
  }

  trait Setup {

    val app: Application = applicationBuilder(emptyUserAnswers).build()
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    val summaryList: Seq[SummaryListRow] = Seq(
      membersFirstNameRow(MemberDetails("Pearl", "Harvey")),
      membersLastNameRow(MemberDetails("Pearl", "Harvey")),
      membersDobRow(MembersDob(LocalDate.of(2000, 1, 1))),
      membersNinoRow(MembersNino("AB123456A")),
      membersPsaCheckRefRow(MembersPsaCheckRef("PSA12345678A"))
    )
     val backLinkUrl: String = routes.MembersPsaCheckRefController.onSubmit(NormalMode).url

    val view: Document =
      Jsoup.parse(app.injector.instanceOf[CheckYourAnswersView].apply(summaryList, "Pearl Harvey", Some(backLinkUrl)).body
      )
  }
}
