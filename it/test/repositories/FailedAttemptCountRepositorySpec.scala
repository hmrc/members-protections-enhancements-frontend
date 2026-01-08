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

package repositories

import config.FrontendAppConfig
import models.mongo.CacheUserDetails
import models.requests.UserDetails
import models.requests.UserType.PSA
import org.mockito.Mockito.when
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.auth.core.AffinityGroup
import uk.gov.hmrc.mongo.TimestampSupport
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FailedAttemptCountRepositorySpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar
    with DefaultPlayMongoRepositorySupport[CacheUserDetails] {

  val instant: Instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  val mockTimestampSupport: TimestampSupport = mock[TimestampSupport]
  val timeSecs: Long = 1000000L
  when(mockTimestampSupport.timestamp()).thenReturn(Instant.ofEpochSecond(timeSecs))

  override val repository: FailedAttemptCountRepositoryImpl = new FailedAttemptCountRepositoryImpl(
    mongoComponent = mongoComponent,
    frontendAppConfig = mockAppConfig,
    timestampSupport = mockTimestampSupport
  )

  implicit val userDetails: UserDetails  = UserDetails(PSA, "psaId", "anotherId", AffinityGroup.Individual)

  "addFailedAttempt" - {
    "must successfully add a new failed attempt" in {
      val result: Future[Unit] = repository.addFailedAttempt()
      await(result) mustBe()
      val findResult: Seq[CacheUserDetails] = find(Filters.equal("psrUserId", "psaId")).futureValue
      findResult must have length 1
      findResult.headOption.get mustBe CacheUserDetails(PSA, Some("psaId"), Some(Instant.ofEpochSecond(timeSecs)))
    }
  }

  "countFailedAttempts" - {
    "must count any matching entries that exist" in {
      val result: Future[Long] = {
        repository.addFailedAttempt().flatMap(_ =>
          repository.addFailedAttempt().flatMap(_ =>
            repository.countFailedAttempts()
          )
        )
      }

      await(result) mustBe 2
    }

    "must return zero when no entries exist" in {
      val result: Future[Long] = repository.countFailedAttempts()
      await(result) mustBe 0
    }
  }
}
