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

import controllers.actions.{AllowListAction, CheckLockoutAction, DataRetrievalAction, IdentifierAction}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import repositories.SessionRepository
import utils.IdGenerator

import javax.inject.Inject
import scala.concurrent.ExecutionContext

class KeepAliveController @Inject()(val controllerComponents: MessagesControllerComponents,
                                    identify: IdentifierAction,
                                    allowListAction: AllowListAction,
                                    checkLockout: CheckLockoutAction,
                                    getData: DataRetrievalAction,
                                    sessionRepository: SessionRepository,
                                    val idGenerator: IdGenerator)(implicit ec: ExecutionContext)
  extends MpeBaseController(identify, allowListAction, checkLockout, getData) {

  def keepAlive(): Action[AnyContent] = handle { implicit request =>
    logInfo("CheckYourAnswersController", "onPageLoad", request.correlationId)
    sessionRepository.keepAlive(request.userAnswers.id).map(_ => Ok)
  }
}
