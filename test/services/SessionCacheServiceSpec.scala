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

package services

import base.SpecBase
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.{reset, times, verify}
import org.scalatest.BeforeAndAfterEach
import repositories._

class SessionCacheServiceSpec extends SpecBase with BeforeAndAfterEach{

  val mockSessionRepository: SessionRepository = mock[SessionRepository]

  override def beforeEach(): Unit = {
    reset(
      mockSessionRepository
    )
  }

  val service = new SessionCacheService(
    mockSessionRepository)

  val serviceEnc = new SessionCacheService(
    mockSessionRepository)

  "SessionCacheService" - {

    "invalidateCache (non encrypted)" - {

      "delete from the cache" in {
        service.clear("id")
        verify(mockSessionRepository, times(1)).clear(any())
      }

    }

    "invalidateCache (encrypted)" - {

      "delete from the cache" in {
        serviceEnc.clear("id")
        verify(mockSessionRepository, times(1)).clear(any())
      }

    }
  }
}