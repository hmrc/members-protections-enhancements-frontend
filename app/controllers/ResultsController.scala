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
import models.MembersResult
import models.audit.{AuditDetail, AuditEvent}
import models.errors.{MatchPerson, MpeError}
import models.requests.{IdentifierRequest, PensionSchemeMemberRequest, UserDetails}
import models.response.RecordStatusMapped.{Active, Dormant, Withdrawn}
import pages.ResultsPage
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import providers.DateTimeProvider
import services.{AuditService, FailedAttemptService, MembersCheckAndRetrieveService, SessionCacheService}
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.{DateTimeFormats, IdGenerator, Logging}
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
                                  auditService: AuditService,
                                  idGenerator: IdGenerator)
                                 (implicit ec: ExecutionContext)
  extends MpeBaseController(identify, checkLockout, getData) with Logging {

  val classLoggingContext: String = "ResultsController"

  def onPageLoad(): Action[AnyContent] = handle(implicit request => {
    implicit val correlationId: String = idGenerator.getCorrelationId
    val methodLoggingContext: String = "onPageLoad"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"
    logInfo(fullLoggingContext, s"with correlationId: $correlationId")
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    getUserData(request) match {
      case Some((memberDetails, membersDob, membersNino, membersPsaCheckRef)) =>
        val pensionSchemeMemberRequest = retrieveMembersRequest(memberDetails, membersDob, membersNino, membersPsaCheckRef)
        val auditDetail = generateAuditDetail(pensionSchemeMemberRequest, request.userDetails)
        checkAndRetrieveService.checkAndRetrieve(pensionSchemeMemberRequest).flatMap {
          case Right(value) =>
            logInfo(s"$fullLoggingContext", s"Successfully retrieved results for supplied details redirecting to Results page")
            auditSubmission("CompleteMemberSearch", routes.ResultsController.onPageLoad().url,
              auditDetail.copy(journey = "resultsDisplayed", searchAPIMatchResult = Some("MATCH"),
                numberOfProtectionsAndEnhancementsTotal = Some(value.protectionRecords.size),
                numberOfProtectionsAndEnhancementsActive = Some(value.protectionRecords.count(_.status == Active)),
                numberOfProtectionsAndEnhancementsDormant = Some(value.protectionRecords.count(_.status == Dormant)),
                numberOfProtectionsAndEnhancementsWithdrawn = Some(value.protectionRecords.count(_.status == Withdrawn))))
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(ResultsPage, MembersResult(isSuccessful = true)))
              _ <- service.save(updatedAnswers)
            } yield {
              Ok(
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
            }
          case Left(error) if error.code == "NO_MATCH" || error.code == "EMPTY_DATA" || error.code == "NOT_FOUND"=>
            val (journey, searchAPIMatchResult): (String, String) = auditParams(error)
            auditSubmission("CompleteMemberSearch", routes.NoResultsController.onPageLoad().url,
              auditDetail.copy(journey = journey, searchAPIMatchResult = Some(searchAPIMatchResult)))

            implicit val req: IdentifierRequest[AnyContent] = request.toIdentifierRequest
            logger.warn(s"$fullLoggingContext - No results found due to ${error.code}")

            failedAttemptService.handleFailedAttempt(
              Redirect(routes.LockedOutController.onPageLoad())
            )(
              Redirect(routes.NoResultsController.onPageLoad())
            )
          case Left(error) =>
            logger.warn(s"$fullLoggingContext - Failure to get the results due to ${error.code}")
            error.source match {
              case MatchPerson =>
                auditSubmission("CompleteMemberSearch", routes.ClearCacheController.defaultError().url,
                  auditDetail.copy(journey = "searchAPIError", searchAPIFailureReason = Some(error.code)))
              case _ =>
                auditSubmission("CompleteMemberSearch", routes.ClearCacheController.defaultError().url,
                  auditDetail.copy(journey = "retrieveAPIError", retrieveAPIFailureReason = Some(error.code)))
            }
            Future.successful(Redirect(routes.ClearCacheController.defaultError()))
        }
      case _ =>
        Future.successful(Redirect(routes.ClearCacheController.onPageLoad()))
    }
  })

  private def auditSubmission(auditType: String, path: String, details: AuditDetail)
                             (implicit hc: HeaderCarrier, ec: ExecutionContext): Future[AuditResult] = {

    val event: AuditEvent[AuditDetail] = AuditEvent(
      auditType = auditType,
      detail = details,
      transactionName = "member-search-results",
      path = path
    )
    auditService.auditEvent(event)
  }

  private def generateAuditDetail(pensionSchemeMemberRequest: PensionSchemeMemberRequest,
                                  userDetails: UserDetails)
                                 (implicit correlationId: String): AuditDetail =
    AuditDetail(
      journey = "journey",
      request = pensionSchemeMemberRequest,
      userDetails = userDetails
    )

  private lazy val auditParams: MpeError => (String, String) = error => {
    error.code match {
      case "NO_MATCH" => ("noMemberMatched", "NO MATCH")
      case "NOT_FOUND" if error.source == MatchPerson => ("noMemberMatched", "NO MATCH")
      case _ => ("memberMatchedNoData", "MATCH")
    }
  }
}
