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
import models.UserAnswers
import org.mockito.ArgumentMatchers.{any, eq => meq}
import org.mockito.Mockito.{reset, times, verify, when}
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class SessionCacheServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockSessionRepository: SessionRepository = mock[SessionRepository]
  val service = new SessionCacheServiceImpl(mockSessionRepository)

  override def beforeEach(): Unit = {
    super.beforeEach()
    reset(mockSessionRepository)
  }

  "save" - {
    "save user answers to sessionRepository" in {

      when(mockSessionRepository.set(any())).thenReturn(Future.successful(()))

      val userAnswers = UserAnswers("test-id")
      service.save(userAnswers).futureValue
      verify(mockSessionRepository, times(1)).set(meq(userAnswers))
    }
  }

}
