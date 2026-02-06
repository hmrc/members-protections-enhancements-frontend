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
import models.requests.{DataRequest, PensionSchemeMemberRequest}
import pages.*
import play.api.i18n.I18nSupport
import play.api.libs.json.Reads
import play.api.mvc.{Action, AnyContent, Call, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.formPage.FormPageViewModel

import javax.inject.Inject
import scala.concurrent.Future

abstract class MpeBaseController @Inject()(identify: IdentifierAction,
                                           checkLockout: CheckLockoutAction,
                                           getData: DataRetrievalAction) extends FrontendBaseController with I18nSupport with Logging {

  def handle(block: DataRequest[AnyContent] => Future[Result]): Action[AnyContent] =
    (identify andThen checkLockout andThen getData).async { implicit request =>
      isResultsSuccessful(block(_))
    }

  private def withDetail[D: Reads](questionPage: QuestionPage[D],
                                   failureRedirect: Call,
                                   block: D => Future[Result])
                                  (implicit request: DataRequest[_]) = {
    request.userAnswers.get[D](questionPage) match {
      case None =>
        Future.successful(Redirect(failureRedirect))
      case Some(value) =>
        block(value)
    }
  }

  def handleWithMemberDetails(block: DataRequest[AnyContent] => MemberDetails => Future[Result]): Action[AnyContent] =
    handle { implicit request =>
      withDetail(
        questionPage = WhatIsTheMembersNamePage,
        failureRedirect = routes.WhatIsTheMembersNameController.onPageLoad(NormalMode),
        block = block(request)
      )
    }

  private type WithDetailsAndDob = MemberDetails => MembersDob => Future[Result]

  def handleWithMemberDob(block: DataRequest[AnyContent] => WithDetailsAndDob): Action[AnyContent] =
    handleWithMemberDetails { implicit request =>
      details => withDetail(
        questionPage = MembersDobPage,
        failureRedirect = routes.MembersDobController.onPageLoad(NormalMode),
        block = block(request)(details)
      )
    }

  private type WithDetailsDobAndNino = MemberDetails => MembersDob => MembersNino => Future[Result]

  def handleWithMemberNino(block: DataRequest[AnyContent] => WithDetailsDobAndNino): Action[AnyContent] =
    handleWithMemberDob { implicit request =>
      details => dob => withDetail(
        questionPage = MembersNinoPage,
        failureRedirect = routes.MembersNinoController.onPageLoad(NormalMode),
        block = block(request)(details)(dob)
      )
    }

  private type WithAllDetails = MemberDetails => MembersDob => MembersNino => MembersPsaCheckRef => Future[Result]
  
  def handleWithAllDetails(block: DataRequest[AnyContent] => WithAllDetails): Action[AnyContent] =
    handleWithMemberNino { implicit request =>
      details => dob => nino => withDetail(
        questionPage = MembersPsaCheckRefPage,
        failureRedirect = routes.MembersPsaCheckRefController.onPageLoad(NormalMode),
        block = block(request)(details)(dob)(nino)
      )
    }
    
  private type WithCheckedAnswers = MemberDetails => MembersDob => MembersNino => MembersPsaCheckRef => CheckMembersDetails => Future[Result]

  def handleWithCheckedAnswers(block: DataRequest[AnyContent] => WithCheckedAnswers): Action[AnyContent] =
    handleWithAllDetails { implicit request =>
      details => dob => nino => psacr => {
        def redirectCall: Call = routes.CheckYourAnswersController.onPageLoad()
        
        withDetail(
          questionPage = CheckYourAnswersPage,
          failureRedirect = redirectCall,
          block = (cya: CheckMembersDetails) => if(cya.isChecked) {
            block(request)(details)(dob)(nino)(psacr)(cya)
          } else {
            Future.successful(Redirect(redirectCall))
          }
        )
      }
    }

  private def isResultsSuccessful(block: DataRequest[AnyContent] => Future[Result])(implicit request: DataRequest[AnyContent]): Future[Result] = {
    request.userAnswers.get(ResultsPage) match {
      case Some(_) =>
        Future.successful(Redirect(routes.ClearCacheController.onPageLoad()))
      case None => block(request)
    }
  }

  protected def viewModel(mode: Mode, page: Page): FormPageViewModel =
    FormPageViewModel(
      onSubmit = submitUrl(mode, page),
      backLinkUrl = Some(backLinkUrl(mode, page))
    )

  protected def submitUrl(mode: Mode, page: Page): Call = page match {
    case WhatIsTheMembersNamePage => routes.WhatIsTheMembersNameController.onSubmit(mode)
    case MembersDobPage => routes.MembersDobController.onSubmit(mode)
    case MembersNinoPage => routes.MembersNinoController.onSubmit(mode)
    case MembersPsaCheckRefPage => routes.MembersPsaCheckRefController.onSubmit(mode)
    case _ => routes.ResultsController.onPageLoad()
  }

  private def backLinkUrl(mode: Mode, page: Page): String = page match {
    case MembersDobPage => routes.WhatIsTheMembersNameController.onPageLoad(mode).url
    case MembersNinoPage => routes.MembersDobController.onPageLoad(mode).url
    case MembersPsaCheckRefPage => routes.MembersNinoController.onPageLoad(mode).url
    case _ => routes.WhatYouWillNeedController.onPageLoad().url
  }

  def retrieveMembersRequest(memberDetails: MemberDetails,
                             membersDob:MembersDob,
                             membersNino:MembersNino,
                             membersPsaCheckRef: MembersPsaCheckRef): PensionSchemeMemberRequest =
      PensionSchemeMemberRequest(memberDetails.firstName,
        memberDetails.lastName,
        membersDob.strDateOfBirth,
        membersNino.nino.filterNot(_.isWhitespace),
        membersPsaCheckRef.psaCheckRef.filterNot(_.isWhitespace))

}
