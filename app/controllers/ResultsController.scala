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
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import views.html.ResultsView

class ResultsController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              val controllerComponents: MessagesControllerComponents,
                                              view: ResultsView
                                            ) extends FrontendBaseController with I18nSupport {

  //TODO This controller and test is created for navigation purposes. Once this ticket is build, Need to add the functionality for it.

  def onPageLoad(): Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      Ok(view(Some(routes.CheckYourAnswersController.onPageLoad().url)))
  }
}
