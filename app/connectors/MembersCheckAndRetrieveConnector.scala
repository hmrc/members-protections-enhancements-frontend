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

package connectors

import com.google.inject.Inject
import config.FrontendAppConfig
import models.errors.MpeError
import models.requests.PensionSchemeMemberRequest
import models.response.ProtectionRecordDetails
import play.api.http.Status.*
import play.api.libs.json.*
import play.api.libs.json.Format.GenericFormat
import play.api.libs.ws.writeableOf_JsValue
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.constants.HeaderKeys.CORRELATION_ID
import utils.HttpResponseHelper
import play.api.Logging

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

class MembersCheckAndRetrieveConnector @Inject() (httpClientV2: HttpClientV2, config: FrontendAppConfig)
    extends Logging
    with HttpResponseHelper {

  def checkAndRetrieve(pensionSchemeMemberRequest: PensionSchemeMemberRequest)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext,
    correlationId: String
  ): Future[Either[MpeError, ProtectionRecordDetails]] =
    httpClientV2
      .post(url"${config.checkAndRetrieveUrl}")
      .withBody(Json.toJson(pensionSchemeMemberRequest))
      .setHeader((CORRELATION_ID, correlationId))
      .execute[HttpResponse]
      .map { response =>
        response.status match {
          case OK =>
            Right(handleResponse[ProtectionRecordDetails](response.json))
          case _ =>
            logger.warn(
              s"[checkAndRetrieve] Error response received" +
                s" with status: ${response.status}, and correlationId: ${response.header("correlationId")} " +
                s" due to ${response.body}"
            )
            Left(handleResponse[MpeError](response.json))
        }
      }
      .andThen { case Failure(t: Throwable) =>
        logger.warn("Unable to retrieve the data", t)
      }
}
