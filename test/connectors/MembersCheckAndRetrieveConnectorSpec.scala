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

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock._
import models.requests.PensionSchemeMemberRequest
import org.scalatest.RecoverMethods.recoverToSucceededIf
import org.scalatest.matchers.should.Matchers.convertToAnyShouldWrapper
import play.api.http.Status._
import uk.gov.hmrc.http.{BadRequestException, HeaderCarrier, NotFoundException, UpstreamErrorResponse}
import utils.UnrecognisedHttpResponseException

import scala.concurrent.ExecutionContext.Implicits.global

class MembersCheckAndRetrieveConnectorSpec extends SpecBase {

  private val checkAndRetrieveUrl = "members-protections-and-enhancements/check-and-retrieve"

  private implicit val headerCarrier: HeaderCarrier = HeaderCarrier()

  val connector: MembersCheckAndRetrieveConnector = app.injector.instanceOf[MembersCheckAndRetrieveConnector]

  val request = PensionSchemeMemberRequest(
    firstName = "Naren",
    lastName = "Vijay",
    dateOfBirth = "2024-12-31",
    nino = "QQ123456C",
    psaCheckRef = "PSA12345678A"
  )

  "checkAndRetrieve" - {
    "return valid response with status 200 for a valid submission" in {

      val response =
        """{
          |"statusCode": "200",
          |"message": "search successful, member details exists"
          |}""".stripMargin

      server.stubFor(
        post(urlEqualTo(s"$checkAndRetrieveUrl"))
          .willReturn(
            aResponse()
              .withStatus(OK)
              .withBody(response)
          )
      )

      connector.checkAndRetrieve(request).map(
        result => {
          result shouldBe response
          server.findAll(postRequestedFor(urlEqualTo(s"$checkAndRetrieveUrl"))).size() shouldBe 1
        }
      )

    }
  }

  "throw BadRequestException when the downstream return BAD_REQUEST response" in {
    server.stubFor(
      post(urlEqualTo(s"$checkAndRetrieveUrl"))
        .willReturn(
          aResponse()
            .withStatus(BAD_REQUEST)
        )
    )

    recoverToSucceededIf[BadRequestException] {
      connector.checkAndRetrieve(request.copy(nino = ""))
    } map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(s"$checkAndRetrieveUrl"))).size() shouldBe 1
    }
  }

  "throw NotFoundException when the downstream return NOT_FOUND response" in {
    server.stubFor(
      post(urlEqualTo(s"$checkAndRetrieveUrl"))
        .willReturn(
          aResponse()
            .withStatus(NOT_FOUND)
        )
    )

    recoverToSucceededIf[NotFoundException] {
      connector.checkAndRetrieve(request)
    } map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(s"$checkAndRetrieveUrl"))).size() shouldBe 1
    }
  }

  "throw UpstreamErrorResponse when the downstream return any 4xx status response" in {
    server.stubFor(
      post(urlEqualTo(s"$checkAndRetrieveUrl"))
        .willReturn(
          aResponse()
            .withStatus(FORBIDDEN)
        )
    )

    recoverToSucceededIf[UpstreamErrorResponse] {
      connector.checkAndRetrieve(request)
    } map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(s"$checkAndRetrieveUrl"))).size() shouldBe 1
    }
  }

  "throw UpstreamErrorResponse when the downstream return any 5xx status response" in {
    server.stubFor(
      post(urlEqualTo(s"$checkAndRetrieveUrl"))
        .willReturn(
          aResponse()
            .withStatus(BAD_GATEWAY)
        )
    )

    recoverToSucceededIf[UpstreamErrorResponse] {
      connector.checkAndRetrieve(request)
    } map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(s"$checkAndRetrieveUrl"))).size() shouldBe 1
    }
  }

  "throw UnrecognisedHttpResponseException when the downstream return unknown error response" in {
    server.stubFor(
      post(urlEqualTo(s"$checkAndRetrieveUrl"))
        .willReturn(
          aResponse()
            .withStatus(MOVED_PERMANENTLY)
        )
    )

    recoverToSucceededIf[UnrecognisedHttpResponseException] {
      connector.checkAndRetrieve(request)
    } map {
      _ =>
        server.findAll(postRequestedFor(urlEqualTo(s"$checkAndRetrieveUrl"))).size() shouldBe 1
    }
  }
}
