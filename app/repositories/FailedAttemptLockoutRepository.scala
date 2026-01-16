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

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import models.mongo.CacheUserDetails
import org.mongodb.scala.{MongoException, MongoWriteException}
import play.api.Logging
import play.api.libs.json.{Format, Json, Writes}
import uk.gov.hmrc.mongo.cache._
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}

import java.time.Instant
import java.util.concurrent.TimeUnit
import javax.cache.CacheException
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[FailedAttemptLockoutRepositoryImpl])
trait FailedAttemptLockoutRepository {
  def putCache(cacheId: String)(data: CacheUserDetails)(implicit ec: ExecutionContext): Future[Unit]
  def getFromCache(cacheId: String): Future[Option[CacheUserDetails]]
  def getLockoutExpiry(cacheId: String): Future[Option[Instant]]
}

@Singleton
class FailedAttemptLockoutRepositoryImpl @Inject()(mongoComponent: MongoComponent,
                                               frontendAppConfig: FrontendAppConfig,
                                               timestampSupport: TimestampSupport)
                                              (implicit ec: ExecutionContext)
  extends EntityCacheWorkaround[String, CacheUserDetails] with FailedAttemptLockoutRepository with Logging {

  val classLoggingContext: String = "FailedAttemptLockoutRepository"
  
  val format: Format[CacheUserDetails] = CacheUserDetails.mongoFormat

  val cacheRepo: MongoCacheRepository[String] = new MongoCacheRepository[String](
      mongoComponent = mongoComponent,
      collectionName = "failed-attempt-lockout",
      ttl = Duration(frontendAppConfig.lockoutTtl, TimeUnit.SECONDS),
      timestampSupport = timestampSupport,
      cacheIdType = CacheIdType.SimpleCacheId
    ) {
    /*
    We override the `put` method from MongoCacheRepository as the base implementation uses an upsert.
    In this use case we would rather throw an error if there is an attempt to create a lockout when one
    already exists as this would suggest that our lockout mechanism has been subverted in some way.    
     */
    override def put[A: Writes](cacheId: String)(dataKey: DataKey[A], data: A): Future[CacheItem] = {
      val methodLoggingContext: String = "put"
      val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

      val id = CacheIdType.SimpleCacheId.run(cacheId)
      val timestamp = timestampSupport.timestamp()

      val cacheItem: CacheItem = CacheItem(
        id = id,
        data = Json.obj(dataKey.unwrap -> Json.toJson(data)),
        createdAt = timestamp,
        modifiedAt = timestamp
      )

      logger.info(s"$fullLoggingContext - Received request to create lockout for user")

      cacheRepo.collection
        .insertOne(cacheItem)
        .toFuture()
        .map {
          case res if res.wasAcknowledged() =>
            logger.info(s"$fullLoggingContext - Successfully created lockout for user")
            cacheItem
          case _ =>
            logger.warn(s"$fullLoggingContext - Lockout was not added successfully to cache")
            throw new CacheException("Failed to add user lockout to cache")
        }
        .recover {
          case ex: MongoWriteException if ex.getMessage.contains("E11000 duplicate key error collection") =>
            logger.warn(s"$fullLoggingContext - Lockout entry already exists for user")
            throw ex
          case ex: MongoException =>
            logger.warn(s"$fullLoggingContext - " +
              s"MongoDB returned an error during lockout creation with error message: ${ex.getMessage}"
            )
            throw ex
        }
    }
  }

  override def getLockoutExpiry(cacheId: String): Future[Option[Instant]] = {
    val methodLoggingContext: String = "getLockoutExpiry"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$fullLoggingContext - Received request to retrieve lockout expiry for user")

    cacheRepo
      .findById(cacheId)
      .map {
        case value@Some(_) =>
          logger.info(s"$fullLoggingContext - Lockout expiry successfully retrieved for the supplied details")
          value.map(_.createdAt)
        case None =>
          logger.info(s"$fullLoggingContext - No lockout found for the supplied details")
          None
      }
  }
}
