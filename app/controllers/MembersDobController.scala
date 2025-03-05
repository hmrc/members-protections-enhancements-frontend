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
import forms.MembersDobFormProvider
import models.{MembersDob, Mode}
import navigation.Navigator
import pages.MembersDobPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc._
import services.SessionCacheService
import views.html.MembersDobView

import scala.concurrent.{ExecutionContext, Future}

class MembersDobController @Inject()(
                                      override val messagesApi: MessagesApi,
                                      identify: IdentifierAction,
                                      getData: DataRetrievalAction,
                                      navigator: Navigator,
                                      service: SessionCacheService,
                                      formProvider: MembersDobFormProvider,
                                      implicit val controllerComponents: MessagesControllerComponents,
                                      view: MembersDobView
                                    )(implicit ec: ExecutionContext) extends MpeBaseController(identify, getData) {

  private val form: Form[MembersDob] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = handleWithMemberDetails {
    implicit request =>
      memberDetails =>
        request.userAnswers.get(MembersDobPage) match {
          case None => Future.successful(Ok(view(form, viewModel(mode, MembersDobPage), memberDetails.fullName)))
          case Some(value) => Future.successful(Ok(view(form.fill(value), viewModel(mode, MembersDobPage), memberDetails.fullName)))
        }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = handleWithMemberDetails {
    implicit request =>
      memberDetails =>
        form.bindFromRequest().fold(
          formWithErrors => {
            Future.successful(BadRequest(view(formWithErrors, viewModel(mode, MembersDobPage), memberDetails.fullName)))
          },
          answer => {
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(MembersDobPage, answer))
              _ <- service.save(updatedAnswers)
            } yield {
              Redirect(navigator.nextPage(MembersDobPage, mode, updatedAnswers))
            }
          }
        )
  }
}
