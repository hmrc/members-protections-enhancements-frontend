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

package repositories

import config.FrontendAppConfig
import models.mongo.CacheUserDetails
import models.mongo.CacheUserDetails.mongoFormat
import models.requests.IdentifierRequest
import models.requests.IdentifierRequest.AdministratorRequest
import models.requests.UserType.PSA
import org.mockito.Mockito.when
import org.mongodb.scala.MongoWriteException
import org.mongodb.scala.model.Filters
import org.scalatest.OptionValues
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatestplus.mockito.MockitoSugar
import play.api.libs.json.Json
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.auth.core.AffinityGroup.Individual
import uk.gov.hmrc.mongo.TimestampSupport
import uk.gov.hmrc.mongo.cache.CacheItem
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.test.DefaultPlayMongoRepositorySupport

import java.time.Instant
import java.time.temporal.ChronoUnit
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class FailedAttemptLockoutRepositorySpec
  extends AnyFreeSpec
    with Matchers
    with ScalaFutures
    with IntegrationPatience
    with OptionValues
    with MockitoSugar
    with DefaultPlayMongoRepositorySupport[CacheItem] {

  val instant: Instant = Instant.now.truncatedTo(ChronoUnit.MILLIS)
  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  val mockTimestampSupport: TimestampSupport = mock[TimestampSupport]
  val timeSecs: Long = 1000000L
  val instantTime: Instant = Instant.ofEpochSecond(timeSecs)
  when(mockTimestampSupport.timestamp()).thenReturn(instantTime)

  val lockoutRepo: FailedAttemptLockoutRepositoryImpl = new FailedAttemptLockoutRepositoryImpl(
    mongoComponent = mongoComponent,
    frontendAppConfig = mockAppConfig,
    timestampSupport = mockTimestampSupport
  )

  override val repository: PlayMongoRepository[CacheItem] = lockoutRepo.cacheRepo

  implicit val request: IdentifierRequest[AnyContentAsEmpty.type] = AdministratorRequest(
    affGroup = Individual,
    userId = "userId",
    psaId = "psaId",
    psrUserType = PSA,
    request = FakeRequest()
  )

  val cacheUserDetails: CacheUserDetails = CacheUserDetails(
    psrUserType = PSA,
    psrUserId = Some("psaId"),
    createdAt = Some(Instant.ofEpochSecond(timeSecs))
  )

  "putCache" - {
    "must successfully add a new lockout" in {
      val result: Future[Unit] = lockoutRepo.putCache("psaId")(cacheUserDetails)
      await(result) mustBe()
      val findResult: Seq[CacheItem] = find(Filters.equal("_id", "psaId")).futureValue
      findResult must have length 1
      findResult.headOption.get.data mustBe Json.obj(
        "dataKey" -> Json.toJson(cacheUserDetails)(mongoFormat)
      )
    }

    "must not create duplicate entries or upsert" in {
      def result: Future[Unit] = lockoutRepo.putCache("psaId")(cacheUserDetails)
      await(result) mustBe()

      assertThrows[MongoWriteException](await(result))
    }
  }

  "getFromCache" - {
    "must successfully retrieve a matching lockout" in {
      val result: Future[Option[CacheUserDetails]] =
        lockoutRepo.putCache("psaId")(cacheUserDetails).flatMap(_ =>
          lockoutRepo.getFromCache("psaId")
        )

      await(result) mustBe Some(cacheUserDetails)
    }

    "must return None when no entries match" in {
      val result: Future[Option[CacheUserDetails]] =
        lockoutRepo.putCache("psaId")(cacheUserDetails).flatMap(_ =>
          lockoutRepo.getFromCache("notPsaId")
        )

      await(result) mustBe None
    }
  }

  "getLockoutExpiry" - {
    "must successfully retrieve a lockout expiry when one exists" in {
      val result: Future[Option[Instant]] =
        lockoutRepo.putCache("psaId")(cacheUserDetails).flatMap(_ =>
          lockoutRepo.getLockoutExpiry("psaId")
        )

      await(result) mustBe Some(instantTime)
    }

    "must return None when no entries match" in {
      val result: Future[Option[CacheUserDetails]] =
        lockoutRepo.putCache("psaId")(cacheUserDetails).flatMap(_ =>
          lockoutRepo.getFromCache("notPsaId")
        )

      await(result) mustBe None
    }
  }
}
