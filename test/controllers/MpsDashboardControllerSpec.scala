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
import controllers.actions.FakePspIdentifierAction
import models.MemberDetails
import org.mockito.Mockito.{times, verify}
import pages.WhatIsTheMembersNamePage
import play.api.Application
import play.api.mvc.AnyContentAsEmpty
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers._

class MpsDashboardControllerSpec extends SpecBase {

  "MpsDashboardController" - {
    "must redirect to unauthorised page if user is not allowed" in {
      val userAnswers = emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value

      val application = applicationBuilder(
        userAnswers = userAnswers,
        allowListResponse = Some(Redirect(routes.UnauthorisedController.onPageLoad()))
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.MpsDashboardController.redirectToMps().url)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "add correlation ID when it does not exist" in {
      val application: Application = applicationBuilder(
        userAnswers = emptyUserAnswers,
        correlationIdInRequest = None,
        idGeneratorResponse = "id"
      ).build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.MpsDashboardController.redirectToMps().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe "http://localhost:8204/manage-pension-schemes/overview"
        verify(mockIdGenerator, times(1)).getCorrelationId
      }
    }

    "redirect to the MPS administrator dashboard for a PSA user" in {
      val application: Application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.MpsDashboardController.redirectToMps().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe "http://localhost:8204/manage-pension-schemes/overview"
        verify(mockIdGenerator, times(0)).getCorrelationId
      }
    }

    "redirect to the MPS practitioner dashboard for a PSP user" in {
      val fakePspIdentifierAction: FakePspIdentifierAction = new FakePspIdentifierAction(parsers)

      val application = applicationBuilder(
        userAnswers = emptyUserAnswers,
        identifierAction = fakePspIdentifierAction
      ).build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.MpsDashboardController.redirectToMps().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe "http://localhost:8204/manage-pension-schemes/dashboard"
        verify(mockIdGenerator, times(0)).getCorrelationId
      }
    }
  }

}
