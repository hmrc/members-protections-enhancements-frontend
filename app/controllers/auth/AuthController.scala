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

package controllers.auth

import config.FrontendAppConfig
import controllers.actions.IdentifierAction
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.NewLogging

import javax.inject.Inject
import scala.concurrent.ExecutionContext


class AuthController @Inject()(val controllerComponents: MessagesControllerComponents,
                               config: FrontendAppConfig,
                               sessionRepository: SessionRepository,
                               identify: IdentifierAction)
                              (implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with NewLogging {

  def signOut(): Action[AnyContent] = identify.async { implicit request =>
    val methodLoggingContext: String = "signOut"
    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(request.correlationId))

    infoLogger("Attempting to clear user session data and redirect to sign out flow with exit survey")

    sessionRepository
      .clear(request.userDetails.userId)
      .map {
        _ =>
          infoLogger("Successfully cleared user session data, redirecting to sign out flow with exit survey")
          Redirect(config.exitSurveyUrl).withNewSession
      }
  }

  def signOutNoSurvey(): Action[AnyContent] = identify.async { implicit request =>
    val methodLoggingContext: String = "signOutNoSurvey"
    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(request.correlationId))

    infoLogger("Attempting to clear user session data and redirect to sign out flow")

    sessionRepository
      .clear(request.userDetails.userId)
      .map {
        _ =>
          infoLogger("Successfully cleared user session data, redirecting to sign out flow")
          Redirect(config.signOutUrl, Map("continue" -> Seq(routes.SignedOutController.onPageLoad().url)))
      }
  }

  def sessionTimeout(): Action[AnyContent] = identify.async { implicit request =>
    val methodLoggingContext: String = "sessionTimeout"
    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(request.correlationId))

    infoLogger("Attempting to handle user session timeout")

    sessionRepository
      .clear(request.userDetails.userId)
      .map { _ =>
        infoLogger("Successfully cleared user session data. Redirecting to 'session timeout' page via sign out flow")

        Redirect(
          url = config.signOutUrl,
          queryStringParams = Map(
            "continue" -> Seq(config.host + routes.SessionTimeoutController.onPageLoad().url),
            "origin" -> Seq(config.appName)
          )
        ).withNewSession
      }
  }
}