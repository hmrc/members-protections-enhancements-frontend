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
import com.github.tomakehurst.wiremock.client.ResponseDefinitionBuilder
import com.github.tomakehurst.wiremock.client.WireMock.*
import com.github.tomakehurst.wiremock.http.Fault
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models.allowlist.CheckRequest
import play.api.Application
import play.api.http.Status.{INTERNAL_SERVER_ERROR, NOT_FOUND, OK}
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.Json
import uk.gov.hmrc.http.HeaderCarrier

class UserAllowListConnectorSpec
  extends SpecBase {

  trait Test {
    implicit lazy val hc: HeaderCarrier = HeaderCarrier()

     val app: Application =
      new GuiceApplicationBuilder()
        .configure(
          "microservice.services.user-allow-list.port" -> wireMockServer.port,
          "internal-auth.token" -> "token"
        )
        .build()

    val connector: UserAllowListConnector = app.injector.instanceOf[UserAllowListConnector]

    def setUpStubs(url: String, status: Int, response: ResponseDefinitionBuilder): StubMapping =
      stubPostWithAuth(url, Json.stringify(Json.toJson(CheckRequest("value"))),
        response.withStatus(status))
  }
  ".check" - {

    val feature = "foobar"
    val url = s"/user-allow-list/members-protections-enhancements-frontend/$feature/check"
    val request = CheckRequest("value")

    "must return true when the server responds OK" in new Test {

      setUpStubs(url, OK, aResponse())

      connector.check(feature, request.value).futureValue mustBe true
    }

    "must return false when the server responds NOT_FOUND" in new Test {

      setUpStubs(url, NOT_FOUND, aResponse())

      connector.check(feature, request.value).futureValue mustBe false
    }

    "must fail when the server responds with any other status" in new Test {

      setUpStubs(url, INTERNAL_SERVER_ERROR, aResponse())

      connector.check(feature, request.value).failed.futureValue
    }

    "must fail when the connection fails" in new Test {

      setUpStubs(url, INTERNAL_SERVER_ERROR, aResponse().withFault(Fault.RANDOM_DATA_THEN_CLOSE))

      connector.check(feature, request.value).failed.futureValue
    }
  }
}