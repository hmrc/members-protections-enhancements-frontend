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
import models.{MemberDetails, MembersDob, MembersNino, MembersPsaCheckRef, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import views.html.ResultsView

class ResultsViewSpec extends SpecBase {

  "view" - {
    "display correct guidance and text" in new Setup {

      view.getElementsByTag("h1").text() mustBe messages(app)("results.heading")
      view.html.contains(messages(app)("results.title"))
      view.html.contains(messages(app)("results.memberDetails.heading"))
      view.html.contains(messages(app)("membersName.name"))
      view.html.contains(messages(app)("membersDob.dob"))
      view.html.contains(messages(app)("membersNino.nino"))
      view.html.contains(messages(app)("results.relatedContent"))
      view.html.contains(messages(app)("results.mpsDashboard"))
      view.html.contains(messages(app)("results.checkAnotherMpe"))
      view.html.contains(messages(app)("results.moreInfo"))
      view.html.contains(messages(app)("results.checkedOn"))
      view.html.contains(messages(app)("site.print"))

      view.html.contains(messages(app)("results.takingHigherTaxFreeLumpSumsUrl"))
      view.html.contains(messages(app)("results.statusKey"))
      view.html.contains(messages(app)("results.protectedAmtKey"))
      view.html.contains(messages(app)("results.protectionRefNumKey"))
      view.html.contains(messages(app)("results.lumpSumKey"))
      view.html.contains(messages(app)("results.factorKey"))
      view.html.contains(messages(app)("results.enhancementFactorKey"))

      view.html.contains(messages(app)("results.individualProtectionSummaryCard"))
      view.html.contains(messages(app)("results.individualProtectionStatusValue"))
      view.html.contains(messages(app)("results.individualProtectedAmtValue"))
      view.html.contains(messages(app)("results.individualProtectionRefNumValue"))

      view.html.contains(messages(app)("results.fixedProtectionSummaryCard"))
      view.html.contains(messages(app)("results.fixedProtectionStatusValue"))
      view.html.contains(messages(app)("results.fixedProtectionRefNumValue"))

      view.html.contains(messages(app)("results.primaryProtectionSummaryCard"))
      view.html.contains(messages(app)("results.primaryProtectionStatusValue"))
      view.html.contains(messages(app)("results.primaryProtectionLumpSumValue"))
      view.html.contains(messages(app)("results.primaryProtectionFactorValue"))

      view.html.contains(messages(app)("results.nonResidentFactorEnhancementStatusCard"))
      view.html.contains(messages(app)("results.nonResidentFactorEnhancementStatusValue"))
      view.html.contains(messages(app)("results.nonResidentFactorEnhancementFactorValue"))
      view.html.contains(messages(app)("results.nonResidentFactorEnhancementProtectionRefNumValue"))
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

    val view: Document =
      Jsoup.parse(app.injector.instanceOf[ResultsView].apply(memberDetails, membersDob, membersNino, membersPsaCheckRef,
        Some(backLinkUrl), localDateTime).body
      )
  }

}
