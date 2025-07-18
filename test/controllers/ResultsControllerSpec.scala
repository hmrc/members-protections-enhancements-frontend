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
import com.github.tomakehurst.wiremock.stubbing.StubMapping
import models._
import models.requests.PensionSchemeMemberRequest
import models.response.RecordStatusMapped.Active
import models.response.RecordTypeMapped.FixedProtection2016
import models.response.{ProtectionRecord, ProtectionRecordDetails}
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.{times, verify, when}
import org.mockito.stubbing.OngoingStubbing
import pages._
import play.api.http.Status.OK
import play.api.{Application, inject}
import play.api.libs.json.Json
import play.api.mvc.Result
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers._
import services.FailedAttemptService
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ResultsView

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util.Locale
import scala.concurrent.Future

class ResultsControllerSpec extends SpecBase {

  trait Test {
    implicit val hc: HeaderCarrier = HeaderCarrier()

    val memberDetails: MemberDetails = MemberDetails("Pearl", "Harvey")
    val membersDob: MembersDob = MembersDob(1, 1, 2022)
    val membersNino: MembersNino = MembersNino("AB123456A")
    val membersPsaCheckRef: MembersPsaCheckRef = MembersPsaCheckRef("PSA12345678A")

    val mockService: FailedAttemptService = mock[FailedAttemptService]
    val checkLockoutResult: Option[Result] = None

    val userAnswers: UserAnswers = emptyUserAnswers
      .set(page = WhatIsTheMembersNamePage, value = memberDetails).success.value
      .set(page = MembersDobPage, value = membersDob).success.value
      .set(page = MembersNinoPage, value = membersNino).success.value
      .set(page = MembersPsaCheckRefPage, value = membersPsaCheckRef).success.value

    lazy val application: Application = applicationBuilder(
      userAnswers = userAnswers,
      checkLockoutResult = checkLockoutResult
    )
      .overrides(
        inject.bind(classOf[FailedAttemptService]).toInstance(mockService)
      )
      .build()

    def mockFailedAttemptCheck(checkResult: Boolean = false): OngoingStubbing[Future[Boolean]] = when(
      mockService.checkForLockout()(ArgumentMatchers.any(), ArgumentMatchers.any())
    ).thenReturn(
      Future.successful(checkResult)
    )

    def mockHandleFailedAttempt(result: Result): OngoingStubbing[Future[Result]] =       when(
      mockService.handleFailedAttempt(ArgumentMatchers.any())(ArgumentMatchers.any())(
        ArgumentMatchers.any(),
        ArgumentMatchers.any()
      )
    ).thenReturn(
      Future.successful(result)
    )
    val backLinkRoute: String = routes.CheckYourAnswersController.onPageLoad().url

    val dateTimeWithZone: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"))
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' h:mma")
    val localDateTime: String = dateTimeWithZone.format(formatter.withLocale(Locale.UK))

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

    def setUpStubs(status: Int, response: String): StubMapping = stubPost(
      url = checkAndRetrieveUrl,
      requestBody = Json.toJson(pensionSchemeMemberRequest).toString(),
      response = aResponse().withStatus(status).withBody(response)
    )
  }

  "Results Controller" - {
    "must redirect to unauthorised page if user is not allowed" in {
      val userAnswers = emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value

      val application = applicationBuilder(
        userAnswers = userAnswers,
        allowListResponse = Some(Redirect(routes.UnauthorisedController.onPageLoad()))
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.ResultsController.onPageLoad().url)

        val result = route(application, request).value
        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "must redirect to lockout page if the user is locked out" in new Test {
      override val checkLockoutResult: Option[Result] = Some(
        Redirect(routes.LockedOutController.onPageLoad())
      )

      running(application) {
        val request = FakeRequest(GET, routes.ResultsController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.LockedOutController.onPageLoad().url)
      }
    }

    "must return OK and the correct view for a GET" - {
      "when correlation ID exists in the request" in new Test {
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

          verify(mockIdGenerator, times(0)).getCorrelationId
        }
      }

      "when correlation ID is not in the request" in new Test {
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

        override lazy val application: Application = applicationBuilder(
          userAnswers = userAnswers,
          correlationIdInRequest = None
        )
          .build()

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

          verify(mockIdGenerator, times(1)).getCorrelationId
        }
      }
    }

    "must redirect to NoResults page when failed attempt threshold not exceeded for a failed attempt" in new Test {
      mockFailedAttemptCheck()
      mockHandleFailedAttempt(Redirect(routes.NoResultsController.onPageLoad()))
      setUpStubs(NOT_FOUND, "")

      running(application) {
        val request = FakeRequest(GET, routes.ResultsController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.NoResultsController.onPageLoad().url
      }
    }

    "must redirect to Lockout page when failed attempt threshold exceeded for a failed attempt" in new Test {
      mockFailedAttemptCheck(checkResult = true)
      mockHandleFailedAttempt(Redirect(routes.LockedOutController.onPageLoad()))
      setUpStubs(NOT_FOUND, "")

      running(application) {
        val request = FakeRequest(GET, routes.ResultsController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.LockedOutController.onPageLoad().url
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
