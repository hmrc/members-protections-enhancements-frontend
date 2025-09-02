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

package utils

import models.CorrelationId
import play.api.libs.json._
import uk.gov.hmrc.http._

trait HttpResponseHelper extends HttpErrorFunctions {_: NewLogging =>

  implicit def httpResponseReads: HttpReads[HttpResponse] = (method: String,
                                                             url: String,
                                                             response: HttpResponse) => {
    logger.info(
      secondaryContext = "httpResponseReads",
      message = s"HTTP call with method: $method, url: $url completed with status: ${response.status}",
      dataLog = correlationIdLogString(response.header("correlationId").getOrElse("N/A"))
    )
    response
  }

  private def checkIdsMatch(requestCorrelationId: CorrelationId,
                            responseCorrelationId: CorrelationId,
                            extraLoggingContext: Option[String]): CorrelationId =
    if (requestCorrelationId.value == responseCorrelationId.value) {
      responseCorrelationId
    } else {
      logger.error(
        secondaryContext = "checkIdsMatch",
        message = "Correlation ID was either missing from response, or did not match ID from request. " +
          "Reverting to ID from request for consistency in logs. Be aware of potential ID inconsistencies" +
          s"Request C-ID: ${requestCorrelationId.value}, Response C-ID: ${responseCorrelationId.value}",
        extraContext = extraLoggingContext
      )
      requestCorrelationId
    }

  def handleResponse[A, F](response: HttpResponse, wrap: A => F)
                          (implicit reads: Reads[A], requestCorrelationId: CorrelationId): F = {
    val methodLoggingContext: String = "handleResponse"

    val correlationId = checkIdsMatch(
      requestCorrelationId = requestCorrelationId,
      responseCorrelationId = response.header("correlationId").getOrElse("N/A"),
      extraLoggingContext = Some(methodLoggingContext)
    )

    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(correlationId))

    response.json.validate[A] match {
      case JsSuccess(value, _) => wrap(value)
      case JsError(errors) => throw JsResultException(errors)
    }
  }
}
