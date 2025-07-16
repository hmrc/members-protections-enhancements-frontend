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

package controllers.actions

import base.SpecBase
import config.FrontendAppConfig
import controllers.routes
import models.requests.IdentifierRequest
import models.requests.IdentifierRequest.AdministratorRequest
import models.requests.UserType.PSA
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{times, verify, when}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AllowListActionSpec extends SpecBase {
  trait Test {
    val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
    val mockSessionRepo: SessionRepository = mock[SessionRepository]

    val testAction: AllowListActionImpl = new AllowListActionImpl(
      frontendAppConfig = mockConfig
    )

    val blockResult: Future[Result] = Future.successful(
      Redirect(routes.CheckYourAnswersController.onPageLoad())
    )
  }

  "invokeBlock" -> {
    "should invoke block when allow listing is disabled" in new Test {
      when(mockConfig.allowListEnabled).thenReturn(false)

      val result: Future[Result] = testAction.invokeBlock(
        request = AdministratorRequest(AffinityGroup.Individual, "anId", "anotherId", PSA, FakeRequest()),
        block = (_: IdentifierRequest[AnyContentAsEmpty.type]) => blockResult
      )

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.CheckYourAnswersController.onPageLoad().url)
    }

    "should invoke block when allow listing is enabled and user is allowed" in new Test {
      when(mockConfig.allowListEnabled).thenReturn(true)
      when(mockConfig.allowedPsrIds).thenReturn(Seq("anotherId"))

      val result: Future[Result] = testAction.invokeBlock(
        request = AdministratorRequest(AffinityGroup.Individual, "anId", "anotherId", PSA, FakeRequest()),
        block = (_: IdentifierRequest[AnyContentAsEmpty.type]) => blockResult
      )

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.CheckYourAnswersController.onPageLoad().url)
    }

    "should return redirect when user is not allowed and allow list is enabled" in new Test {
      when(mockConfig.allowListEnabled).thenReturn(true)
      when(mockConfig.allowedPsrIds).thenReturn(Seq("notAnId"))

      val result: Future[Result] = testAction.invokeBlock(
        request = AdministratorRequest(AffinityGroup.Individual, "anId", "anotherId", PSA, FakeRequest()),
        block = (_: IdentifierRequest[AnyContentAsEmpty.type]) => blockResult
      )

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad().url)
    }
  }
}
