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

package controllers.actions

import base.SpecBase
import config.{Constants, FrontendAppConfig}
import controllers.routes
import models.requests.IdentifierRequest.{AdministratorRequest, PractitionerRequest}
import models.requests.UserDetails
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Application
import play.api.libs.json.Json
import play.api.mvc.Results.Ok
import play.api.mvc.{Action, AnyContent, BodyParsers, Result}
import play.api.test.Helpers._
import play.api.test.{FakeRequest, StubPlayBodyParsersFactory}
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.auth.core.syntax.retrieved.authSyntaxForRetrieved
import uk.gov.hmrc.http.SessionKeys

import scala.concurrent.{ExecutionContext, Future}

class AuthenticatedIdentifierActionSpec extends SpecBase with StubPlayBodyParsersFactory {

  def authAction(appConfig: FrontendAppConfig) =
    new AuthenticatedIdentifierAction(
      authConnector = mockAuthConnector,
      config = appConfig,
      playBodyParsers = bodyParsers
    )(ExecutionContext.global)

  class Handler(appConfig: FrontendAppConfig) {
    def run: Action[AnyContent] = authAction(appConfig) { request =>
      request match {
        case AdministratorRequest(UserDetails(psrUserType, psrUserId, userId, affinityGroup), _) =>
          Ok(Json.obj("psrUserType" -> psrUserType, "userId" -> userId, "psaId" -> psrUserId, "affinityGroup" -> affinityGroup))

        case PractitionerRequest(UserDetails(psrUserType, psrUserId, userId, affinityGroup), _) =>
          Ok(Json.obj("psrUserType" -> psrUserType, "userId" -> userId, "pspId" -> psrUserId, "affinityGroup" -> affinityGroup))
      }
    }
  }

  def appConfig(implicit app: Application): FrontendAppConfig = injected[FrontendAppConfig]

  def handler(implicit app: Application): Handler = new Handler(appConfig)

  def authResult(affinityGroup: Option[AffinityGroup],
                 internalId: Option[String],
                 enrolments: Enrolment*): Option[String] ~ Option[AffinityGroup] ~ Enrolments =
    internalId and affinityGroup and Enrolments(enrolments.toSet)

  val psaEnrolment: Enrolment =
    Enrolment(Constants.psaEnrolmentKey, Seq(EnrolmentIdentifier(Constants.psaIdKey, "A2100001")), "Activated")

  val pspEnrolment: Enrolment =
    Enrolment(Constants.pspEnrolmentKey, Seq(EnrolmentIdentifier(Constants.pspIdKey, "21000002")), "Activated")

  private val mockAuthConnector: AuthConnector = mock[AuthConnector]
  private val bodyParsers: BodyParsers.Default = mock[BodyParsers.Default]

  def setAuthValue(value: Option[String] ~ Option[AffinityGroup] ~ Enrolments): Unit =
    setAuthValue(Future.successful(value))

  def setAuthValue[A](value: Future[A]): Unit =
    when(mockAuthConnector.authorise[A](any(), any())(any(), any()))
      .thenReturn(value)

