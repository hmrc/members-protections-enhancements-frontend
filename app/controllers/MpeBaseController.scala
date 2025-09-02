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

import controllers.actions.{CheckLockoutAction, DataRetrievalAction, IdentifierAction}
import models._
import models.requests.{DataRequest, PensionSchemeMemberRequest}
import pages._
import play.api.i18n.I18nSupport
import play.api.mvc.{Action, AnyContent, Call, Result}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.Logging
import viewmodels.formPage.FormPageViewModel

import javax.inject.Inject
import scala.concurrent.Future

abstract class MpeBaseController @Inject()(identify: IdentifierAction,
                                           checkLockout: CheckLockoutAction,
                                           getData: DataRetrievalAction)
  extends FrontendBaseController with I18nSupport {_: Logging =>

  def handleWithMemberDetails(context: String)(block: DataRequest[AnyContent] => MemberDetails => Future[Result]): Action[AnyContent] =
    handle(context) { implicit request =>
      val methodLoggingContext: String = "handleWithMemberDetails"

      val infoLogger: String => Unit = infoLog(
        secondaryContext = methodLoggingContext,
        dataLog = correlationIdLogString(request.correlationId),
        extraContext = Some(context)
      )

      val warnLogger: (String, Option[Throwable]) => Unit = warnLog(
        secondaryContext = methodLoggingContext,
        dataLog = correlationIdLogString(request.correlationId),
        extraContext = Some(context)
      )

      infoLogger("Checking if user session has existing member details")

      request.userAnswers.get(WhatIsTheMembersNamePage) match {
        case None =>
          warnLogger("No member details exist for the user session. Redirecting to start page", None)
          Future.successful(Redirect(routes.WhatIsTheMembersNameController.onPageLoad(NormalMode)))
        case Some(memberDetails) =>
          infoLogger("Member details already exist for the user session. Continuing with block action")
          block(request)(memberDetails)
      }
    }

  def handle(context: String)(block: DataRequest[AnyContent] => Future[Result]): Action[AnyContent] = {
    val methodLoggingContext: String = "handle"
    logger.info(
      secondaryContext = methodLoggingContext,
      message = "Attempting to handle request. Carrying out pre-requisite checks",
      extraContext = Some(context)
    )
    (identify andThen checkLockout andThen getData).async { implicit request =>
      val infoLogger: String => Unit = infoLog(
        secondaryContext = methodLoggingContext,
        dataLog = correlationIdLogString(request.correlationId),
        extraContext = Some(context)
      )

      val warnLogger: (String, Option[Throwable]) => Unit = warnLog(
        secondaryContext = methodLoggingContext,
        dataLog = correlationIdLogString(request.correlationId),
        extraContext = Some(context)
      )

      infoLogger("Request successfully completed all pre-requisite checks. Checking if previous result exists")

      request.userAnswers.get(ResultsPage) match {
        case Some(_) =>
          warnLogger("User session already has results. Redirecting to clear cache and restart journey", None)
          Future.successful(Redirect(routes.ClearCacheController.onPageLoad()))
        case None =>
          infoLogger("No previous result exists. Continuing with block action")
          block(request)
      }
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

  def getUserData[A](request: DataRequest[A]): Option[(MemberDetails, MembersDob, MembersNino, MembersPsaCheckRef)] =
    for {
      memberDetails <- request.userAnswers.get(WhatIsTheMembersNamePage)
      dob <- request.userAnswers.get(MembersDobPage)
      nino <- request.userAnswers.get(MembersNinoPage)
      psaRefCheck <- request.userAnswers.get(MembersPsaCheckRefPage)
    } yield (memberDetails, dob, nino, psaRefCheck)

  def retrieveMembersRequest(memberDetails: MemberDetails,
                             membersDob:MembersDob,
                             membersNino:MembersNino,
                             membersPsaCheckRef: MembersPsaCheckRef): PensionSchemeMemberRequest =
      PensionSchemeMemberRequest(
        firstName = memberDetails.firstName,
        lastName = memberDetails.lastName,
        dateOfBirth = membersDob.strDateOfBirth,
        nino = membersNino.nino.filterNot(_.isWhitespace),
        psaCheckRef = membersPsaCheckRef.psaCheckRef.filterNot(_.isWhitespace)
      )

}
