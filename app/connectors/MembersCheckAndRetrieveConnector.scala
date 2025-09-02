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
import models.errors.{ErrorWrapper, MpeError}
import models.requests.PensionSchemeMemberRequest
import models.response.{ProtectionRecordDetails, ResponseWrapper}
import models.{CorrelationId, ResultType}
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.{HttpResponseHelper, Logging}

import scala.concurrent.ExecutionContext
import scala.util.Failure

@ImplementedBy(classOf[MembersCheckAndRetrieveConnectorImpl])
trait MembersCheckAndRetrieveConnector extends Logging {

  def checkAndRetrieve(pensionSchemeMemberRequest: PensionSchemeMemberRequest)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: CorrelationId): ResultType
}

class MembersCheckAndRetrieveConnectorImpl @Inject()(httpClientV2: HttpClientV2, config: FrontendAppConfig)
  extends MembersCheckAndRetrieveConnector with HttpResponseHelper {

  private def retrieveCorrelationId(response: HttpResponse): CorrelationId =
    CorrelationId(response.header("correlationId").getOrElse("No correlationId"))

  private def checkIdsMatch(requestCorrelationId: CorrelationId,
                              responseCorrelationId: CorrelationId,
                              extraLoggingContext: Option[String]): CorrelationId = {
    if (requestCorrelationId.value != responseCorrelationId.value) {
      logger.error(
        secondaryContext = "checkIdsMatch",
        message = "Correlation ID was either missing from response, or did not match ID from request. " +
          "Reverting to ID from request for consistency in logs. Be aware of potential ID inconsistencies" +
          s"Request C-ID: ${requestCorrelationId.value}, Response C-ID: ${responseCorrelationId.value}",
        extraContext = extraLoggingContext
      )
    }

    requestCorrelationId
  }

  override def checkAndRetrieve(pensionSchemeMemberRequest: PensionSchemeMemberRequest)
                               (implicit hc: HeaderCarrier,
                                ec: ExecutionContext,
                                correlationId: CorrelationId): ResultType = {
    val checkAndRetrieveUrl = url"${config.checkAndRetrieveUrl}"

    val methodLoggingContext: String = "checkAndRetrieve"
    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(correlationId))
    infoLogger("Attempting to match member, and retrieve protection record details using the provided user answers")

    httpClientV2.post(checkAndRetrieveUrl)
      .withBody(Json.toJson(pensionSchemeMemberRequest))
      .setHeader(("correlationId", correlationId.value))
      .execute[HttpResponse].map { response =>
        val resultCorrelationId: CorrelationId = checkIdsMatch(
          requestCorrelationId = correlationId,
          responseCorrelationId = retrieveCorrelationId(response),
          extraLoggingContext = Some(methodLoggingContext)
        )

        val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(resultCorrelationId))
        val warnLogger: (String, Option[Throwable]) => Unit = warnLog(
          secondaryContext = methodLoggingContext,
          dataLog = correlationIdLogString(resultCorrelationId)
        )
        infoLogger("Successfully completed check and retrieve. Attempting to parse HTTP response")

        val result: Either[ErrorWrapper, ResponseWrapper[ProtectionRecordDetails]] = response.status match {
          case OK =>
            infoLogger("Success response received with 200 status. Attempting to parse protection record details")
            Right(handleResponse[ProtectionRecordDetails, ResponseWrapper[ProtectionRecordDetails]](
              response, ResponseWrapper.wrap, Some(methodLoggingContext), resultCorrelationId
            ))
          case errStatus =>
            warnLogger(s"Error response received with $errStatus status. Attempting to parse error", None)
            Left(handleResponse[MpeError, ErrorWrapper](
              response, ErrorWrapper.wrap, Some(methodLoggingContext), resultCorrelationId
            ))
        }

        result
      } andThen {
      case Failure(t: Throwable) => logger.errorWithException(
        secondaryContext = methodLoggingContext,
        message = s"Attempt to complete check and retrieve failed with error: ${t.getMessage}",
        ex = t,
        dataLog = correlationIdLogString(correlationId)
      )
    }
  }
}
