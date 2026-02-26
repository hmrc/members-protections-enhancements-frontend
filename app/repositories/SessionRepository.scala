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
import models.userAnswers.{EncryptedUserAnswers, UserAnswers}
import org.mongodb.scala.bson.conversions.Bson
import org.mongodb.scala.model.*
import uk.gov.hmrc.mdc.Mdc
import uk.gov.hmrc.mongo.MongoComponent
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import utils.encryption.AesGcmAdCrypto

import java.time.{Clock, Instant}
import java.util.concurrent.TimeUnit
import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionRepository @Inject() (mongoComponent: MongoComponent, appConfig: FrontendAppConfig, clock: Clock)(implicit
  ec: ExecutionContext,
  aesGcmAdCrypto: AesGcmAdCrypto
) extends PlayMongoRepository[EncryptedUserAnswers](
      collectionName = "user-answers",
      mongoComponent = mongoComponent,
      domainFormat = EncryptedUserAnswers.format,
      indexes = Seq(
        IndexModel(
          keys = Indexes.ascending("lastUpdated"),
          indexOptions = IndexOptions()
            .name("lastUpdatedIdx")
            .expireAfter(appConfig.sessionDataTtl, TimeUnit.SECONDS)
        )
      )
    ) {

  private def byId(id: String): Bson = Filters.equal("_id", id)

  def keepAlive(id: String): Future[Boolean] = Mdc.preservingMdc {
    collection
      .updateOne(
        filter = byId(id),
        update = Updates.set("lastUpdated", Instant.now(clock))
      )
      .toFuture()
      .map(_ => true)
  }

  def get(id: String): Future[Option[UserAnswers]] = Mdc.preservingMdc {
    implicit val associatedText: String = id

    keepAlive(id).flatMap { _ =>
      collection
        .find[EncryptedUserAnswers](byId(id))
        .headOption()
        .map(_.map(_.decrypt))
    }
  }

  def set(answers: UserAnswers): Future[Unit] = Mdc.preservingMdc {
    val updatedAnswers = answers.copy(lastUpdated = Instant.now(clock))
    implicit val associatedText: String = updatedAnswers.id

    collection
      .replaceOne(
        filter = byId(updatedAnswers.id),
        replacement = updatedAnswers.encrypt,
        options = ReplaceOptions().upsert(true)
      )
      .toFuture()
      .map(_ => ())
  }

  def clear(id: String): Future[Boolean] = Mdc.preservingMdc {
    collection
      .deleteOne(byId(id))
      .toFuture()
      .map(_ => true)
  }
}
