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
import controllers.MembersNinoController.viewModel
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import forms.MembersNinoFormProvider
import models.requests.DataRequest
import models.{MemberDetails, MembersNino, Mode}
import navigation.Navigator
import pages.{MembersNinoPage, WhatIsTheMembersNamePage}
import play.api.data.Form
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import services.SessionCacheService
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.DisplayMessage.Message
import viewmodels.models.FormPageViewModel
import views.html.MembersNinoView

import scala.concurrent.{ExecutionContext, Future}

class MembersNinoController @Inject()(
                                       override val messagesApi: MessagesApi,
                                       identify: IdentifierAction,
                                       getData: DataRetrievalAction,
                                       navigator: Navigator,
                                       service: SessionCacheService,
                                       formProvider: MembersNinoFormProvider,
                                       implicit val controllerComponents: MessagesControllerComponents,
                                       view: MembersNinoView
                                     )(implicit ec: ExecutionContext) extends FrontendBaseController with I18nSupport {

  private val form: Form[MembersNino] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      withMemberDetails { memberDetails =>
        request.userAnswers.get(MembersNinoPage) match {
          case None => Future.successful(Ok(view(form, viewModel(mode), memberDetails.fullName)))
          case Some(value) => Future.successful(Ok(view(form.fill(value), viewModel(mode), memberDetails.fullName)))
        }
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      withMemberDetails { memberDetails =>
        form
          .bindFromRequest()
          .fold(
            formWithErrors => {
              Future.successful(BadRequest(view(formWithErrors, viewModel(mode), memberDetails.fullName)))
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

  private def withMemberDetails(f: MemberDetails => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    request.userAnswers.get(WhatIsTheMembersNamePage) match {
      case None =>
        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      case Some(memberDetails) =>
        f(memberDetails)
    }
  }
}

object MembersNinoController {
  def viewModel(mode: Mode): FormPageViewModel[MembersNino] = {
    FormPageViewModel(
      title = Message("membersNino.title"),
      heading = Message("membersNino.heading"),
      page = MembersNino("nino"),
      onSubmit = routes.MembersNinoController.onSubmit(mode),
      backLinkUrl = Some(routes.MembersDobController.onPageLoad(mode).url)
    )

  }
}
