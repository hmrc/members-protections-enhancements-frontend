/*
 * Copyright 2026 HM Revenue & Customs
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
import models.*
import models.requests.DataRequest
import models.userAnswers.UserAnswers
import navigation.Navigator
import pages.*
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.formPage.FormPageViewModel

import javax.inject.Inject
import scala.concurrent.Future

abstract class MpeBaseController @Inject() (
  identify: IdentifierAction,
  checkLockout: CheckLockoutAction,
  getData: DataRetrievalAction
) extends FrontendBaseController
    with I18nSupport
    with Logging {

  protected def authRetrieval(block: DataRequest[AnyContent] => Future[Result]): Action[AnyContent] = {
    def isResultsSuccessful(
      block: DataRequest[AnyContent] => Future[Result]
    )(implicit request: DataRequest[AnyContent]): Future[Result] =
      request.userAnswers.get(ResultsPage) match {
        case Some(_) =>
          Future.successful(Redirect(routes.ClearCacheController.onPageLoad()))
        case None => block(request)
      }
    identify.andThen(checkLockout).andThen(getData).async { implicit request =>
      isResultsSuccessful(block(_))
    }
  }

  protected def withName(block: String => Future[Result])(implicit request: DataRequest[AnyContent]): Future[Result] =
    request.userAnswers
      .get(WhatIsTheMembersNamePage)
      .map(_.fullName)
      .fold(Future.successful(Redirect(routes.WhatIsTheMembersNameController.onPageLoad(NormalMode))))(block)

  protected def withPreviousPageCheck(page: Page, mode: Mode)(
    block: => Future[Result]
  )(implicit request: DataRequest[AnyContent]): Future[Result] =
    Navigator.firstPreviousPageWithNoData(page, mode, request.userAnswers) match {
      case Some(call) => Future.successful(Redirect(call))
      case _ => block
    }

  protected def withPreviousPageCheckAndName(page: Page, mode: Mode)(
    block: String => Future[Result]
  )(implicit request: DataRequest[AnyContent]): Future[Result] =
    Navigator.firstPreviousPageWithNoData(page, mode, request.userAnswers) match {
      case Some(call) => Future.successful(Redirect(call))
      case _ =>
        block(request.userAnswers.getOrException(WhatIsTheMembersNamePage).fullName)
    }

  protected def withCheckedAnswers(request: DataRequest[AnyContent])(
    block: (
      MemberDetails,
      MembersDob,
      MembersNino,
      MembersPsaCheckRef
    ) => Future[Result]
  ): Future[Result] =
    (
      request.userAnswers.get(WhatIsTheMembersNamePage),
      request.userAnswers.get(MembersDobPage),
      request.userAnswers.get(MembersNinoPage),
      request.userAnswers.get(MembersPsaCheckRefPage),
      request.userAnswers.get(CheckYourAnswersPage)
    ) match {
      case (Some(details), Some(dob), Some(nino), Some(psacr), None) =>
        Future.successful(Redirect(routes.CheckYourAnswersController.onPageLoad()))
      case (Some(details), Some(dob), Some(nino), Some(psacr), Some(cya)) =>
        if (cya.isChecked) {
          block(details, dob, nino, psacr)
        } else {
          Future.successful(Redirect(routes.CheckYourAnswersController.onPageLoad()))
        }
      case _ => Future.successful(Redirect(routes.WhatIsTheMembersNameController.onPageLoad(NormalMode)))
    }

  protected def viewModel(page: Page, mode: Mode, userAnswers: UserAnswers): FormPageViewModel =
    FormPageViewModel(
      onSubmit = Navigator.submitUrl(page, mode, userAnswers),
      backLinkUrl = Some(Navigator.backLinkUrl(mode, page))
    )
}
