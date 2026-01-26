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

package controllers

import base.SpecBase
import controllers.actions.FakePspIdentifierAction
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._

class MpsDashboardControllerSpec extends SpecBase {

  "MpsDashboardController" - {
    "redirect to the MPS administrator dashboard for a PSA user" in {

      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.MpsDashboardController.redirectToMps().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe "http://localhost:8204/manage-pension-schemes/overview"

      }
    }

    "redirect to the MPS practitioner dashboard for a PSP user" in {

      val fakePspIdentifierAction: FakePspIdentifierAction = new FakePspIdentifierAction(parsers)
      val application = applicationBuilder(userAnswers = emptyUserAnswers, fakePspIdentifierAction).build()

      running(application) {
        implicit val request: FakeRequest[AnyContentAsEmpty.type] =
          FakeRequest(GET, routes.MpsDashboardController.redirectToMps().url)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustBe "http://localhost:8204/manage-pension-schemes/overview"
      }
    }
  }

}
