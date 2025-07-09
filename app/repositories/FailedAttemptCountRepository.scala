package repositories

import com.google.inject.{Inject, Singleton}
import com.mongodb.DuplicateKeyException
import config.FrontendAppConfig
import models.UserAnswers
import models.mongo.CacheUserDetails
import models.requests.IdentifierRequest
import org.mongodb.scala.MongoException
import org.mongodb.scala.model.{Filters, IndexModel, IndexOptions, Indexes}
import org.mongodb.scala.result.InsertOneResult
import play.api.libs.json.Format
import uk.gov.hmrc.mongo.cache.{CacheIdType, CacheItem, EntityCache, MongoCacheRepository}
import uk.gov.hmrc.mongo.play.json.PlayMongoRepository
import uk.gov.hmrc.mongo.{MongoComponent, TimestampSupport}

import java.util.concurrent.TimeUnit
import scala.concurrent.{ExecutionContext, Future}
import scala.concurrent.duration.Duration

@Singleton
class FailedAttemptCountRepository @Inject()(mongoComponent: MongoComponent,
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
    )
  ) {

  def addFailedAttempt()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Unit] =
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
        case res if res.wasAcknowledged() => ()
        case _ => ??? //TODO Throw a fatal exception
      }
      .recover {
        case _: MongoException => ???
      }

  def countFailedAttempts()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Long] = {
    collection
      .countDocuments(
        filter = Filters.equal(fieldName = "internalId", value = request.userDetails.userId)
      )
      .toFuture()
      .recover {
        case _: MongoException => ???
      }
  }
}

