/*
 * Copyright 2024 HM Revenue & Customs
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

import com.google.inject.{ImplementedBy, Inject}
import config.FrontendAppConfig
import models.{CorrelationId, ResultType}
import models.errors.{ErrorWrapper, MpeError}
import models.requests.PensionSchemeMemberRequest
import models.response.{ProtectionRecordDetails, ResponseWrapper}
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.{HttpResponseHelper, Logging, NewLogging}

import scala.concurrent.ExecutionContext
import scala.util.Failure

@ImplementedBy(classOf[MembersCheckAndRetrieveConnectorImpl])
trait MembersCheckAndRetrieveConnector extends Logging {

  def checkAndRetrieve(pensionSchemeMemberRequest: PensionSchemeMemberRequest)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: CorrelationId): ResultType
}

class MembersCheckAndRetrieveConnectorImpl @Inject()(httpClientV2: HttpClientV2, config: FrontendAppConfig)
  extends MembersCheckAndRetrieveConnector with HttpResponseHelper with NewLogging {

  private def retrieveCorrelationId(response: HttpResponse): CorrelationId =
    CorrelationId(response.header("correlationId").getOrElse("No correlationId"))

  override def checkAndRetrieve(pensionSchemeMemberRequest: PensionSchemeMemberRequest)
                               (implicit hc: HeaderCarrier,
                                ec: ExecutionContext,
                                correlationId: CorrelationId): ResultType = {

    val checkAndRetrieveUrl = url"${config.checkAndRetrieveUrl}"
    val methodLoggingContext: String = "checkAndRetrieve"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"
    logInfo(fullLoggingContext, s"with correlationId: $correlationId")

    httpClientV2.post(checkAndRetrieveUrl)
      .withBody(Json.toJson(pensionSchemeMemberRequest))
      .setHeader(
        ("correlationId", correlationId.value))
      .execute[HttpResponse].map { response =>
        val responseCorrelationId: CorrelationId = retrieveCorrelationId(response)
        val resultCorrelationId: CorrelationId = checkIdsMatch(correlationId, responseCorrelationId)
         val result: Either[ErrorWrapper, ResponseWrapper[ProtectionRecordDetails]] = response.status match {
            case OK =>
              logInfo(fullLoggingContext, s"Success response received" +
                s" with status ${response.status}, and correlationId: ${retrieveCorrelationId(response)}")
              Right(handleResponse[ProtectionRecordDetails, ResponseWrapper[ProtectionRecordDetails]](response.json, ResponseWrapper.wrap))
            case _ =>
              logError(fullLoggingContext, s"Error response received" +
                s" with status: ${response.status}, and correlationId: ${retrieveCorrelationId(response)} " +
                s" due to ${response.body}")
              Left(handleResponse[MpeError, ErrorWrapper](response.json, ErrorWrapper.wrap))
          }

      result
      } andThen {
        case Failure(t: Throwable) => logger.warn("Unable to retrieve the data", t)
      }
  }
}
