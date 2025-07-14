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
import controllers.actions.{CheckLockoutAction, DataRetrievalAction, IdentifierAction}
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import providers.DateTimeProvider
import services.{FailedAttemptService, MembersCheckAndRetrieveService}
import utils.DateTimeFormats
import views.html.ResultsView

import scala.concurrent.{ExecutionContext, Future}
import scala.language.implicitConversions

class ResultsController @Inject()(override val messagesApi: MessagesApi,
                                  identify: IdentifierAction,
                                  checkLockout: CheckLockoutAction,
                                  getData: DataRetrievalAction,
                                  val controllerComponents: MessagesControllerComponents,
                                  view: ResultsView,
                                  dateTimeProvider: DateTimeProvider,
                                  checkAndRetrieveService: MembersCheckAndRetrieveService,
                                  failedAttemptService: FailedAttemptService)
                                 (implicit ec: ExecutionContext)
  extends MpeBaseController(identify, checkLockout, getData) with Logging {

  val classLoggingContext: String = "ResultsController"

  def onPageLoad(): Action[AnyContent] = handle(implicit request => {
    val methodLoggingContext: String = "onPageLoad"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    getUserData(request) match {
      case Some((memberDetails, membersDob, membersNino, membersPsaCheckRef)) =>
        checkAndRetrieveService.checkAndRetrieve(retrieveMembersRequest(memberDetails, membersDob, membersNino, membersPsaCheckRef)).flatMap {
          case Right(value) =>
            logger.info(s"$fullLoggingContext - Successfully retrieved results for supplied details")
            Future.successful(Ok(
              view(
                memberDetails = memberDetails,
                membersDob = membersDob,
                membersNino = membersNino,
                membersPsaCheckRef = membersPsaCheckRef,
                backLinkUrl = Some(routes.CheckYourAnswersController.onPageLoad().url),
                formattedTimestamp = DateTimeFormats.getCurrentDateTimestamp(dateTimeProvider.now()),
                protectionRecordDetails = value
              )
            ))
          case _ =>
            implicit val req: IdentifierRequest[AnyContent] = request.toIdentifierRequest
            logger.warn(s"$fullLoggingContext - Failed to retrieve results for supplied details")

            failedAttemptService.handleFailedAttempt(
              Redirect(routes.LockedOutController.onPageLoad())
            )(
              Redirect(routes.NoResultsController.onPageLoad())
            )

        }
      case _ =>
        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
    }
  })
}
