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
import models.CorrelationId
import models.mongo.CacheUserDetails
import org.bson.codecs.Codec
import org.mongodb.scala.model.IndexModel
import org.mongodb.scala.{MongoException, MongoWriteException}
import play.api.Logging
import play.api.libs.json.{Format, Json, Writes}
import uk.gov.hmrc.mongo.cache._
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}
import utils.NewLogging

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.cache.CacheException
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[FailedAttemptLockoutRepositoryImpl])
trait FailedAttemptLockoutRepository {
  def putCache(cacheId: String)(data: CacheUserDetails)(implicit ec: ExecutionContext): Future[Unit]
  def getFromCache(cacheId: String): Future[Option[CacheUserDetails]]
  def getLockoutExpiry(cacheId: String)(implicit correlationId: CorrelationId): Future[Option[Instant]]
}

@Singleton
class FailedAttemptLockoutRepositoryImpl @Inject()(mongoComponent: MongoComponent,
                                                   frontendAppConfig: FrontendAppConfig,
                                                   timestampSupport: TimestampSupport)
                                                  (implicit ec: ExecutionContext)
  extends EntityCache[String, CacheUserDetails] with FailedAttemptLockoutRepository with NewLogging {

  lazy val format: Format[CacheUserDetails] = CacheUserDetails.mongoFormat

  lazy val cacheRepo: MongoCacheRepository[String] = new MongoCacheRepository[String](
      mongoComponent = mongoComponent,
      collectionName = "failed-attempt-lockout",
      ttl = Duration(frontendAppConfig.lockoutTtl, TimeUnit.SECONDS),
      timestampSupport = timestampSupport,
      cacheIdType = CacheIdType.SimpleCacheId
    ) {
    override def put[A: Writes](cacheId: String)(dataKey: DataKey[A], data: A): Future[CacheItem] = {
      val methodLoggingContext: String = "put"

      val infoLogger: String => Unit = infoLog(secondaryContext = methodLoggingContext)
      val warnLogger: (String, Option[Throwable]) => Unit = warnLog(secondaryContext = methodLoggingContext)

      val id = CacheIdType.SimpleCacheId.run(cacheId)
      val timestamp = timestampSupport.timestamp()

      val cacheItem: CacheItem = CacheItem(
        id = id,
        data = Json.obj(dataKey.unwrap -> Json.toJson(data)),
        createdAt = timestamp,
        modifiedAt = timestamp
      )

      infoLogger("Received request to create lockout for user")

      cacheRepo.collection
        .insertOne(cacheItem)
        .toFuture()
        .map {
          case res if res.wasAcknowledged() =>
            infoLogger(s"Successfully created lockout for user")
            cacheItem
          case _ =>
            val ex: CacheException = new CacheException("Failed to add user lockout to cache")
            warnLogger("Lockout was not added successfully to cache", Some(ex))
            throw ex
        }
        .recover {
          case ex: MongoWriteException if ex.getMessage.contains("E11000 duplicate key error collection") =>
            warnLogger(
              "Lockout entry already exists for user",
              Some(ex)
            )
            throw ex
          case ex: MongoException =>
            warnLogger(
              s"MongoDB returned an error during lockout creation with error message: ${ex.getMessage}",
              Some(ex)
            )
            throw ex
        }
    }
  }

  override def getLockoutExpiry(cacheId: String)
                               (implicit correlationId: CorrelationId): Future[Option[Instant]] = {
    val methodLoggingContext: String = "getLockoutExpiry"

    val infoLogger: String => Unit = infoLog(
      secondaryContext = methodLoggingContext,
      dataLog = correlationIdLogString(correlationId)
    )

    infoLogger(s"Received request to retrieve lockout expiry for user")

    cacheRepo
      .findById(cacheId)
      .map {
        case value@Some(_) =>
          infoLogger(s"Lockout expiry successfully retrieved for the supplied details")
          value.map(_.createdAt)
        case None =>
          infoLogger(s"No lockout found for the supplied details")
          None
      }
  }
}
