/*
 * Copyright 2021 HM Revenue & Customs
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
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{times, verify, when}
import org.scalatestplus.mockito.MockitoSugar
import play.api.inject
import play.api.inject.bind
import play.api.test.FakeRequest
import play.api.test.Helpers._
import repositories.SessionRepository
import utils.IdGenerator

import scala.concurrent.Future

class KeepAliveControllerSpec extends SpecBase with MockitoSugar {

  "keepAlive" - {

    "when the user has answered some questions" - {

      "must keep the answers alive and return OK" - {
        "when data request has no correlation id" in {

          val mockSessionRepository = mock[SessionRepository]
          when(mockSessionRepository.keepAlive(any())) thenReturn Future.successful(true)

          val mockIdGenerator = mock[IdGenerator]
          val application = applicationBuilder(userAnswers = emptyUserAnswers)
            .overrides(
              inject.bind(classOf[IdGenerator]).to(mockIdGenerator),
              bind[SessionRepository].toInstance(mockSessionRepository)
            ).build()

        running(application) {

          val request = FakeRequest(GET, routes.KeepAliveController.keepAlive().url)

          val result = route(application, request).value

            status(result) mustEqual OK
            verify(mockSessionRepository, times(1)).keepAlive(emptyUserAnswers.id)
            verify(mockIdGenerator, times(1)).getCorrelationId
          }
        }
        "when data request has correlation id, no need to generate new" in {

          val mockSessionRepository = mock[SessionRepository]
          when(mockSessionRepository.keepAlive(any())) thenReturn Future.successful(true)

          val mockIdGenerator = mock[IdGenerator]
          val application = applicationBuilder(userAnswers = emptyUserAnswers, correlationId = Some("X-123"))
            .overrides(
              inject.bind(classOf[IdGenerator]).to(mockIdGenerator),
              bind[SessionRepository].toInstance(mockSessionRepository)
            ).build()

          running(application) {

            val request = FakeRequest(GET, routes.KeepAliveController.keepAlive().url)

            val result = route(application, request).value

            status(result) mustEqual OK
            verify(mockSessionRepository, times(1)).keepAlive(emptyUserAnswers.id)
            verify(mockIdGenerator, times(0)).getCorrelationId
          }
        }
      }
    }
  }
}
