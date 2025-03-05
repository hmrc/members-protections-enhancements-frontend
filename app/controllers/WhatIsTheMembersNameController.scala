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
import forms.WhatIsTheMembersNameFormProvider
import models.{MemberDetails, Mode}
import navigation.Navigator
import pages._
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
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
  extends MpeBaseController(identify, getData) {

  private val form: Form[MemberDetails] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = handle {
    implicit request =>
      val namesForm = request.userAnswers.get(WhatIsTheMembersNamePage) match {
        case None        => form
        case Some(value) => form.fill(value)
      }
      Future.successful(Ok(view(namesForm, viewModel(mode, WhatIsTheMembersNamePage))))
  }


  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors =>
            Future.successful(BadRequest(view(formWithErrors, viewModel(mode, WhatIsTheMembersNamePage)))
            ),
          answer =>
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(WhatIsTheMembersNamePage, answer))
              _ <- service.save(updatedAnswers)
            } yield Redirect(navigator.nextPage(WhatIsTheMembersNamePage, mode, updatedAnswers)))
  }

}
