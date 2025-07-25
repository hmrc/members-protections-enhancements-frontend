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

import models.response.ProtectionRecordDetails
import play.api.http.Status._
import play.api.libs.json.{JsError, JsResultException, JsSuccess, JsValue}
import uk.gov.hmrc.http._

trait HttpResponseHelper extends HttpErrorFunctions {

  implicit val httpResponseReads: HttpReads[HttpResponse] = (method: String, url: String, response: HttpResponse) => response

  def handleErrorResponse(httpMethod: String, url: String)(response: HttpResponse): String =
    response.status match {
      case BAD_REQUEST =>
        badRequestMessage(httpMethod, url, response.body)
      case status if is4xx(status) || is5xx(status) =>
        upstreamResponseMessage(httpMethod, url, status, response.body)
      case _ =>
        new UnrecognisedHttpResponseException(httpMethod, url, response).getMessage
    }

  def handleSuccessResponse(response: JsValue): ProtectionRecordDetails =
    response.validate[ProtectionRecordDetails] match {
      case JsSuccess(value, _) => value
      case JsError(errors) => throw JsResultException(errors)
    }
}

class UnrecognisedHttpResponseException(method: String, url: String, response: HttpResponse)
  extends Exception(s"$method of '$url' failed with status ${response.status}. Response body: '$response'")

class MpeException(message: String)
  extends RuntimeException(message)
