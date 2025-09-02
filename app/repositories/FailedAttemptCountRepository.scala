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
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}
import utils.NewLogging

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
        Indexes.ascending("psrUserId"),
        IndexOptions()
          .name("psrUserIdIndex")
      )
    ),
    replaceIndexes = true
  ) with FailedAttemptCountRepository with NewLogging {

  def addFailedAttempt()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Unit] = {
    val methodLoggingContext: String = "addFailedAttempt"

    val infoLogger: String => Unit = infoLog(
      secondaryContext = methodLoggingContext,
      dataLog = correlationIdLogString(request.correlationId)
    )

    val warnLogger: (String, Option[Throwable]) => Unit = warnLog(
      secondaryContext = methodLoggingContext,
      dataLog = correlationIdLogString(request.correlationId)
    )

    infoLogger("Attempting to add user failed attempt to cache")

    collection
      .insertOne(
        document = CacheUserDetails(
          userDetails = request.userDetails,
          withPsrUserId = true,
          createdAt = Some(timestampSupport.timestamp())
        )
      )
      .toFuture()
      .map {
        case res if res.wasAcknowledged() =>
          infoLogger("Successfully cached user failed attempt")
        case _ =>
          val error: CacheException = new CacheException("Result was not acknowledged")
          warnLogger("Failed attempt was not added successfully to cache", Some(error))
          throw error
      }
      .recover {
        case ex: MongoException =>
          warnLogger("MongoDB returned an error while attempting to cache user failed attempt", Some(ex))
          throw ex
      }
  }

  def countFailedAttempts()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Long] = {
    val methodLoggingContext: String = "countFailedAttempts"

    val infoLogger: String => Unit = infoLog(
      secondaryContext = methodLoggingContext,
      dataLog = correlationIdLogString(request.correlationId)
    )

    val warnLogger: (String, Option[Throwable]) => Unit = warnLog(
      secondaryContext = methodLoggingContext,
      dataLog = correlationIdLogString(request.correlationId)
    )

    infoLogger("Attempting to retrieve user failed attempt count")

    collection
      .countDocuments(
        filter = Filters.equal(fieldName = "psrUserId", value = request.userDetails.psrUserId)
      )
      .toFuture()
      .map(res => {
        infoLogger(s"Successfully retrieved user failed attempt count of: $res")
        res
      })
      .recover {
        case ex: MongoException =>
          warnLogger(s"A MongoDB error occurred while attempting to retrieve user failed attempt count", Some(ex))
          throw ex
      }
  }
}

