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

package services

import base.SpecBase
import config.FrontendAppConfig
import models.CorrelationId
import models.mongo.CacheUserDetails
import models.requests.{IdentifierRequest, RequestWithCorrelationId}
import models.requests.IdentifierRequest.AdministratorRequest
import models.requests.UserType.{PSA, PSP}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.http.Status.IM_A_TEAPOT
import play.api.mvc.Results._
import play.api.mvc.{AnyContentAsEmpty, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, contentAsString, defaultAwaitTimeout, status}
import repositories.{FailedAttemptCountRepository, FailedAttemptLockoutRepository}
import uk.gov.hmrc.auth.core.AffinityGroup

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FailedAttemptServiceSpec extends SpecBase {

  trait Test {
    val mockCountRepo: FailedAttemptCountRepository = mock[FailedAttemptCountRepository]
    val mockLockoutRepo: FailedAttemptLockoutRepository = mock[FailedAttemptLockoutRepository]
    val mockConfig: FrontendAppConfig = mock[FrontendAppConfig]

    implicit val correlationId: CorrelationId = "X-ID"

    val service: FailedAttemptServiceImpl = new FailedAttemptServiceImpl(
      lockoutRepo = mockLockoutRepo,
      countRepo = mockCountRepo,
      appConfig = mockConfig
    )

    lazy val lockoutThreshold: Int = 5
    when(mockConfig.lockoutThreshold).thenReturn(lockoutThreshold)

    lazy val addAttemptResult: Future[Unit] = Future.successful()
    when(
      mockCountRepo.addFailedAttempt()(ArgumentMatchers.any(), ArgumentMatchers.any())
    )
      .thenReturn(addAttemptResult)

    lazy val attemptAmount: Long = 4L
    lazy val countAttemptResult: Future[Long] = Future.successful(attemptAmount)
    when(
      mockCountRepo.countFailedAttempts()(ArgumentMatchers.any(), ArgumentMatchers.any())
    )
      .thenReturn(countAttemptResult)

    lazy val createLockoutResult: Future[Unit] = Future.successful()
    when(
      mockLockoutRepo.putCache(ArgumentMatchers.any())(ArgumentMatchers.any())(ArgumentMatchers.any())
    ).thenReturn(createLockoutResult)

    val timestampSeconds: Long = 100000L
    val instantTime: Option[Instant] = Some(Instant.ofEpochSecond(timestampSeconds))
    lazy val checkLockoutResult: Future[Option[CacheUserDetails]] = Future.successful(Some(
      CacheUserDetails(
        psrUserType = PSA,
        psrUserId = None,
        createdAt = instantTime
      )
    ))
    when(
      mockLockoutRepo.getFromCache(ArgumentMatchers.any())
    ).thenReturn(checkLockoutResult)

    lazy val getExpiryResult: Future[Option[Instant]] = Future.successful(instantTime)
    when(
      mockLockoutRepo.getLockoutExpiry(ArgumentMatchers.any())(ArgumentMatchers.any())
    ).thenReturn(getExpiryResult)

    implicit val request: IdentifierRequest[AnyContentAsEmpty.type] = AdministratorRequest(
      affGroup = AffinityGroup.Individual,
      userId = "internalId",
      psaId = "psaId",
      psrUserType = PSA,
      request = RequestWithCorrelationId(FakeRequest(), correlationId)
    )
  }

  "checkForLockout" - {
    "should return true when a matching lockout exists" in new Test {
      val result: Future[Boolean] = service.checkForLockout()
      await(result) mustBe true
    }

    "should return false when no lockout exists" in new Test {
      override lazy val checkLockoutResult: Future[Option[CacheUserDetails]] = Future.successful(None)

      val result: Future[Boolean] = service.checkForLockout()
      await(result) mustBe false
    }

    "should return an exception for a non matching lockout" in new Test {
      override lazy val checkLockoutResult: Future[Option[CacheUserDetails]] = Future.successful(Some(
        CacheUserDetails(
          psrUserType = PSP,
          psrUserId = None,
          createdAt = Some(Instant.ofEpochSecond(timestampSeconds))
        )
      ))

      val result: Future[Boolean] = service.checkForLockout()
      assertThrows[IllegalStateException](
        await(result)
      )
    }

    "should throw an error when lockout check fails" in new Test{
      override lazy val checkLockoutResult: Future[Option[CacheUserDetails]] = Future.failed(
        new RuntimeException("error")
      )

      val result: Future[Boolean] = service.checkForLockout()
      assertThrows[RuntimeException](await(result))
    }
  }

  "handleFailedAttempt" - {
    "should throw an error when adding failed attempt fails" in new Test {
      override lazy val addAttemptResult: Future[Unit] = Future.failed(new RuntimeException("error"))
      val result: Future[Result] = service.handleFailedAttempt(Ok(""))(Ok(""))

      assertThrows[RuntimeException](await(result))
    }

    "should throw an error when counting failed attempts fails" in new Test {
      override lazy val countAttemptResult: Future[Long] = Future.failed(new RuntimeException("error"))
      val result: Future[Result] = service.handleFailedAttempt(Ok(""))(Ok(""))

      assertThrows[RuntimeException](await(result))
    }

    "should return no lockout result when count is below the threshold" in new Test {
      val result: Future[Result] = service.handleFailedAttempt(Ok(""))(ImATeapot("teapot time"))
      status(result) mustBe IM_A_TEAPOT
      contentAsString(result) mustBe "teapot time"
    }

    "should return an error when above threshold and lockout creation fails" in new Test {
      override lazy val countAttemptResult: Future[Long] = Future.successful(lockoutThreshold + 1)
      override lazy val createLockoutResult: Future[Unit] = Future.failed(new RuntimeException("error"))

      val result: Future[Result] = service.handleFailedAttempt(Ok(""))(Ok(""))
      assertThrows[RuntimeException](await(result))
    }

    "should return lockout result when above the threshold" in new Test {
      override lazy val countAttemptResult: Future[Long] = Future.successful(lockoutThreshold + 1)

      val result: Future[Result] = service.handleFailedAttempt(ImATeapot("teapot time"))(Ok(""))
      status(result) mustBe IM_A_TEAPOT
      contentAsString(result) mustBe "teapot time"
    }
  }

  "getLockoutExpiry" - {
    "should return expiry time when a matching lockout exists" in new Test {
      val result: Future[Option[Instant]] = service.getLockoutExpiry()
      await(result) mustBe instantTime
    }

    "should return false when no lockout exists" in new Test {
      override lazy val getExpiryResult: Future[Option[Instant]] = Future.successful(None)
      val result: Future[Option[Instant]] = service.getLockoutExpiry()
      await(result) mustBe None
    }

    "should return an exception retrieval fails" in new Test {
      override lazy val getExpiryResult: Future[Option[Instant]] = Future.failed(new RuntimeException(""))

      val result: Future[Option[Instant]] = service.getLockoutExpiry()
      assertThrows[RuntimeException](
        await(result)
      )
    }
  }
}
