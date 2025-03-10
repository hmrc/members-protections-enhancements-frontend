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
import forms.MembersPsaCheckRefFormProvider
import models.{MembersPsaCheckRef, Mode}
import navigation.Navigator
import pages.MembersPsaCheckRefPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import views.html.MembersPsaCheckRefView

import scala.concurrent.{ExecutionContext, Future}

class MembersPsaCheckRefController @Inject()(
                                              override val messagesApi: MessagesApi,
                                              identify: IdentifierAction,
                                              getData: DataRetrievalAction,
                                              navigator: Navigator,
                                              service: SessionCacheService,
                                              formProvider: MembersPsaCheckRefFormProvider,
                                              implicit val controllerComponents: MessagesControllerComponents,
                                              view: MembersPsaCheckRefView
                                            )(implicit ec: ExecutionContext) extends MpeBaseController(identify, getData) {

  private val form: Form[MembersPsaCheckRef] = formProvider()


  def onPageLoad(mode: Mode): Action[AnyContent] = handleWithMemberDetails {
    implicit request =>
      membersDetails =>
        request.userAnswers.get(MembersPsaCheckRefPage) match {
          case None => Future.successful(Ok(view(form, viewModel(mode, MembersPsaCheckRefPage), membersDetails.fullName)))
          case Some(value) => Future.successful(Ok(view(form.fill(value), viewModel(mode, MembersPsaCheckRefPage), membersDetails.fullName)))
        }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = handleWithMemberDetails {
    implicit request =>
      memberDetails =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future.successful(BadRequest(view(formWithErrors, viewModel(mode, MembersPsaCheckRefPage), memberDetails.fullName)))
            },
            answer => {
              for {
                updatedAnswers <- Future.fromTry(request.userAnswers.set(MembersPsaCheckRefPage, answer))
                _ <- service.save(updatedAnswers)
              } yield {
                Redirect(navigator.nextPage(MembersPsaCheckRefPage, mode, updatedAnswers))
              }
            }
          )
  }
}
