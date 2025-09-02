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

import controllers.actions.{DataRetrievalAction, IdentifierAction}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.binders.RedirectUrl._
import uk.gov.hmrc.play.bootstrap.binders._
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.{JourneyRecoveryContinueView, JourneyRecoveryStartAgainView}

import javax.inject.Inject

class JourneyRecoveryController @Inject()(val controllerComponents: MessagesControllerComponents,
                                          identify: IdentifierAction,
                                          getData: DataRetrievalAction,
                                          continueView: JourneyRecoveryContinueView,
                                          startAgainView: JourneyRecoveryStartAgainView)
  extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(continueUrl: Option[RedirectUrl] = None): Action[AnyContent] =
    (identify andThen getData) { implicit request =>
      val methodLoggingContext: String = "onPageLoad"
      val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(request.correlationId))

      val warnLogger: (String, Option[Throwable]) => Unit = warnLog(
        secondaryContext = methodLoggingContext,
        dataLog = correlationIdLogString(request.correlationId)
      )

      infoLogger("Attempting to recover journey")

      continueUrl.fold{
        infoLogger("No continue URL was provided. Attempting to serve 'start again' view")
        Ok(startAgainView())
      }{ url =>
        url.getEither(OnlyRelative) match {
          case Left(message) =>
            warnLogger(
              s"Continue URL failed to validate with error message: $message. Attempting to serve 'start again' view",
              None
            )
            Ok(startAgainView())
          case Right(safeUrl) =>
            infoLogger("Supplied continue URL validated successfully. Attempting to serve 'continue' view")
            Ok(continueView(safeUrl.url))
        }
      }
    }
}
