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
import models.CorrelationId
import models.requests.{IdentifierRequest, RequestWithCorrelationId}
import models.requests.IdentifierRequest.AdministratorRequest
import models.requests.UserType.PSA
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{times, verify, when}
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import services.FailedAttemptService
import uk.gov.hmrc.auth.core.AffinityGroup

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class CheckLockoutActionSpec extends SpecBase {
  trait Test {
    val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]
    val mockService: FailedAttemptService = mock[FailedAttemptService]
    val mockSessionRepo: SessionRepository = mock[SessionRepository]
    implicit val correlationId: CorrelationId = "X-ID"

    val testAction: CheckLockoutActionImpl = new CheckLockoutActionImpl(
      config = mockConfig,
      failedAttemptService = mockService,
      mockSessionRepo
    )

    val unauthorisedResult: Future[Result] = Future.successful(
      Redirect(routes.UnauthorisedController.onPageLoad())
    )
  }

  "invokeBlock" -> {
    "should invoke block when a user is not locked out" in new Test {
      when(
        mockService.checkForLockout()(ArgumentMatchers.any(), ArgumentMatchers.any())
      ).thenReturn(
        Future.successful(false)
      )
      val result: Future[Result] = testAction.invokeBlock(
        request = AdministratorRequest(
          affGroup = AffinityGroup.Individual,
          userId = "anId",
          psaId = "anotherId",
          psrUserType = PSA,
          request = RequestWithCorrelationId(FakeRequest(), correlationId)
        ),
        block = (_: IdentifierRequest[AnyContentAsEmpty.type]) => unauthorisedResult
      )


      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.UnauthorisedController.onPageLoad().url)
    }

    "should return lockout redirect when user is locked out" in new Test {
      when(
        mockService.checkForLockout()(ArgumentMatchers.any(), ArgumentMatchers.any())
      ).thenReturn(
        Future.successful(true)
      )

      when(mockSessionRepo.clear(ArgumentMatchers.any())).thenReturn(Future.successful(true))

      val result: Future[Result] = testAction.invokeBlock(
        request = AdministratorRequest(
          affGroup = AffinityGroup.Individual,
          userId = "anId",
          psaId = "anotherId",
          psrUserType = PSA,
          request = RequestWithCorrelationId(FakeRequest(), correlationId)
        ),
        block = (_: IdentifierRequest[AnyContentAsEmpty.type]) => unauthorisedResult
      )

      status(result) mustBe SEE_OTHER
      redirectLocation(result) mustBe Some(controllers.routes.LockedOutController.onPageLoad().url)
      verify(mockSessionRepo, times(1)).clear(ArgumentMatchers.any())
    }
  }
}
