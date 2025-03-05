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

import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.requests.DataRequest
import models.{MemberDetails, Mode, NormalMode}
import pages.{MembersDobPage, MembersNinoPage, Page, WhatIsTheMembersNamePage}
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import viewmodels.models.FormPageViewModel

import javax.inject.Inject
import scala.concurrent.Future

abstract class MpeBaseController @Inject()(
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction) extends FrontendBaseController with I18nSupport {

  def handleWithMemberDetails(f: DataRequest[AnyContent] => MemberDetails => Future[Result]): Action[AnyContent] = (identify andThen getData).async {
    implicit request =>
      withMemberDetails { memberDetails =>
        f(request)(memberDetails)
      }
    }

  def handle(f: DataRequest[AnyContent] => Future[Result]): Action[AnyContent] = (identify andThen getData).async {
    implicit request => f(request)
  }

  private def withMemberDetails(f: MemberDetails => Future[Result])(implicit request: DataRequest[_]): Future[Result] = {
    request.userAnswers.get(WhatIsTheMembersNamePage) match {
      case None =>
        Future.successful(Redirect(routes.WhatIsTheMembersNameController.onPageLoad(NormalMode)))
      case Some(memberDetails) =>
        f(memberDetails)
    }
  }

  protected def viewModel(mode: Mode, page: Page): FormPageViewModel =
    FormPageViewModel(
      onSubmit = submitUrl(mode, page),
      backLinkUrl = Some(backLinkUrl(mode, page))
    )

  private def submitUrl(mode: Mode, page: Page): Call = page match {
    case WhatIsTheMembersNamePage => routes.WhatIsTheMembersNameController.onSubmit(mode)
    case MembersDobPage => routes.MembersDobController.onSubmit(mode)
    case MembersNinoPage => routes.MembersNinoController.onPageLoad()
    case _ => routes.CheckMembersProtectionEnhancementsController.onPageLoad()
  }

  private def backLinkUrl(mode: Mode, page: Page): String = page match {
    case WhatIsTheMembersNamePage => routes.CheckMembersProtectionEnhancementsController.onPageLoad().url
    case MembersDobPage => routes.WhatIsTheMembersNameController.onPageLoad(mode).url
    case MembersNinoPage => routes.MembersDobController.onPageLoad(mode).url
    case _ => routes.CheckMembersProtectionEnhancementsController.onPageLoad().url
  }
}
