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

import models.response.RecordStatusMapped.Active
import models.response.RecordTypeMapped.FixedProtection2016
import models.response.{ProtectionRecord, ProtectionRecordDetails}
import org.scalatest.flatspec.AnyFlatSpec
import org.scalatest.matchers.should.Matchers
import org.scalatestplus.scalacheck.ScalaCheckDrivenPropertyChecks
import play.api.http.Status._
import play.api.libs.json.JsResultException
import uk.gov.hmrc.http._

// scalastyle:off magic.number

class HttpResponseHelperSpec extends AnyFlatSpec with Matchers with ScalaCheckDrivenPropertyChecks {

  import HttpResponseHelperSpec._

  "handleErrorResponse" should "transform Bad Request into BadRequestException" in {
    val response = responseFor(BAD_REQUEST)
    val expected = "test-method of 'test-url' returned 400 (Bad Request). Response body 'Message for 400'"
    failure()(response) shouldBe expected
  }

  "handleSuccessResponse" should "throw JsResultException for invalid json" in {
    val response = HttpResponse(OK, "{}")
    a[JsResultException] should be thrownBy success()(response)
  }

  "handleSuccessResponse" should "return a valid response" in {

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
    testModel shouldBe success()(response)
  }

  it should "transform any 4xx into Upstream4xxResponse other than 400" in {
    val response = responseFor(FORBIDDEN)
    val expected = "test-method of 'test-url' returned 403. Response body: 'Message for 403'"
    failure()(response) shouldBe expected
  }

  it should "transform any other 5xx into Upstream5xxResponse" in {
    val response = responseFor(BAD_GATEWAY)
    val expected = "test-method of 'test-url' returned 502. Response body: 'Message for 502'"
    failure()(response) shouldBe expected
  }

  it should "transform any other status other than 4xx and 5xx into an UnrecognisedHttpResponseException" in {
    val response = responseFor(NOT_MODIFIED)
    val expected = "test-method of 'test-url' failed with status 304. Response body: 'HttpResponse status=304'"
    failure()(response) shouldBe expected
  }

}

object HttpResponseHelperSpec {

  def failure(): HttpResponse => String = {
    new HttpResponseHelper {}.handleErrorResponse("test-method", "test-url")
  }

  def success(): HttpResponse => ProtectionRecordDetails = res => {
    new HttpResponseHelper {}.handleSuccessResponse(res.json)
  }

  def responseFor(status: Int): HttpResponse = HttpResponse(status, s"Message for $status")

}
