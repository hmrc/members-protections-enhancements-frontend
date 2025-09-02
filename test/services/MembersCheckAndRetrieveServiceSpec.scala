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

import base.SpecBase
import connectors.MembersCheckAndRetrieveConnector
import models.CorrelationId
import models.errors.{ErrorWrapper, NotFoundError}
import models.requests.PensionSchemeMemberRequest
import models.response.RecordStatusMapped.Active
import models.response.RecordTypeMapped.FixedProtection2016
import models.response.{ProtectionRecord, ProtectionRecordDetails, ResponseWrapper}
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MembersCheckAndRetrieveServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  implicit val hc: HeaderCarrier = HeaderCarrier()
  implicit val correlationId: CorrelationId = "X-123"
  val mockConnector: MembersCheckAndRetrieveConnector = mock[MembersCheckAndRetrieveConnector]
  val service = new MembersCheckAndRetrieveServiceImpl(mockConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
  }


  val request: PensionSchemeMemberRequest = PensionSchemeMemberRequest(
    firstName = "Naren",
    lastName = "Vijay",
    dateOfBirth = "2024-12-31",
    nino = "QQ123456C",
    psaCheckRef = "PSA12345678A"
  )

  "checkAndRetrieve" - {
    "return a valid response body for a valid data" in {

      val response = ResponseWrapper(
        correlationId = correlationId,
        responseData = ProtectionRecordDetails(Seq(
          ProtectionRecord(
            protectionReference = Some("some-id"),
            `type` = FixedProtection2016,
            status = Active,
            protectedAmount = Some(1),
            lumpSumAmount = Some(1),
            lumpSumPercentage = Some(1),
            enhancementFactor = Some(0.5)
          )
        ))
      )

      when(mockConnector.checkAndRetrieve(request)).thenReturn(Future.successful(Right(response)))

      val result = service.checkAndRetrieve(request)
      await(result) mustBe Right(response)
    }

    "return a NotFoundError for an invalid submission" in {
      val response = Left(ErrorWrapper(correlationId, NotFoundError))

      when(mockConnector.checkAndRetrieve(request)).thenReturn(Future.successful(response))

      val result = service.checkAndRetrieve(request)
      await(result) mustBe response
    }
  }

}
