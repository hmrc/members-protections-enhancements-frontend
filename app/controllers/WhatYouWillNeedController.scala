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

import com.google.inject.Inject
import controllers.actions.{CheckLockoutAction, DataRetrievalAction, IdentifierAction}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.Logging
import views.html.WhatYouWillNeedView

import scala.concurrent.Future

class WhatYouWillNeedController @Inject()(override val messagesApi: MessagesApi,
                                          identify: IdentifierAction,
                                          checkLockout: CheckLockoutAction,
                                          getData: DataRetrievalAction,
                                          val controllerComponents: MessagesControllerComponents,
                                          view: WhatYouWillNeedView)
  extends MpeBaseController(identify, checkLockout, getData) with Logging {

  def start(): Action[AnyContent] = Action.async { request =>
    val methodLoggingContext: String = "start"
    val correlationId = request.headers.get("correlationId").getOrElse("N/A")

    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(correlationId))

    infoLogger("Attempting to redirect to start page")

    Future.successful(
      Redirect(controllers.routes.WhatYouWillNeedController.onPageLoad())
    )
  }

  def onPageLoad(): Action[AnyContent] = handle("onPageLoad") { implicit request =>
    val methodLoggingContext: String = "start"
    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(request.correlationId))

    infoLogger("Attempting to serve 'what you will need' view")

    Future.successful(Ok(view(Some(routes.MpsDashboardController.redirectToMps().url))))
  }
}