  "AuthenticateIdentifierAction" - {
    "return an unauthorised result" - {
      "Redirect user to sign in page when user is not signed in to GG" in runningApplication { implicit app =>
        setAuthValue(Future.failed(new NoActiveSession("No user signed in") {}))

        val result = handler.run(FakeRequest())
        val continueUrl = urlEncode(appConfig.loginContinueUrl)
        val expectedUrl = s"${appConfig.loginUrl}?continue=$continueUrl"

        redirectLocation(result) mustBe Some(expectedUrl)
      }

      "Redirect user to unauthorised page when authorise fails to match predicate" in runningApplication { implicit app =>
        setAuthValue(Future.failed(new AuthorisationException("Authorise predicate fails") {}))

        val result = handler.run(FakeRequest())
        val expectedUrl = routes.UnauthorisedController.onPageLoad().url

        redirectLocation(result) mustBe Some(expectedUrl)
      }

      "Redirect to Unauthorised page when user does not have an Internal Id" in runningApplication { implicit app =>
        setAuthValue(authResult(Some(AffinityGroup.Individual), None, psaEnrolment))

        val result = handler.run(FakeRequest())
        val expectedUrl = routes.UnauthorisedController.onPageLoad().url

        redirectLocation(result) mustBe Some(expectedUrl)
      }

      "Redirect user to MPS registration page when user has insufficient enrolments" in runningApplication {
        implicit app =>
          setAuthValue(Future.failed(new InsufficientEnrolments("Insufficient enrolments") {}))

          val result = handler.run(FakeRequest().withSession(SessionKeys.sessionId -> "foo"))
          val expectedUrl = appConfig.mpsRegistrationUrl

          redirectLocation(result) mustBe Some(expectedUrl)
      }

      "Redirect user to error page when non-auth exception is thrown" in runningApplication { implicit app =>
        setAuthValue(Future.failed(new RuntimeException("Some funky error")))
        val result: Future[Result] = handler.run(FakeRequest())

        assertThrows[RuntimeException](
          await(result)
        )
      }
    }

    "return an IdentifierRequest" - {
      "User has a psa enrolment and has a valid session" in runningApplication { implicit app =>
        setAuthValue(authResult(Some(AffinityGroup.Individual), Some("internalId"), psaEnrolment))

        val result = handler.run(FakeRequest().withSession(SessionKeys.sessionId -> "foo"))

        status(result) mustBe OK
        (contentAsJson(result) \ "psaId").asOpt[String] mustBe Some("A2100001")
        (contentAsJson(result) \ "pspId").asOpt[String] mustBe None
        (contentAsJson(result) \ "userId").asOpt[String] mustBe Some("internalId")
      }

      "User has a psp enrolment and has a valid session" in runningApplication { implicit app =>
        setAuthValue(authResult(Some(AffinityGroup.Individual), Some("internalId"), pspEnrolment))

        val result = handler.run(FakeRequest().withSession(SessionKeys.sessionId -> "foo"))

        status(result) mustBe OK
        (contentAsJson(result) \ "psaId").asOpt[String] mustBe None
        (contentAsJson(result) \ "pspId").asOpt[String] mustBe Some("21000002")
        (contentAsJson(result) \ "userId").asOpt[String] mustBe Some("internalId")
      }
    }

    "handle exceptions during block invocation" in runningApplication { implicit app =>
      setAuthValue(authResult(Some(AffinityGroup.Individual), Some("internalId"), psaEnrolment))

      case class ExceptionHandler(appConfig: FrontendAppConfig) extends Handler(appConfig){
        override def run: Action[AnyContent] = authAction(appConfig) { _ =>
          throw new RuntimeException("An exception")
        }
      }

      val result: Future[Result] = ExceptionHandler(appConfig).run(
        FakeRequest().withSession(SessionKeys.sessionId -> "foo")
      )

      assertThrows[RuntimeException](
        await(result)
      )
    }

    "Redirect user to login" - {
      "User has a psa enrolment but has a no valid session" in runningApplication { implicit app =>
        setAuthValue(authResult(Some(AffinityGroup.Individual), Some("internalId"), psaEnrolment))

        val result = handler.run(FakeRequest())

        status(result) mustBe SEE_OTHER
        val continueUrl = urlEncode(appConfig.loginContinueUrl)
        val expectedUrl = s"${appConfig.loginUrl}?continue=$continueUrl"

        redirectLocation(result) mustBe Some(expectedUrl)
      }

      "User has a psp enrolment but has a no valid session" in runningApplication { implicit app =>
        setAuthValue(authResult(Some(AffinityGroup.Individual), Some("internalId"), pspEnrolment))

        val result = handler.run(FakeRequest())

        status(result) mustBe SEE_OTHER
        val continueUrl = urlEncode(appConfig.loginContinueUrl)
        val expectedUrl = s"${appConfig.loginUrl}?continue=$continueUrl"

        redirectLocation(result) mustBe Some(expectedUrl)
      }
    }
  }

}

