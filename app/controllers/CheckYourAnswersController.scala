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
import models._
import pages._
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.SummaryListRow
import viewmodels.checkYourAnswers.CheckYourAnswersSummary._
import views.html.CheckYourAnswersView

import scala.concurrent.Future

class CheckYourAnswersController @Inject()(
                                            override val messagesApi: MessagesApi,
                                            identify: IdentifierAction,
                                            getData: DataRetrievalAction,
                                            implicit val controllerComponents: MessagesControllerComponents,
                                            view: CheckYourAnswersView
                                          ) extends MpeBaseController(identify, getData) {

  def onPageLoad(): Action[AnyContent] = handle {
    implicit request =>
      (for {
        memberDetails <- request.userAnswers.get(WhatIsTheMembersNamePage)
        dob <- request.userAnswers.get(MembersDobPage)
        nino <- request.userAnswers.get(MembersNinoPage)
        psaRefCheck <- request.userAnswers.get(MembersPsaCheckRefPage)
      } yield Future.successful(Ok(
        view(rows(memberDetails, dob, nino, psaRefCheck), Some(routes.MembersPsaCheckRefController.onPageLoad(NormalMode).url))
      )
      )).getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
  }

  private def rows(memberDetails: MemberDetails, membersDob: MembersDob, membersNino: MembersNino,
                   membersPsaCheckRef: MembersPsaCheckRef)(implicit messages: Messages): Seq[SummaryListRow] = {
    List(
      membersFirstNameRow(memberDetails),
      membersLastNameRow(memberDetails),
      membersDobRow(membersDob),
      membersNinoRow(membersNino),
      membersPsaCheckRefRow(membersPsaCheckRef)
    )
  }
}
