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
import forms.MembersNinoFormProvider
import models.{MembersNino, Mode}
import navigation.Navigator
import pages.MembersNinoPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import views.html.MembersNinoView

import scala.concurrent.{ExecutionContext, Future}

class MembersNinoController @Inject()(override val messagesApi: MessagesApi,
                                      identify: IdentifierAction,
                                      checkLockout: CheckLockoutAction,
                                      getData: DataRetrievalAction,
                                      navigator: Navigator,
                                      service: SessionCacheService,
                                      formProvider: MembersNinoFormProvider,
                                      implicit val controllerComponents: MessagesControllerComponents,
                                      view: MembersNinoView)(implicit ec: ExecutionContext)
  extends MpeBaseController(identify, checkLockout, getData) {

  private val form: Form[MembersNino] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = handleWithMemberDob {
    implicit request =>

      memberDetails => _ =>
        request.userAnswers.get(MembersNinoPage) match {
          case None => Future.successful(Ok(view(form, viewModel(mode, MembersNinoPage), memberDetails.fullName)))
          case Some(value) => Future.successful(Ok(view(form.fill(value), viewModel(mode, MembersNinoPage), memberDetails.fullName)))
        }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = handleWithMemberDetails {
    implicit request =>
      memberDetails =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future.successful(BadRequest(view(formWithErrors, viewModel(mode, MembersNinoPage), memberDetails.fullName)))
            },
            answer => {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(MembersNinoPage, answer))
                _ <- service.save(updatedAnswers)
              } yield {
                Redirect(navigator.nextPage(MembersNinoPage, mode, updatedAnswers))
              }
            }
          )
  }

}
