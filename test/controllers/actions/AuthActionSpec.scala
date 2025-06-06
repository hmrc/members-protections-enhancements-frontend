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
import com.google.inject.Inject
import config.FrontendAppConfig
import handlers.ErrorHandler
import play.api.mvc.{Action, AnyContent, BodyParsers, Results}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.authorise.Predicate
import uk.gov.hmrc.auth.core.retrieve.Retrieval
import uk.gov.hmrc.http.HeaderCarrier
import views.html.ErrorTemplate

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.{ExecutionContext, Future}

class AuthActionSpec extends SpecBase {

  class Harness(authAction: IdentifierAction) {
    def onPageLoad(): Action[AnyContent] = authAction { _ => Results.Ok }
  }

  "Auth Action" - {
    "when the user hasn't logged in" - {
      "must redirect the user to log in " in {
        val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            authConnector = new FakeFailingAuthConnector(new MissingBearerToken),
            config = appConfig,
            playBodyParsers = bodyParsers,
            errorHandler = errorHandler
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user's session has expired" - {
      "must redirect the user to log in " in {
        val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            authConnector = new FakeFailingAuthConnector(new BearerTokenExpired),
            config = appConfig,
            playBodyParsers = bodyParsers,
            errorHandler = errorHandler
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value must startWith(appConfig.loginUrl)
        }
      }
    }

    "the user doesn't have sufficient enrolments" - {
      "must redirect the user to the sign in page" in {
        val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            authConnector = new FakeFailingAuthConnector(new InsufficientEnrolments),
            config = appConfig,
            playBodyParsers = bodyParsers,
            errorHandler = errorHandler
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())
          val continueUrl = urlEncode(appConfig.loginContinueUrl)
          val expectedUrl = s"${appConfig.loginUrl}?continue=$continueUrl"

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe expectedUrl
        }
      }
    }

    "the user doesn't have sufficient confidence level" - {
      "must redirect the user to the sign in page" in {
        val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            new FakeFailingAuthConnector(new InsufficientConfidenceLevel),
            config = appConfig,
            playBodyParsers = bodyParsers,
            errorHandler = errorHandler
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())
          val continueUrl = urlEncode(appConfig.loginContinueUrl)
          val expectedUrl = s"${appConfig.loginUrl}?continue=$continueUrl"

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe expectedUrl
        }
      }
    }

    "the user used an unaccepted auth provider" - {
      "must redirect the user to the sign in page" in {
        val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            authConnector = new FakeFailingAuthConnector(new UnsupportedAuthProvider),
            config = appConfig,
            playBodyParsers = bodyParsers,
            errorHandler = errorHandler
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())
          val continueUrl = urlEncode(appConfig.loginContinueUrl)
          val expectedUrl = s"${appConfig.loginUrl}?continue=$continueUrl"

          status(result) mustBe SEE_OTHER
          redirectLocation(result).value mustBe expectedUrl
        }
      }
    }

    "the user has an unsupported affinity group" - {
      "must redirect the user to the sign in page" in {
        val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            authConnector = new FakeFailingAuthConnector(new UnsupportedAffinityGroup),
            config = appConfig,
            playBodyParsers = bodyParsers,
            errorHandler = errorHandler
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())
          val continueUrl = urlEncode(appConfig.loginContinueUrl)
          val expectedUrl = s"${appConfig.loginUrl}?continue=$continueUrl"

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(expectedUrl)
        }
      }
    }

    "the user has an unsupported credential role" - {
      "must redirect the user to the sign in page" in {
        val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            authConnector = new FakeFailingAuthConnector(new UnsupportedCredentialRole),
            config = appConfig,
            playBodyParsers = bodyParsers,
            errorHandler = errorHandler
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())
          val continueUrl = urlEncode(appConfig.loginContinueUrl)
          val expectedUrl = s"${appConfig.loginUrl}?continue=$continueUrl"

          status(result) mustBe SEE_OTHER
          redirectLocation(result) mustBe Some(expectedUrl)
        }
      }
    }

    "any unhandled exception occurs" - {
      "must redirect the user to the default error page" in {
        val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

        running(application) {
          val bodyParsers = application.injector.instanceOf[BodyParsers.Default]
          val appConfig = application.injector.instanceOf[FrontendAppConfig]
          val errorHandler = application.injector.instanceOf[ErrorHandler]

          val authAction = new AuthenticatedIdentifierAction(
            authConnector = new FakeFailingAuthConnector(new RuntimeException()),
            config = appConfig,
            playBodyParsers = bodyParsers,
            errorHandler = errorHandler
          )

          val controller = new Harness(authAction)
          val result = controller.onPageLoad()(FakeRequest())

          status(result) mustBe INTERNAL_SERVER_ERROR
          contentAsString(result) must include(
            "Sorry, there is a problem with the service - " +
              "500 - " +
              "Check a pension scheme member’s protections and enhancements"
          )
        }
      }
    }
  }
}

class FakeFailingAuthConnector @Inject()(exceptionToReturn: Throwable) extends AuthConnector {
  val serviceUrl: String = ""

  override def authorise[A](predicate: Predicate, retrieval: Retrieval[A])(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[A] =
    Future.failed(exceptionToReturn)
}
