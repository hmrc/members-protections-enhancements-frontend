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

package controllers

import base.SpecBase
import com.github.tomakehurst.wiremock.client.WireMock.aResponse
import models._
import models.requests.PensionSchemeMemberRequest
import models.response.RecordStatusMapped.Active
import models.response.RecordTypeMapped.FixedProtection2016
import models.response.{ProtectionRecord, ProtectionRecordDetails}
import pages._
import play.api.http.Status.OK
import play.api.libs.json.Json
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ResultsView

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util.Locale

class ResultsControllerSpec extends SpecBase {

  trait Test {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val memberDetails: MemberDetails = MemberDetails("Pearl", "Harvey")
    val membersDob: MembersDob = MembersDob(1, 1, 2022)
    val membersNino: MembersNino = MembersNino("AB123456A")
    val membersPsaCheckRef: MembersPsaCheckRef = MembersPsaCheckRef("PSA12345678A")

    val userAnswers = emptyUserAnswers
      .set(page = WhatIsTheMembersNamePage, value = memberDetails).success.value
      .set(page = MembersDobPage, value = membersDob).success.value
      .set(page = MembersNinoPage, value = membersNino).success.value
      .set(page = MembersPsaCheckRefPage, value = membersPsaCheckRef).success.value

    val application = applicationBuilder(userAnswers = userAnswers).build()

    val backLinkRoute = routes.CheckYourAnswersController.onPageLoad().url

    val dateTimeWithZone = ZonedDateTime.now(ZoneId.of("Europe/London"))
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' h:mma")
    val localDateTime = dateTimeWithZone.format(formatter.withLocale(Locale.UK))

    val pensionSchemeMemberRequest = PensionSchemeMemberRequest("Pearl", "Harvey", "2022-01-01", "AB123456A", "PSA12345678A")

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

    def setUpStubs(status: Int, response: String) = stubPost(checkAndRetrieveUrl, Json.toJson(pensionSchemeMemberRequest).toString(),
      aResponse().withStatus(status).withBody(response))
  }

  "Results Controller" - {
    "must return OK and the correct view for a GET" in new Test {

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

      running(application) {
        val request = FakeRequest(GET, routes.ResultsController.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[ResultsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          memberDetails,
          membersDob,
          membersNino,
          membersPsaCheckRef,
          Some(backLinkRoute),
          localDateTime,
          testModel
        )(request, messages(application)).toString
      }
    }

    "must redirect to NoResults page when response received with 404 status" in new Test {

      setUpStubs(NOT_FOUND, "")

      running(application) {
        val request = FakeRequest(GET, routes.ResultsController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.NoResultsController.onPageLoad().url
      }
    }

    "must redirect to Journey Recovery for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        val request = FakeRequest(GET, routes.ResultsController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.JourneyRecoveryController.onPageLoad().url
      }
    }
  }
}
