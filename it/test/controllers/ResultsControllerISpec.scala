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

import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import play.api.http.Status.OK
import play.api.test.Helpers
import play.api.test.Helpers.*
import utils.IntegrationSpecBase
import models.*

class ResultsControllerISpec extends IntegrationSpecBase {

  private val mpeSubmission = "/members-protections-and-enhancements/check-and-retrieve"

  "Calling MPE service" when {
    "the user is authorised" must {
      "when completed all the sections with data redirect to final page" in {
        server.stubFor(post(urlEqualTo(s"$authoriseUri")).willReturn(ok(authResponseBody)))
        server.stubFor(
          post(urlEqualTo(s"$mpeSubmission"))
            .withRequestBody(equalToJson(fullUserAnswersJsonToPost))
            .willReturn(ok(validMPEPOSTResponse))
        )

        val response = route(
          applicationBuilder(fullUserAnswers),
          buildGet(controllers.routes.ResultsController.onPageLoad().url)
        ).get

        Helpers.status(response) mustBe OK
      }
      "when not completed all the sections with data redirect to member's name page" in {
        server.stubFor(post(urlEqualTo(s"$authoriseUri")).willReturn(ok(authResponseBody)))
        server.stubFor(
          post(urlEqualTo(s"$mpeSubmission"))
            .withRequestBody(equalToJson(fullUserAnswersJsonToPost))
            .willReturn(ok(validMPEPOSTResponse))
        )

        val response = route(
          applicationBuilder(emptyUserAnswers),
          buildGet(controllers.routes.ResultsController.onPageLoad().url)
        ).get

        Helpers.status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.routes.WhatIsTheMembersNameController.onPageLoad(NormalMode).url)
      }
    }
    "the user is not authorised" must {
      "when completed all the sections with data redirect to unauthorised page" in {
        server.stubFor(post(urlEqualTo(s"$authoriseUri")).willReturn(ok(authResponseNotAuthorisedBody)))
        server.stubFor(
          post(urlEqualTo(s"$mpeSubmission"))
            .withRequestBody(equalToJson(fullUserAnswersJsonToPost))
            .willReturn(ok(validMPEPOSTResponse))
        )

        val response = route(
          applicationBuilder(fullUserAnswers),
          buildGet(controllers.routes.ResultsController.onPageLoad().url)
        ).get

        Helpers.status(response) mustBe SEE_OTHER
        redirectLocation(response) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad().url)
      }
    }

  }
}
