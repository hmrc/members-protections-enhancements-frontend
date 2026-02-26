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

import models.errors.ErrorSource.MatchPerson
import models.errors.MpeError
import models.response.RecordStatusMapped.Active
import models.response.RecordTypeMapped.FixedProtection2016
import models.response.{ProtectionRecord, ProtectionRecordDetails}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.http.Status.*
import play.api.libs.json.JsResultException
import uk.gov.hmrc.http.*

// scalastyle:off magic.number

class HttpResponseHelperSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  import HttpResponseHelperSpec.*

  "handleResponse" should "transform Bad Request into BadRequestException" in {
    val body: String =
      """
        |{
        | "code":"BAD_REQUEST",
        | "message":"message",
        | "source": "MatchPerson"
        |}""".stripMargin

    val response = responseFor(BAD_REQUEST, body)
    failure()(response) shouldBe MpeError("BAD_REQUEST", "message", None, MatchPerson)
  }

  "handleResponse" should "throw JsResultException for invalid json" in {
    val response = HttpResponse(OK, "{}")
    a[JsResultException] should be thrownBy success()(response)
  }

  "handleResponse" should "return a valid response" in {

    val testModel: ProtectionRecordDetails = ProtectionRecordDetails(
      Seq(
        ProtectionRecord(
          protectionReference = Some("some-id"),
          `type` = FixedProtection2016,
          status = Active,
          protectedAmount = Some(1),
          lumpSumAmount = Some(1),
          lumpSumPercentage = Some(1),
          enhancementFactor = Some(0.5)
        )
      )
    )

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
    testModel shouldBe success()(response)
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
    failure()(response) shouldBe MpeError("FORBIDDEN", "message", None, MatchPerson)
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
    failure()(response) shouldBe MpeError("INTERNAL_SERVER_ERROR", "message", None, MatchPerson)
  }
}

object HttpResponseHelperSpec {

  def failure(): HttpResponse => MpeError = res => new HttpResponseHelper {}.handleResponse[MpeError](res.json)

  def success(): HttpResponse => ProtectionRecordDetails = res =>
    new HttpResponseHelper {}.handleResponse[ProtectionRecordDetails](res.json)

  def responseFor(status: Int, response: String): HttpResponse = HttpResponse(status, response)

}
