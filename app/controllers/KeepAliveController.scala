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
import models.CorrelationId
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import utils.NewLogging

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class KeepAliveController @Inject()(val controllerComponents: MessagesControllerComponents,
                                    identify: IdentifierAction,
                                    checkLockout: CheckLockoutAction,
                                    getData: DataRetrievalAction,
                                    sessionRepository: SessionRepository)(implicit ec: ExecutionContext)
  extends MpeBaseController(identify, checkLockout, getData) with NewLogging {

  def keepAlive(): Action[AnyContent] = handle("keepAlive") { implicit request =>
    val methodLoggingContext: String = "keepAlive"
    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(request.correlationId))

    infoLogger("Attempting to keep user session alive")

    sessionRepository.keepAlive(request.userAnswers.id).map(_ => {
      infoLogger("Successfully updated user session expiry. Returning success status")
      Ok
    })
  }
}
