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

package controllers

import base.SpecBase
import models._
import pages._
import play.api.i18n.Messages
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.checkYourAnswers.ResultsSummary._
import views.html.ResultsView

class ResultsControllerSpec extends SpecBase {

  "Results Controller" - {
    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers
        .set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
        .set(page = MembersDobPage, value = MembersDob(1, 1, 2022)).success.value
        .set(page = MembersNinoPage, value = MembersNino("AB123456A")).success.value
        .set(page = MembersPsaCheckRefPage, value = MembersPsaCheckRef("PSA12345678A")).success.value

      val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, routes.ResultsController.onPageLoad().url)
        implicit val msgs: Messages = messages(application)

        val result = route(application, request).value

        val view = application.injector.instanceOf[ResultsView]

        val memberDetails: Seq[Seq[TableRow]] = Seq(
          membersNameRow(MemberDetails("Pearl", "Harvey")),
          membersDobRow(MembersDob(1, 1, 2022)),
          membersNinoRow(MembersNino("AB123456A")),
          membersPsaCheckRefRow(MembersPsaCheckRef("PSA12345678A"))
        )

        val backLinkRoute = routes.CheckYourAnswersController.onPageLoad().url
        val localDateTime: String = "02 April 2025 at 15:12"

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(memberDetails, Some(backLinkRoute), localDateTime)(request, messages(application)).toString
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        val request = FakeRequest(GET, routes.ResultsController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }

}
