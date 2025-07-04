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
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import providers.DateTimeProvider
import services.MembersCheckAndRetrieveService
import utils.DateTimeFormats
import views.html.ResultsView

import scala.concurrent.{ExecutionContext, Future}

class ResultsController @Inject()(override val messagesApi: MessagesApi,
                                  identify: IdentifierAction,
                                  getData: DataRetrievalAction,
                                  val controllerComponents: MessagesControllerComponents,
                                  view: ResultsView,
                                  dateTimeProvider: DateTimeProvider,
                                  checkAndRetrieveService: MembersCheckAndRetrieveService)
                                 (implicit ec: ExecutionContext) extends MpeBaseController(identify, getData) {

  def onPageLoad(): Action[AnyContent] = handle {
    implicit request =>
      getUserData(request) match {
        case Some((memberDetails, membersDob, membersNino, membersPsaCheckRef)) =>
          checkAndRetrieveService.checkAndRetrieve(retrieveMembersRequest(memberDetails, membersDob, membersNino, membersPsaCheckRef)).map {
            case Right(value) => Ok(
              view(
                memberDetails = memberDetails,
                membersDob = membersDob,
                membersNino = membersNino,
                membersPsaCheckRef = membersPsaCheckRef,
                backLinkUrl = Some(routes.CheckYourAnswersController.onPageLoad().url),
                formattedTimestamp = DateTimeFormats.getCurrentDateTimestamp(dateTimeProvider.now()),
                protectionRecordDetails = value
              )
            )
            case _ =>
              Redirect(routes.NoResultsController.onPageLoad())
          }
        case _ =>
          Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      }
  }
}
