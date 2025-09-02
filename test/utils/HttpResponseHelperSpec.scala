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

import com.fasterxml.jackson.core.JsonParseException
import com.fasterxml.jackson.databind.JsonMappingException
import models.CorrelationId
import models.errors.{ErrorWrapper, MatchPerson, MpeError}
import models.response.RecordStatusMapped.Active
import models.response.RecordTypeMapped.FixedProtection2016
import models.response.{ProtectionRecord, ProtectionRecordDetails, ResponseWrapper}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.http.Status._
import play.api.libs.json.JsResultException
import uk.gov.hmrc.http._

// scalastyle:off magic.number

class HttpResponseHelperSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  import HttpResponseHelperSpec._

  "handleResponse" should "transform Bad Request into BadRequestException" in {
    val body: String =
      """
        |{
        | "code":"BAD_REQUEST",
        | "message":"message",
        | "source": "MatchPerson"
        |}""".stripMargin

    val response = responseFor(BAD_REQUEST, body)
    failure()(response).error shouldBe MpeError("BAD_REQUEST", "message", None, MatchPerson)
  }

  "handleResponse" should "throw JsonMappingException for empty json" in {
    val response = HttpResponse(OK, "")
    a[JsonMappingException] should be thrownBy success()(response)
  }

  "handleResponse" should "throw JsonParseException for invalid json" in {
    val response = HttpResponse(OK, "{foo}")
    a[JsonParseException] should be thrownBy success()(response)
  }

  "handleResponse" should "throw JsResultException for valid json with incorrect format" in {
    val response = HttpResponse(OK, "{}")
    a[JsResultException] should be thrownBy success()(response)
  }

  "handleResponse" should "return a valid response" in {
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

    val res: String =
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

    val response = HttpResponse(OK, res)
    testModel shouldBe success()(response).responseData
  }

  it should "transform FORBIDDEN into MpeError" in {
    val body: String =
      """
        |{
        | "code":"FORBIDDEN",
        | "message":"message",
        | "source": "MatchPerson"
        |}""".stripMargin

    val response = responseFor(FORBIDDEN, body)
    failure()(response).error shouldBe MpeError("FORBIDDEN", "message", None, MatchPerson)
  }

  it should "transform INTERNAL_SERVER_ERROR into MpeError" in {
    val body: String =
      """
        |{
        | "code":"INTERNAL_SERVER_ERROR",
        | "message":"message",
        | "source": "MatchPerson"
        |}""".stripMargin
    val response = responseFor(INTERNAL_SERVER_ERROR, body)
    failure()(response).error shouldBe MpeError("INTERNAL_SERVER_ERROR", "message", None, MatchPerson)
  }
}

object HttpResponseHelperSpec {
  implicit val correlationId: CorrelationId = "X-ID"
  class DummyClass extends Logging with HttpResponseHelper

  def failure(): HttpResponse => ErrorWrapper = res => {
    new DummyClass {}.handleResponse[MpeError, ErrorWrapper](
      res, ErrorWrapper.wrap, None, correlationId
    )
  }

  def success(): HttpResponse => ResponseWrapper[ProtectionRecordDetails] = res => {
    new DummyClass {}.handleResponse[ProtectionRecordDetails, ResponseWrapper[ProtectionRecordDetails]](
      res, ResponseWrapper.wrap, None, correlationId
    )
  }

  def responseFor(status: Int, response: String): HttpResponse = HttpResponse(status, response)

}
