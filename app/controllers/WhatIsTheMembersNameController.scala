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
import models.{MemberDetails, Mode, UserAnswers}
import navigation.Navigator
import pages._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.DisplayMessage.Message
import viewmodels.models.FormPageViewModel
import views.html.WhatIsTheMembersNameView

import scala.concurrent.{ExecutionContext, Future}

class WhatIsTheMembersNameController @Inject()(
                                                override val messagesApi: MessagesApi,
                                                identify: IdentifierAction,
                                                getData: DataRetrievalAction,
                                                navigator: Navigator,
                                                service: SessionCacheService,
                                                val controllerComponents: MessagesControllerComponents,
                                                formProvider: WhatIsTheMembersNameFormProvider,
                                                view: WhatIsTheMembersNameView,
                                              )(implicit ec: ExecutionContext)
  extends FrontendBaseController
    with I18nSupport {

  private val form = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData) {
    implicit request =>
      Ok(view(form, viewModel(mode)))
  }


  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, viewModel(mode)))
            ),
          answer =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.getOrElse(UserAnswers(request.userId)).set(WhatIsTheMembersNamePage, answer))
              _ <- service.save(updatedAnswers)
            } yield Redirect(navigator.nextPage(WhatIsTheMembersNamePage, mode, updatedAnswers)))
  }

}

object WhatIsTheMembersNameController {

  def viewModel(mode: Mode): FormPageViewModel[MemberDetails] = FormPageViewModel(
    Message("memberName.title"),
    Message("memberName.heading"),
    MemberDetails(
      "memberName.firstName",
      "memberName.lastName"
    ),
    routes.WhatIsTheMembersNameController.onSubmit(mode)
  )
}
