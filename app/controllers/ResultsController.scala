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
import models.{MemberDetails, MembersDob, MembersNino, MembersPsaCheckRef}
import pages.{MembersDobPage, MembersNinoPage, MembersPsaCheckRefPage, WhatIsTheMembersNamePage}
import play.api.i18n.{Messages, MessagesApi}
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow
import viewmodels.checkYourAnswers.ResultsSummary._
import views.html.ResultsView

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import scala.concurrent.Future

class ResultsController @Inject()(
                                   override val messagesApi: MessagesApi,
                                   identify: IdentifierAction,
                                   getData: DataRetrievalAction,
                                   val controllerComponents: MessagesControllerComponents,
                                   view: ResultsView
                                 ) extends MpeBaseController(identify, getData) {

  def onPageLoad(): Action[AnyContent] = handle {
    implicit request =>

      (for {
        memberDetails <- request.userAnswers.get(WhatIsTheMembersNamePage)
        dob <- request.userAnswers.get(MembersDobPage)
        nino <- request.userAnswers.get(MembersNinoPage)
        psaRefCheck <- request.userAnswers.get(MembersPsaCheckRefPage)
      } yield Future.successful(Ok(
        view(resultsTable(memberDetails, dob, nino, psaRefCheck), Some(routes.CheckYourAnswersController.onPageLoad().url), getFormattedTimestamp)
      )
      )).getOrElse(Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad())))
  }

  private def resultsTable(memberDetails: MemberDetails, membersDob: MembersDob, membersNino: MembersNino,
                           membersPsaCheckRef: MembersPsaCheckRef)(implicit messages: Messages): Seq[Seq[TableRow]] = {
    List(
      membersNameRow(memberDetails),
      membersDobRow(membersDob),
      membersNinoRow(membersNino),
      membersPsaCheckRefRow(membersPsaCheckRef)
    )
  }

  private def getFormattedTimestamp: String = {
    val timeStamp = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm")
    timeStamp.format(formatter)
  }
}
