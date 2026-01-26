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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.errors.ErrorSource.MatchPerson
import models.errors.MpeError
import models.requests.PensionSchemeMemberRequest
import models.response.RecordStatusMapped.Active
import models.response.RecordTypeMapped.FixedProtection2016
import models.response.{ProtectionRecord, ProtectionRecordDetails}
import org.scalatest.matchers.should.Matchers.shouldBe
import play.api.Application
import play.api.http.Status.*
import play.api.libs.json.Json
import play.api.test.Helpers.{await, defaultAwaitTimeout}
import uk.gov.hmrc.http.HeaderCarrier

import scala.concurrent.ExecutionContext.Implicits.global

class MembersCheckAndRetrieveConnectorSpec extends SpecBase {

  trait Test {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    implicit val correlationId: String = "X-123"
    val app: Application = applicationBuilder(emptyUserAnswers).build()

    val connector: MembersCheckAndRetrieveConnector = app.injector.instanceOf[MembersCheckAndRetrieveConnector]

    val pensionSchemeMemberRequest: PensionSchemeMemberRequest = PensionSchemeMemberRequest("Pearl", "Harvey", "2022-01-01", "AB123456A", "PSA12345678A")

    val checkAndRetrieveUrl = "/members-protections-and-enhancements/check-and-retrieve"

    val testModel: ProtectionRecordDetails = ProtectionRecordDetails(Seq(
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

    def setUpStubs(status: Int, response: String): StubMapping = stubPost(checkAndRetrieveUrl, Json.toJson(pensionSchemeMemberRequest).toString(),
      aResponse().withStatus(status).withBody(response).withHeader("correlationId", "X-123"))
  }

  "checkAndRetrieve" - {
    "return valid response with status 200 for a valid submission" in new Test {

      val response: String =
        """
          |{
          | "protectionRecords": [
          |   {
          |     "protectionReference": "some-id",
          |     "type": "FIXED PROTECTION 2016",
          |     "status": "OPEN",
          |     "protectedAmount": 1,
          |     "lumpSumAmount": 1,
          |     "lumpSumPercentage": 1,
          |     "enhancementFactor": 0.5
          |   }
          | ]
          |}""".stripMargin

      setUpStubs(OK, response)

      private val result = await(connector.checkAndRetrieve(pensionSchemeMemberRequest))
      result shouldBe Right(testModel)
      WireMock.verify(postRequestedFor(urlEqualTo(checkAndRetrieveUrl)))
    }
  }

  "return MpeError when the downstream return NOT_FOUND response" in new Test {
    val response: String =
      """
        |{
        | "code":"CODE",
        | "message":"message",
        | "source": "MatchPerson"
        |}""".stripMargin

    setUpStubs(NOT_FOUND, response)

    private val result = await(connector.checkAndRetrieve(pensionSchemeMemberRequest))
    result shouldBe Left(MpeError("CODE", "message", None, MatchPerson))
    WireMock.verify(postRequestedFor(urlEqualTo(checkAndRetrieveUrl)))
  }

  "return appropriate MpeError when the downstream return error response" in new Test {
    val response: String =
      """
        |{
        | "code":"BAD_REQUEST",
        | "message":"message",
        | "source": "MatchPerson"
        |}""".stripMargin

    setUpStubs(BAD_REQUEST, response)

    private val result = await(connector.checkAndRetrieve(pensionSchemeMemberRequest))
    result shouldBe Left(MpeError("BAD_REQUEST", "message", None, MatchPerson))
    WireMock.verify(postRequestedFor(urlEqualTo(checkAndRetrieveUrl)))
  }
}
