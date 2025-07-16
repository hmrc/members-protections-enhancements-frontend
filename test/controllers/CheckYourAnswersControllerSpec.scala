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
import org.mockito.Mockito.{times, verify}
import pages.{MembersDobPage, MembersNinoPage, MembersPsaCheckRefPage, WhatIsTheMembersNamePage}
import play.api.http.Status.OK
import play.api.i18n.Messages
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import utils.IdGenerator
import viewmodels.checkYourAnswers.CheckYourAnswersSummary._
import viewmodels.govuk.SummaryListFluency
import views.html.CheckYourAnswersView

class CheckYourAnswersControllerSpec extends SpecBase with SummaryListFluency  {


  val userAnswers: UserAnswers = emptyUserAnswers
    .set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
    .set(page = MembersDobPage, value = MembersDob(1, 1, 2000)).success.value
    .set(page = MembersNinoPage, value = MembersNino("AB123456A")).success.value
    .set(page = MembersPsaCheckRefPage, value = MembersPsaCheckRef("PSA12345678A")).success.value

  "Check Your Answers Controller" - {

    "must return OK and the correct view for a GET when userAnswers are present" - {
      "when data request has no correlation id" in {

        val mockIdGenerator = mock[IdGenerator]
        val application = applicationBuilder(userAnswers = userAnswers)
          .overrides(
            inject.bind(classOf[IdGenerator]).to(mockIdGenerator)
          ).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
          implicit val msgs: Messages = messages(application)

          val result = route(application, request).value
          val view = application.injector.instanceOf[CheckYourAnswersView]
          val list: Seq[SummaryListRow] = Seq(
            membersFirstNameRow(MemberDetails("Pearl", "Harvey")),
            membersLastNameRow(MemberDetails("Pearl", "Harvey")),
            membersDobRow(MembersDob(1, 1, 2000)),
            membersNinoRow(MembersNino("AB123456A")),
            membersPsaCheckRefRow(MembersPsaCheckRef("PSA12345678A"))
          )
          val backLinkRoute = routes.MembersPsaCheckRefController.onPageLoad(NormalMode).url

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list, "Pearl Harvey", Some(backLinkRoute))(request, messages(application)).toString
          verify(mockIdGenerator, times(1)).getCorrelationId
        }
      }

      "when data request has correlation id, no need to generate new" in {

        val mockIdGenerator = mock[IdGenerator]
        val application = applicationBuilder(userAnswers = userAnswers, correlationId = Some("X-123"))
          .overrides(
            inject.bind(classOf[IdGenerator]).to(mockIdGenerator)
          ).build()

        running(application) {
          val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)
          implicit val msgs: Messages = messages(application)

          val result = route(application, request).value

          val view = application.injector.instanceOf[CheckYourAnswersView]
          val list: Seq[SummaryListRow] = Seq(
            membersFirstNameRow(MemberDetails("Pearl", "Harvey")),
            membersLastNameRow(MemberDetails("Pearl", "Harvey")),
            membersDobRow(MembersDob(1, 1, 2000)),
            membersNinoRow(MembersNino("AB123456A")),
            membersPsaCheckRefRow(MembersPsaCheckRef("PSA12345678A"))
          )
          val backLinkRoute = routes.MembersPsaCheckRefController.onPageLoad(NormalMode).url

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(list, "Pearl Harvey", Some(backLinkRoute))(request, messages(application)).toString
          verify(mockIdGenerator, times(0)).getCorrelationId
        }
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {

      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        val request = FakeRequest(GET, routes.CheckYourAnswersController.onPageLoad().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }

    "must return to ResultsController page when submitted" in {

      val application = applicationBuilder(userAnswers = userAnswers).build()

      val onSubmit = routes.CheckYourAnswersController.onSubmit()
      running(application) {
        val request = FakeRequest(GET, onSubmit.url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ResultsController.onPageLoad().url
      }
    }
  }
}
