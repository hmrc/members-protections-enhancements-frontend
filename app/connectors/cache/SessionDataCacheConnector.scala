package connectors.cache

import uk.gov.hmrc.http.HttpReads.Implicits.{readFromJson, readOptionOfNotFound, readUnit}
import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import models.cache.SessionData
import play.api.Logger
import uk.gov.hmrc.http.{HeaderCarrier, StringContextOps}
import uk.gov.hmrc.http.client.HttpClientV2
import utils.FutureUtils.FutureOps

import scala.concurrent.{ExecutionContext, Future}
class SessionDataCacheConnectorImpl @Inject()(config : FrontendAppConfig, http: HttpClientV2)
  extends SessionDataCacheConnector {

  private def url(cacheId: String): String =
    s"${config.pensionsAdministrator}/pension-administrator/journey-cache/session-data/$cacheId"

  override def fetch(cacheId:  String)(implicit hc:  HeaderCarrier, ec:  ExecutionContext): Future[Option[SessionData]] =
    http.get(url"${url(cacheId)}")
      .execute[Option[SessionData]]
      .tapError(t => Future.successful(logger.error(s"Failed to fetch $cacheId with message ${t.getMessage}")))

  override def remove(cacheId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] =
    http.delete(url"${url(cacheId)}")
      .execute[Unit]
      .tapError(t => Future.successful(logger.error(s"Failed to delete $cacheId with message ${t.getMessage}")))
}

@ImplementedBy(classOf[SessionDataCacheConnectorImpl])
trait SessionDataCacheConnector {
  protected val logger: Logger = Logger(classOf[SessionDataCacheConnector])
  def fetch(cacheId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Option[SessionData]]
  def remove(cacheId: String)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit]
}
