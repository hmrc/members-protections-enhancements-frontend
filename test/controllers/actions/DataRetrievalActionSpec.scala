/*
 * Copyright 2024 HM Revenue & Customs
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
import models.requests.IdentifierRequest.AdministratorRequest
import models.requests.{DataRequest, IdentifierRequest}
import models.{MemberDetails, SessionData, UserAnswers}
import org.mockito.ArgumentCaptor
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito._
import org.scalatestplus.mockito.MockitoSugar
import pages.WhatIsTheMembersNamePage
import play.api.test.FakeRequest
import repositories.SessionRepository

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DataRetrievalActionSpec extends SpecBase with MockitoSugar {

  private val dummyValue               = MemberDetails("fname", "lname")
  private val userAnswers: UserAnswers = UserAnswers().setOrException(WhatIsTheMembersNamePage, dummyValue)

  class Harness(sessionRepository: SessionRepository) extends DataRetrievalActionImpl(sessionRepository) {
    def callTransform[A](request: IdentifierRequest[A]): Future[DataRequest[A]] = transform(request)
  }

  "DataRetrievalAction.transform" - {

    "when there is no data in the cache" - {

      "must set userAnswers to 'None' in the request" in {

        val sessionRepository                              = mock[SessionRepository]
        when(sessionRepository.get(any)) thenReturn Future(None)
        when(sessionRepository.set(any())) thenReturn Future(true)
        val action                                         = new Harness(sessionRepository)
        val sessionDataCaptor: ArgumentCaptor[SessionData] = ArgumentCaptor.forClass(classOf[SessionData])
        val result                                         = action.callTransform(AdministratorRequest.apply("id", FakeRequest(), "A2100001")).futureValue

        result.userAnswers.get(WhatIsTheMembersNamePage) mustBe None
        verify(sessionRepository, times(1)).set(sessionDataCaptor.capture())
      }
    }

    "when there is data in the cache" - {

      "must build a userAnswers object and add it to the request" in {

        val sessionRepository = mock[SessionRepository]
        when(sessionRepository.get(any())) thenReturn Future(Some(SessionData(userAnswers, id = "id")))
        when(sessionRepository.set(any())) thenReturn Future(true)
        val action                                         = new Harness(sessionRepository)
        val sessionDataCaptor: ArgumentCaptor[SessionData] = ArgumentCaptor.forClass(classOf[SessionData])
        val result                                         = action.callTransform(AdministratorRequest.apply("id", FakeRequest(), "A2100001")).futureValue

        result.userAnswers.get(WhatIsTheMembersNamePage) mustBe defined
        result.userAnswers.get(WhatIsTheMembersNamePage) mustBe Some(dummyValue)
      }
    }
  }
}
