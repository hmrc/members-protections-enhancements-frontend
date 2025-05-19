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
import models.requests.PensionSchemeMemberRequest
import org.mockito.Mockito.when
import org.scalatestplus.scalacheck.ScalaCheckPropertyChecks
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class MembersCheckAndRetrieveServiceSpec extends SpecBase with ScalaCheckPropertyChecks {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  val mockConnector: MembersCheckAndRetrieveConnector = mock[MembersCheckAndRetrieveConnector]
  val service = new MembersCheckAndRetrieveServiceImpl(mockConnector)

  override def beforeEach(): Unit = {
    super.beforeEach()
  }

  "checkAndRetrieve" - {
    "return a valid response body for a valid data" in {

      val request = PensionSchemeMemberRequest(
        firstName = "Naren",
        lastName = "Vijay",
        dateOfBirth = "2024-12-31",
        nino = "QQ123456C",
        psaCheckRef = "PSA12345678A"
      )

      val response = """{
                                  |"statusCode": "200",
                                  |"message": "search successful, member details exists"
                                  |}""".stripMargin
      when(mockConnector.checkAndRetrieve(request)).thenReturn(Future.successful(response))

      val result = service.checkAndRetrieve(Some(request))
      await(result) mustBe response
    }

    "return an empty response body for invalid or no data submission" in {

      val response = "error"

      val result = service.checkAndRetrieve(None)
      await(result) mustBe response
    }
  }

}
