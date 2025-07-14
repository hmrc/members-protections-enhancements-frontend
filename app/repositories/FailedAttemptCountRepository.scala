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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import models.mongo.CacheUserDetails
import models.requests.IdentifierRequest
import org.mongodb.scala.MongoException
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes}
import play.api.Logging
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}

import java.util.concurrent.TimeUnit
import javax.cache.CacheException
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[FailedAttemptCountRepositoryImpl])
trait FailedAttemptCountRepository {
  def addFailedAttempt()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Unit]
  def countFailedAttempts()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Long]
}

@Singleton
class FailedAttemptCountRepositoryImpl @Inject()(mongoComponent: MongoComponent,
                                                 frontendAppConfig: FrontendAppConfig,
                                                 timestampSupport: TimestampSupport)
                                                (implicit ec: ExecutionContext)
  extends PlayMongoRepository[CacheUserDetails](
    collectionName = "failed-attempt-count",
    mongoComponent = mongoComponent,
    domainFormat = CacheUserDetails.mongoFormat,
    indexes = Seq(
      IndexModel(
        Indexes.ascending("createdAt"),
        IndexOptions()
          .name("createdAtIndex")
          .expireAfter(frontendAppConfig.failedAttemptTtl, TimeUnit.SECONDS)
      ),
      IndexModel(
        Indexes.ascending("internalId"),
        IndexOptions()
          .name("internalIdIndex")
      )
    ),
    replaceIndexes = true
  ) with FailedAttemptCountRepository with Logging {

  val classLoggingContext: String = "FailedAttemptCountRepository"

  def addFailedAttempt()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Unit] = {
    val methodLoggingContext: String = "addFailedAttempt"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$fullLoggingContext - Received request to add failed attempt for user")

    collection
      .insertOne(
        document = CacheUserDetails(
          userDetails = request.userDetails,
          withInternalId = true,
          createdAt = Some(timestampSupport.timestamp())
        )
      )
      .toFuture()
      .map {
        case res if res.wasAcknowledged() =>
          logger.info(s"$fullLoggingContext - Successfully created failed attempt for user")
        case _ =>
          logger.warn(s"$fullLoggingContext - Failed attempt was not added successfully to MongoDB")
          throw new CacheException("Failed to add user failed attempt to MongoDB")
      }
      .recover {
        case ex: MongoException =>
          logger.warn(s"$fullLoggingContext - " +
            s"MongoDB returned an error during failed attempt creation with message: ${ex.getMessage}"
          )
          throw ex
      }
  }

  def countFailedAttempts()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Long] = {
    val methodLoggingContext: String = "countFailedAttempts"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$fullLoggingContext - Received request to count failed attempts for user")

    collection
      .countDocuments(
        filter = Filters.equal(fieldName = "internalId", value = request.userDetails.userId)
      )
      .toFuture()
      .map(res => {
        logger.info(s"Successfully retrieved failed attempt count for user of: $res")
        res
      })
      .recover {
        case ex: MongoException =>
          logger.warn(s"$fullLoggingContext - " +
            s"MongoDB returned an error during failed attempt count with message: ${ex.getMessage}"
          )
          throw ex
      }
  }
}

