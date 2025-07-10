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
import models.errors.{MpeError, NotFoundError}
import models.requests.PensionSchemeMemberRequest
import models.response.ProtectionRecordDetails
import play.api.Logger
import play.api.http.Status._
import play.api.libs.json._
import uk.gov.hmrc.http.client.HttpClientV2
import uk.gov.hmrc.http.{HeaderCarrier, HttpResponse, StringContextOps}
import utils.HttpResponseHelper

import scala.concurrent.{ExecutionContext, Future}
import scala.util.Failure

@ImplementedBy(classOf[MembersCheckAndRetrieveConnectorImpl])
trait MembersCheckAndRetrieveConnector {

  def checkAndRetrieve(pensionSchemeMemberRequest: PensionSchemeMemberRequest)
                      (implicit hc: HeaderCarrier, ec: ExecutionContext):  Future[Either[MpeError, ProtectionRecordDetails]]
}

class MembersCheckAndRetrieveConnectorImpl @Inject()(httpClientV2: HttpClientV2, config: FrontendAppConfig)
  extends MembersCheckAndRetrieveConnector
    with HttpResponseHelper {

  private val logger = Logger(classOf[MembersCheckAndRetrieveConnectorImpl])

  override def checkAndRetrieve(pensionSchemeMemberRequest: PensionSchemeMemberRequest)
                               (implicit hc: HeaderCarrier, ec: ExecutionContext):  Future[Either[MpeError, ProtectionRecordDetails]] = {

    val checkAndRetrieveUrl = url"${config.checkAndRetrieveUrl}"

    httpClientV2.post(checkAndRetrieveUrl)
      .withBody(Json.toJson(pensionSchemeMemberRequest))
      .execute[HttpResponse].map { response =>
        response.status match {
            case OK =>
              logger.info(s"Response from the NPS: ${Json.prettyPrint(response.json)}")
              Right(handleSuccessResponse(response.json))
            case NOT_FOUND => Left(NotFoundError)
            case _ => handleErrorResponse("POST", checkAndRetrieveUrl.toString)(response)
          }
      } andThen {
        case Failure(t: Throwable) => logger.warn("Unable to retrieve the data", t)
      }
  }
}
