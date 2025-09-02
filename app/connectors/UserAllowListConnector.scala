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

package connectors

import config.FrontendAppConfig
import connectors.UserAllowListConnector.UnexpectedResponseException
import models.CorrelationId
import models.allowlist.CheckRequest
import play.api.http.Status.{NOT_FOUND, OK}
import play.api.libs.json.Json
import uk.gov.hmrc.http.HttpReads.Implicits.readRaw
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.NewLogging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NoStackTrace

@Singleton
class UserAllowListConnector @Inject()(config: FrontendAppConfig, httpClient: HttpClientV2)
                                      (implicit ec: ExecutionContext) extends NewLogging {

  def check(feature: String, value: String)
           (implicit hc: HeaderCarrier, correlationId: CorrelationId): Future[Boolean] = {

    val methodLoggingContext: String = "check"
    val logString: String = correlationIdLogString(correlationId)
    val infoLogger: String => Unit = infoLog(methodLoggingContext, logString)
    val warnLogger: (String, Option[Throwable]) => Unit = warnLog(methodLoggingContext, logString)

    infoLogger("Attempting to check if user is present on the service allowlist")

    httpClient.post(url"${config.userAllowListService.baseUrl}/user-allow-list/${config.appName}/$feature/check")
      .setHeader("Authorization" -> config.internalAuthToken)
      .withBody(Json.toJson(CheckRequest(value)))
      .execute[HttpResponse]
      .flatMap { response =>
        response.status match {
          case OK        =>
            infoLogger("User's details were found on the allowlist. Allowing access to the service")
            Future.successful(true)
          case NOT_FOUND =>
            warnLogger("Could not find user's details on the allowlist. Blocking access to the service", None)
            Future.successful(false)
          case status    =>
            val err: UnexpectedResponseException = UnexpectedResponseException(status)
            logger.errorWithException(
              methodLoggingContext,
              "An unexpected error occurred while attempting to check the allowlist",
              err,
              logString
            )
            Future.failed(err)
        }
      }
  }
}

object UserAllowListConnector {

  final case class UnexpectedResponseException(status: Int) extends Exception with NoStackTrace {
    override def getMessage: String = s"Unexpected status: $status"
  }
}