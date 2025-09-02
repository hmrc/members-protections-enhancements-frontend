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
import models.CorrelationId
import models.requests.IdentifierRequest.AdministratorRequest
import models.requests.UserType.PSA
import models.requests.{DataRequest, IdentifierRequest, RequestWithCorrelationId}
import models.userAnswers.UserAnswers
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.JsObject
import play.api.test.FakeRequest
import repositories.SessionRepository
import uk.gov.hmrc.auth.core.AffinityGroup

import java.time.Instant
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar {

  class Harness(sessionRepository: SessionRepository) extends DataRetrievalActionImpl(sessionRepository) {
    def callTransform[A](request: IdentifierRequest[A]): Future[DataRequest[A]] = transform(request)
  }

  "Data Retrieval Action" - {
    val userAnswers = UserAnswers("id", lastUpdated = Instant.now())
    implicit val correlationId: CorrelationId = "X-ID"

    "when there is no data in the cache" - {
      "must set userAnswers to 'None' in the request" in {
        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get(any())) thenReturn Future(None)
        val action = new Harness(sessionRepository)

        val result = action.callTransform(AdministratorRequest.apply(
          affGroup = AffinityGroup.Individual,
          userId = "id",
          psaId = "A2100001",
          psrUserType = PSA,
          request = RequestWithCorrelationId(FakeRequest(), correlationId)
        )).futureValue

        result.userAnswers.data mustBe JsObject.empty
        result.userAnswers.id mustBe "id"
      }
    }

    "when there is data in the cache" - {
      "must build a userAnswers object and add it to the request" in {
        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get(any())) thenReturn Future(Some(userAnswers))
        val action = new Harness(sessionRepository)

        val result = action.callTransform(AdministratorRequest.apply(
          affGroup = AffinityGroup.Individual,
          userId = "id",
          psaId = "A2100001",
          psrUserType = PSA,
          request = RequestWithCorrelationId(FakeRequest(), correlationId)
        )).futureValue

        result.userAnswers mustBe userAnswers
      }
    }
  }
}
