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
import org.mockito.Mockito.{times, verify}
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers._
import views.html.WhatYouWillNeedView

class WhatYouWillNeedControllerSpec extends SpecBase {

  private lazy val backLinkUrl = routes.MpsDashboardController.redirectToMps().url

  "Check Members Protection Enhancements Controller" - {
    "must return OK and the correct view for a GET" - {
      "when correlation ID isn't found in the request" in {
        val application = applicationBuilder(
          userAnswers = emptyUserAnswers,
          correlationIdInRequest = None,
          idGeneratorResponse = "id"
        ).build()

        running(application) {
          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.WhatYouWillNeedController.onPageLoad().url)

          implicit val msg: Messages = messages(application)

          val result = route(application, request).value

          val view = application.injector.instanceOf[WhatYouWillNeedView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(Some(backLinkUrl)).toString
          verify(mockIdGenerator, times(1)).getCorrelationId
        }
      }

      "when correlation ID exists in the request" in {
        val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

        running(application) {
          implicit val request: FakeRequest[AnyContentAsEmpty.type] =
            FakeRequest(GET, routes.WhatYouWillNeedController.onPageLoad().url)

          implicit val msg: Messages = messages(application)

          val result = route(application, request).value

          val view = application.injector.instanceOf[WhatYouWillNeedView]

          status(result) mustEqual OK
          contentAsString(result) mustEqual view(Some(backLinkUrl)).toString
          verify(mockIdGenerator, times(0)).getCorrelationId
        }
      }
    }
  }
}
