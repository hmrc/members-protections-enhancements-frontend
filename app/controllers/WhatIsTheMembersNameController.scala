/*
 * Copyright 2024 HM Revenue & Customs
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
import controllers.WhatIsTheMembersNameController.viewModel
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.WhatIsTheMembersNameFormProvider
import models.Mode
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.DisplayMessage.Message
import viewmodels.models.{FormPageViewModel, NameViewModel}
import views.html.WhatIsTheMembersNameView

import scala.concurrent.{ExecutionContext, Future}

class WhatIsTheMembersNameController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                val controllerComponents: MessagesControllerComponents,
                                                formProvider: WhatIsTheMembersNameFormProvider,
                                                view: WhatIsTheMembersNameView,
                                              )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      val form = formProvider()
      Ok(view(form, viewModel(mode)))
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>

      Future.successful(Ok)
  }

}

object WhatIsTheMembersNameController {

  def viewModel(mode: Mode): FormPageViewModel[NameViewModel] = FormPageViewModel(
    Message("member.name.title"),
    Message("member.name.heading"),
    NameViewModel(
      "member.name.firstName",
      "member.name.lastName"
    ),
    routes.WhatIsTheMembersNameController.onSubmit()
  )
}
