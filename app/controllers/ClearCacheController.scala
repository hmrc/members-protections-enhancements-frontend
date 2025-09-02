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

import controllers.actions.{CheckLockoutAction, DataRetrievalAction, IdentifierAction}
import play.api.i18n.{I18nSupport, MessagesApi}
import models.CorrelationId
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import views.html.ErrorTemplate

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class ClearCacheController @Inject()(override val messagesApi: MessagesApi,
                                     val controllerComponents: MessagesControllerComponents,
                                     identify: IdentifierAction,
                                     checkLockout: CheckLockoutAction,
                                     getData: DataRetrievalAction,
                                     sessionCacheService: SessionCacheService,
                                     view: ErrorTemplate)(implicit ec: ExecutionContext)
  extends FrontendBaseController with I18nSupport with Logging {

  def onPageLoad(): Action[AnyContent] = (identify andThen checkLockout andThen getData).async { implicit request =>
    val methodLoggingContext: String = "onPageLoad"
    implicit val correlationId: CorrelationId = request.correlationId
    val infoLogger = infoLog(methodLoggingContext, correlationIdLogString(correlationId))

    infoLogger("Attempting to clear user session data, and redirect to start of journey")

    sessionCacheService
      .clear(request.userAnswers)
      .map {
        _ =>
          infoLogger("Successfully cleared user session data. Redirecting to start of journey")
          Redirect(routes.WhatYouWillNeedController.onPageLoad().url)
      }
  }

  def defaultError(): Action[AnyContent] = (identify andThen checkLockout andThen getData).async { implicit request =>
    val methodLoggingContext: String = "defaultError"
    implicit val correlationId: CorrelationId = request.correlationId
    val infoLogger = infoLog(methodLoggingContext, correlationIdLogString(correlationId))

    infoLogger("Attempting to clear user session data, and serve 'unauthorised' view")

    sessionCacheService
      .clear(request.userAnswers)
      .map { _ =>
        infoLogger("Successfully cleared user session data. Attempting to serve 'unauthorised' view")
        Ok(view(heading = request.messages(messagesApi).messages("journeyRecovery.startAgain.heading")))
      }
  }
}
