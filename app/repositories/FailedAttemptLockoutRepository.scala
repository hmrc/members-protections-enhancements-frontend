package repositories

import com.google.inject.{Inject, Singleton}
import com.mongodb.DuplicateKeyException
import config.FrontendAppConfig
import models.mongo.CacheUserDetails
import org.mongodb.scala.{MongoException, SingleObservable}
import play.api.libs.json.{Format, Json}
import uk.gov.hmrc.mongo.cache.{CacheIdType, CacheItem, EntityCache, MongoCacheRepository}
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}

import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FailedAttemptLockoutRepository @Inject()(mongoComponent: MongoComponent,
                                               frontendAppConfig: FrontendAppConfig,
                                               timestampSupport: TimestampSupport)
                                              (implicit ec: ExecutionContext)
  extends EntityCache[String, CacheUserDetails] {
    override val cacheRepo: MongoCacheRepository[String] = new MongoCacheRepository[String](
      mongoComponent = mongoComponent,
      collectionName = "failed-attempt-lockout",
      ttl = Duration(frontendAppConfig.lockoutTtl, TimeUnit.SECONDS),
      timestampSupport = timestampSupport,
      cacheIdType = CacheIdType.SimpleCacheId
    )

  override val format: Format[CacheUserDetails] = CacheUserDetails.mongoFormat

  //TODO: Handle conflict error
  override def putCache(cacheId: String)(data: CacheUserDetails)
                       (implicit ec: ExecutionContext): Future[Unit] = {
    val id = CacheIdType.SimpleCacheId.run(cacheId)
    val timestamp = timestampSupport.timestamp()

    cacheRepo.collection
      .insertOne(
        CacheItem(id, Json.toJsObject(data), timestamp, timestamp)
      )
      .toFuture()
      .map {
        case res if res.wasAcknowledged() => ()
        case _ => ??? //TODO Throw a fatal exception
      }
      .recover {
        case _: DuplicateKeyException => ???
        case _: MongoException => ???
      }

  }
}
