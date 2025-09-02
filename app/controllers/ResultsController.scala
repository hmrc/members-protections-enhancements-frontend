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
import models.audit.{AuditDetail, AuditEvent}
import models.errors.{ErrorWrapper, MatchPerson, MpeError}
import models.requests.{DataRequest, PensionSchemeMemberRequest, UserDetails}
import models.{CorrelationId, MembersResult}
import pages.ResultsPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents, Result}
import providers.DateTimeProvider
import services.{AuditService, FailedAttemptService, MembersCheckAndRetrieveService, SessionCacheService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import utils.{DateTimeFormats, Logging}
import views.html.ResultsView

import scala.concurrent.{ExecutionContext, Future}

class ResultsController @Inject()(override val messagesApi: MessagesApi,
                                  identify: IdentifierAction,
                                  checkLockout: CheckLockoutAction,
                                  getData: DataRetrievalAction,
                                  val controllerComponents: MessagesControllerComponents,
                                  view: ResultsView,
                                  dateTimeProvider: DateTimeProvider,
                                  checkAndRetrieveService: MembersCheckAndRetrieveService,
                                  failedAttemptService: FailedAttemptService,
                                  service: SessionCacheService,
                                  auditService: AuditService)
                                 (implicit ec: ExecutionContext)
  extends MpeBaseController(identify, checkLockout, getData) with Logging {

  def onPageLoad(): Action[AnyContent] = handle("onPageLoad"){ implicit request =>
    val methodLoggingContext: String = "onPageLoad"
    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(request.correlationId))

    infoLogger("Attempting to check for existing user answers to all questions")

    getUserData(request) match {
      case Some((memberDetails, membersDob, membersNino, membersPsaCheckRef)) =>
        infoLogger("Existing answers found. Attempting to match member and retrieve protection record details")

        val pensionSchemeMemberRequest = retrieveMembersRequest(
          memberDetails = memberDetails,
          membersDob = membersDob,
          membersNino = membersNino,
          membersPsaCheckRef = membersPsaCheckRef
        )

        val auditDetail = generateAuditDetail(pensionSchemeMemberRequest, request.userDetails)

        implicit val correlationId: CorrelationId = request.correlationId

        checkAndRetrieveService.checkAndRetrieve(pensionSchemeMemberRequest).flatMap {
          case Right(value) => handleSuccess(
            context = methodLoggingContext,
            auditDetail = auditDetail,
            resultBuilder = _ => Ok(view(
              memberDetails = memberDetails,
              membersDob = membersDob,
              membersNino = membersNino,
              membersPsaCheckRef = membersPsaCheckRef,
              backLinkUrl = Some(routes.CheckYourAnswersController.onPageLoad().url),
              formattedTimestamp = DateTimeFormats.getCurrentDateTimestamp(dateTimeProvider.now()),
              protectionRecordDetails = value.responseData
            ))
          )
          case Left(value) => handleError(value, auditDetail, methodLoggingContext)
        }
      case _ =>
        Future.successful(Redirect(routes.ClearCacheController.onPageLoad()))
    }
  }

  private def handleSuccess[A](context: String,
                               auditDetail: AuditDetail,
                               resultBuilder: Unit => Result)(implicit request: DataRequest[A]): Future[Result] = {
    val methodLoggingContext = "handleSuccess"
    implicit val correlationId: CorrelationId = request.correlationId

    val infoLogger: String => Unit = infoLog(
      secondaryContext = methodLoggingContext,
      dataLog = correlationIdLogString(correlationId),
      extraContext = Some(context)
    )

    infoLogger("Successfully matched member and retrieved protection record details. Updating session data cache")

    auditSubmission(
      auditType = "CompleteMemberSearch",
      path = routes.ResultsController.onPageLoad().url,
      details = auditDetail.copy(journey = "resultsDisplayed", searchAPIMatchResult = Some("MATCH"))
    )

    for {
      updatedAnswers <- Future.fromTry(request.userAnswers.set(ResultsPage, MembersResult(isSuccessful = true)))
      _ <- service.save(updatedAnswers)
    } yield {
      infoLogger("Successfully updated session data cache. Attempting to serve 'results' view with data")
      resultBuilder()
    }
  }

  private def handleError[A](errorWrapper: ErrorWrapper, auditDetail: AuditDetail, context: String)
                            (implicit request: DataRequest[A], hc: HeaderCarrier): Future[Result] = {
    val methodLoggingContext = "handleError"
    implicit val correlationId: CorrelationId = errorWrapper.correlationId

    val warnLogger: (String, Option[Throwable]) => Unit = warnLog(
      secondaryContext = methodLoggingContext,
      dataLog = correlationIdLogString(correlationId),
      extraContext = Some(context)
    )

    val errorLogger: (String, Option[Throwable]) => Unit = errorLog(
      secondaryContext = methodLoggingContext,
      dataLog = correlationIdLogString(correlationId),
      extraContext = Some(context)
    )

    errorWrapper.error match {
      case MpeError(code, _, _, _) if code == "NO_MATCH" || code == "EMPTY_DATA" || code == "NOT_FOUND"=>
        val (journey, searchAPIMatchResult): (String, String) = auditParams(errorWrapper.error)
        auditSubmission(
          auditType = "CompleteMemberSearch",
          path = routes.NoResultsController.onPageLoad().url,
          details = auditDetail.copy(journey = journey, searchAPIMatchResult = Some(searchAPIMatchResult))
        )

        warnLogger(s"No results could be found for the supplied member details with error reason: $code", None)

        failedAttemptService.handleFailedAttempt(
          Redirect(routes.LockedOutController.onPageLoad())
        )(
          Redirect(routes.NoResultsController.onPageLoad())
        )(request.toIdentifierRequest, ec)
      case error =>
        implicit val correlationId: CorrelationId = errorWrapper.correlationId
        auditSubmission(
          auditType = "CompleteMemberSearch",
          path = routes.ClearCacheController.defaultError().url,
          details = error.source match {
            case MatchPerson => auditDetail.copy(journey = "searchAPIError", searchAPIFailureReason = Some(error.code))
            case _ => auditDetail.copy(journey = "retrieveAPIError", retrieveAPIFailureReason = Some(error.code))
          }
        )

        errorLogger(
          s"Attempt failed due to error with ${error.toLogString}. Redirecting to clear user session data cache",
          None
        )

        Future.successful(Redirect(routes.ClearCacheController.defaultError()))
    }
  }

  private def auditSubmission(auditType: String, path: String, details: AuditDetail)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext, correlationId: CorrelationId): Future[AuditResult] = {

    val event: AuditEvent[AuditDetail] = AuditEvent(
      auditType = auditType,
      detail = details,
      transactionName = "member-search-results",
      path = path
    )
    auditService.auditEvent(event)
  }

  private def generateAuditDetail(pensionSchemeMemberRequest: PensionSchemeMemberRequest, userDetails: UserDetails): AuditDetail =
    AuditDetail(journey = "journey",
      request = pensionSchemeMemberRequest,
      userDetails = userDetails)

  private lazy val auditParams: MpeError => (String, String) = error => {
    error.code match {
      case "NO_MATCH" => ("noMemberMatched", "NO MATCH")
      case "NOT_FOUND" if error.source == MatchPerson => ("noMemberMatched", "NO MATCH")
      case _ => ("memberMatchedNoData", "MATCH")
    }
  }
}
