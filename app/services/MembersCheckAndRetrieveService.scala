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

package services

import com.google.inject.ImplementedBy
import connectors.MembersCheckAndRetrieveConnector
import models.requests.PensionSchemeMemberRequest
import models.{CorrelationId, ResultType}
import uk.gov.hmrc.http.HeaderCarrier
import utils.NewLogging

import javax.inject.{Inject, Singleton}
import scala.concurrent.ExecutionContext

@ImplementedBy(classOf[MembersCheckAndRetrieveServiceImpl])
trait MembersCheckAndRetrieveService {
  def checkAndRetrieve(pensionSchemeMemberRequest: PensionSchemeMemberRequest)
                      (implicit hc: HeaderCarrier,
                       ec: ExecutionContext,
                       correlationId: CorrelationId): ResultType
}

@Singleton
class MembersCheckAndRetrieveServiceImpl @Inject()(connector: MembersCheckAndRetrieveConnector)
  extends MembersCheckAndRetrieveService with NewLogging {

  override def checkAndRetrieve(pensionSchemeMemberRequest: PensionSchemeMemberRequest)
                               (implicit hc: HeaderCarrier,
                                ec: ExecutionContext,
                                correlationId: CorrelationId): ResultType = {
    logger.info(
      secondaryContext = "checkAndRetrieve",
      message = "Attempting to find match for supplied member, and retrieve associated protection record details",
      dataLog = correlationIdLogString(correlationId)
    )
    connector.checkAndRetrieve(pensionSchemeMemberRequest)
  }
}
