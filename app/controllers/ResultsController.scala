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

import com.google.inject.Inject
import controllers.actions.{CheckLockoutAction, DataRetrievalAction, IdentifierAction}
import models.MembersResult
import models.audit.{AuditDetail, AuditEvent}
import models.errors.ErrorSource.MatchPerson
import models.errors.{ErrorSource, MpeError}
import models.requests.{DataRequest, PensionSchemeMemberRequest, UserDetails}
import models.response.RecordStatusMapped.{Active, Dormant, Withdrawn}
import pages.ResultsPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import providers.DateTimeProvider
import services.{AuditService, FailedAttemptService, MembersCheckAndRetrieveService, SessionCacheService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.constants.AuditJourneyTypes.{
  DEFAULT_JOURNEY,
  MEMBER_MATCHED_NO_DATA,
  NO_MEMBER_MATCHED,
  RESULTS_DISPLAYED,
  RETRIEVE_API_ERROR,
  SEARCH_API_ERROR
}
import utils.{DateTimeFormats, IdGenerator, Logging}
import views.html.ResultsView
import utils.constants.AuditResultTypes.{MATCH, NO_MATCH as NO_MATCH_RESULT}
import utils.constants.AuditTransactionTypes.MEMBER_SEARCH_RESULTS
import utils.constants.AuditTypes.COMPLETE_MEMBER_SEARCH
import utils.constants.ErrorCodes.{EMPTY_DATA, NOT_FOUND as NOT_FOUND_ERROR, NO_MATCH}

import scala.concurrent.{ExecutionContext, Future}

class ResultsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  checkLockout: CheckLockoutAction,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  view: ResultsView,
  dateTimeProvider: DateTimeProvider,
  checkAndRetrieveService: MembersCheckAndRetrieveService,
  failedAttemptService: FailedAttemptService,
  service: SessionCacheService,
  auditService: AuditService,
  idGenerator: IdGenerator
)(implicit ec: ExecutionContext)
    extends MpeBaseController(identify, checkLockout, getData)
    with Logging {

  val classLoggingContext: String = "ResultsController"

  def onPageLoad(): Action[AnyContent] = handleWithCheckedAnswers {
    implicit request => memberDetails => membersDob => membersNino => membersPsaCheckRef => _ =>
      {
        implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)
        implicit val correlationId: String = idGenerator.getCorrelationId

        val fullLoggingContext: String = s"[$classLoggingContext][onPageLoad]"
        logInfo(fullLoggingContext, s"with correlationId: $correlationId")

        val pensionSchemeMemberRequest: PensionSchemeMemberRequest = retrieveMembersRequest(
          memberDetails = memberDetails,
          membersDob = membersDob,
          membersNino = membersNino,
          membersPsaCheckRef = membersPsaCheckRef
        )
        val auditDetail: AuditDetail = generateAuditDetail(pensionSchemeMemberRequest, request.userDetails)

        checkAndRetrieveService.checkAndRetrieve(pensionSchemeMemberRequest).flatMap {
          case Right(value) =>
            logInfo(
              s"$fullLoggingContext",
              s"Successfully retrieved results for supplied details redirecting to Results page"
            )
            auditSubmission(
              COMPLETE_MEMBER_SEARCH,
              routes.ResultsController.onPageLoad().url,
              details = auditDetail.copy(
                journey = RESULTS_DISPLAYED,
                searchAPIMatchResult = Some(MATCH),
                numberOfProtectionsAndEnhancementsTotal = Some(value.protectionRecords.size),
                numberOfProtectionsAndEnhancementsActive = Some(value.protectionRecords.count(_.status == Active)),
                numberOfProtectionsAndEnhancementsDormant = Some(value.protectionRecords.count(_.status == Dormant)),
                numberOfProtectionsAndEnhancementsWithdrawn = Some(value.protectionRecords.count(_.status == Withdrawn))
              )
            )
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ResultsPage, MembersResult(isSuccessful = true)))
              _ <- service.save(updatedAnswers)
            } yield Ok(
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
          case Left(error) => handleErrorResponse(error, auditDetail, fullLoggingContext)
        }
      }

  }

  private def handleErrorResponse(error: MpeError, auditDetail: AuditDetail, loggingContext: String)(implicit
    request: DataRequest[AnyContent]
  ) = {
    val fullLoggingContext: String = s"$loggingContext[handleErrorResponse]"

    error.code match {
      case NO_MATCH | EMPTY_DATA | NOT_FOUND_ERROR =>
        logWarn(fullLoggingContext, s"No results found due to ${error.code}")

        implicit val details: UserDetails = request.userDetails
        val (journey, searchAPIMatchResult): (String, String) = auditParams(error)
        auditSubmission(
          auditType = COMPLETE_MEMBER_SEARCH,
          path = routes.NoResultsController.onPageLoad().url,
          details = auditDetail.copy(journey = journey, searchAPIMatchResult = Some(searchAPIMatchResult))
        )

        failedAttemptService.handleFailedAttempt(
          Redirect(routes.LockedOutController.onPageLoad())
        )(
          Redirect(routes.NoResultsController.onPageLoad())
        )
      case _ =>
        logWarn(fullLoggingContext, s"Failure to get the results due to ${error.code}")

        val auditJourneyString: String = if (error.source == MatchPerson) SEARCH_API_ERROR else RETRIEVE_API_ERROR
        auditSubmission(
          auditType = COMPLETE_MEMBER_SEARCH,
          path = routes.ClearCacheController.defaultError().url,
          details = auditDetail.copy(
            journey = auditJourneyString,
            retrieveAPIFailureReason = Some(error.code)
          )
        )

        Future.successful(Redirect(routes.ClearCacheController.defaultError()))
    }
  }

  private def auditSubmission(auditType: String, path: String, details: AuditDetail)(implicit
    hc: HeaderCarrier,
    ec: ExecutionContext
  ): Future[AuditResult] = {

    val event: AuditEvent[AuditDetail] = AuditEvent(
      auditType = auditType,
      detail = details,
      transactionName = MEMBER_SEARCH_RESULTS,
      path = path
    )
    auditService.auditEvent(event)
  }

  private def generateAuditDetail(pensionSchemeMemberRequest: PensionSchemeMemberRequest, userDetails: UserDetails)(
    implicit correlationId: String
  ): AuditDetail =
    AuditDetail(
      journey = DEFAULT_JOURNEY,
      request = pensionSchemeMemberRequest,
      userDetails = userDetails
    )

  private lazy val auditParams: MpeError => (String, String) = error =>
    error.code match {
      case NO_MATCH => (NO_MEMBER_MATCHED, NO_MATCH_RESULT)
      case NOT_FOUND_ERROR if error.source == MatchPerson => (NO_MEMBER_MATCHED, NO_MATCH_RESULT)
      case _ => (MEMBER_MATCHED_NO_DATA, MATCH)
    }
}
