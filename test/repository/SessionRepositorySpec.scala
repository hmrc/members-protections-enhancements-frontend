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

package repository

import config.FrontendAppConfig
import models.userAnswers.{EncryptedUserAnswers, UserAnswers}
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalatest.{BeforeAndAfterEach, OptionValues}
import org.scalatest.concurrent.ScalaFutures
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers.mustEqual
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import repositories.SessionRepository
import uk.gov.hmrc.mongo.test.PlayMongoRepositorySupport
import utils.encryption.MockAesGcmAdCrypto

import java.time.temporal.ChronoUnit
import java.time.{Clock, Instant, ZoneId}
import scala.concurrent.ExecutionContext.Implicits.global

class SessionRepositorySpec extends AnyFreeSpec
  with Matchers
  with PlayMongoRepositorySupport[EncryptedUserAnswers]
  with BeforeAndAfterEach
  with ScalaFutures
  with MockitoSugar
  with OptionValues
  with MockAesGcmAdCrypto {

  private val instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  private val stubClock: Clock = Clock.fixed(instant, ZoneId.systemDefault)

  private val mockAppConfig = mock[FrontendAppConfig]
  when(mockAppConfig.sessionDataTtl) thenReturn 1

  protected override val repository = new SessionRepository(
    mongoComponent = mongoComponent,
    appConfig = mockAppConfig,
    clock = stubClock
  )

  override def beforeEach(): Unit = {
    super.beforeEach()
    deleteAll().futureValue
    org.mockito.Mockito.reset()
  }

  private val userAnswers = UserAnswers("id", Json.obj("foo" -> "bar"), Instant.ofEpochSecond(1))

  ".set" - {
    "must set the last updated time on the supplied user answers to `now`, and save them" in {
      val expectedResult = userAnswers.copy(lastUpdated = instant).encrypt

      val setResult: Unit = repository.set(userAnswers).futureValue
      val updatedRecord = find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value

      setResult mustEqual ()
      updatedRecord mustEqual expectedResult
    }
  }

  ".get" - {
    "when there is a record for this id" - {
      "must update the lastUpdated time and get the record" in {
        insert(userAnswers.encrypt).futureValue

        val result = repository.get(userAnswers.id).futureValue
        val expectedResult = userAnswers copy (lastUpdated = instant)

        result.value mustEqual expectedResult
      }
    }

    "when there is no record for this id" - {
      "must return None" in {
        repository.get("id that does not exist").futureValue must not be defined
      }
    }
  }

  ".clear" - {
    "must remove a record" in {
      insert(userAnswers.encrypt).futureValue

      val result = repository.clear(userAnswers.id).futureValue

      result mustEqual true
      repository.get(userAnswers.id).futureValue must not be defined
    }

    "must return true when there is no record to remove" in {
      val result = repository.clear("id that does not exist").futureValue

      result mustEqual true
    }
  }

  ".keepAlive" - {
    "when there is a record for this id" - {
      "must update its lastUpdated to `now` and return true" in {
        insert(userAnswers.encrypt).futureValue

        val result = repository.keepAlive(userAnswers.id).futureValue
        val expectedUpdatedAnswers = userAnswers.copy(lastUpdated = instant).encrypt

        result mustEqual true
        val updatedAnswers = find(Filters.equal("_id", userAnswers.id)).futureValue.headOption.value
        updatedAnswers mustEqual expectedUpdatedAnswers
      }
    }

    "when there is no record for this id" - {
      "must return true" in {
        repository.keepAlive("id that does not exist").futureValue mustEqual true
      }
    }
  }
}
