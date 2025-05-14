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
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class MembersCheckAndRetrieveServiceImpl @Inject()(checkAndRetrieveConnector: MembersCheckAndRetrieveConnector) extends MembersCheckAndRetrieveService {

  override def checkAndRetrieve(pensionSchemeMemberRequest: Option[PensionSchemeMemberRequest])
                               (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String] =
    pensionSchemeMemberRequest match {
      case Some(data) => checkAndRetrieveConnector.checkAndRetrieve(data)
      case _ => Future("")
    }
}

@ImplementedBy(classOf[MembersCheckAndRetrieveServiceImpl])
trait MembersCheckAndRetrieveService {
  def checkAndRetrieve(pensionSchemeMemberRequest: Option[PensionSchemeMemberRequest])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[String]
}
