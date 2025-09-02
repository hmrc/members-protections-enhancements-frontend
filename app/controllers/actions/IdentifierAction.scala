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

package controllers.actions

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.{Constants, FrontendAppConfig}
import connectors.UserAllowListConnector
import controllers.routes
import models.CorrelationId
import models.requests.IdentifierRequest.{AdministratorRequest, PractitionerRequest}
import models.requests.{IdentifierRequest, RequestWithCorrelationId, UserType}
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, authorisedEnrolments, internalId}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.{HeaderCarrier, UnauthorizedException}
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.{IdGenerator, NewLogging}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AuthenticatedIdentifierAction])
trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent]

@Singleton
class AuthenticatedIdentifierAction @Inject()(override val authConnector: AuthConnector,
                                              userAllowListConnector: UserAllowListConnector,
                                              idGenerator: IdGenerator,
                                              config: FrontendAppConfig,
                                              playBodyParsers: BodyParsers.Default)
                                             (implicit override val executionContext: ExecutionContext)
  extends IdentifierAction with AuthorisedFunctions with NewLogging {

  private[actions] def handleWithCorrelationId[A](request: Request[A],
                                                  extraContext: String)
                                                 (block: RequestWithCorrelationId[A] => Future[Result]): Future[Result] = {
    val methodLoggingContext: String = "handleWithCorrelationId"

    val infoLogger: String => Unit = infoLog(
      secondaryContext = methodLoggingContext,
      extraContext = Some(extraContext)
    )

    val warnLogger: (String, Option[Throwable]) => Unit = warnLog(
      secondaryContext = methodLoggingContext,
      extraContext = Some(extraContext)
    )

    infoLogger("Attempting to retrieve Correlation ID from request headers")

    val correlationId = request.headers
      .get("correlationId")
      .fold {
        warnLogger("Correlation ID was missing from request headers. Generating new ID for request", None)
        idGenerator.getCorrelationId
      } { id =>
        infoLogger("Correlation ID was successfully retrieved from request headers")
        id
      }

    block(RequestWithCorrelationId(request, CorrelationId(correlationId)))
  }

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    val methodLoggingContext: String = "invokeBlock"
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    handleWithCorrelationId(request, methodLoggingContext) { req =>
      val idLogString = correlationIdLogString(req.correlationId)
      val infoLogger: String => Unit = infoLog(methodLoggingContext, idLogString)
      val warnLogger: (String, Option[Throwable]) => Unit = warnLog(methodLoggingContext, idLogString)

      logger.info(methodLoggingContext, "Attempting to complete authorisation for request")

      authorised(Enrolment(Constants.psaEnrolmentKey).or(Enrolment(Constants.pspEnrolmentKey)))
        .retrieve(internalId and affinityGroup and authorisedEnrolments) {
          case Some(internalId) ~ Some(affGroup) ~ IsPSA(psaId) if hasValidSession(hc) =>
            infoLogger("Authorisation completed successfully for PSA user")
            isValidUser(
              request = AdministratorRequest(affGroup, internalId, psaId.value, UserType.PSA, req),
              block = block,
              extraContext = Some(methodLoggingContext)
            )
          case Some(internalId) ~ Some(affGroup) ~ IsPSP(pspId) if hasValidSession(hc) =>
            infoLogger("Authorisation completed successfully for PSP user")
            isValidUser(
              request = PractitionerRequest(affGroup, internalId, pspId.value, UserType.PSP, req),
              block = block,
              extraContext = Some(methodLoggingContext)
            )
          case Some(_) ~ Some(_) ~ _ if !hasValidSession(hc) =>
            warnLogger("Authorisation completed successfully, but session is invalid. Redirecting user to log in", None)
            Future.successful(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
          case _ =>
            val err: UnauthorizedException = new UnauthorizedException(
              message = "Unable to retrieve user details or type from authorisation response"
            )
            warnLogger("Authorisation completed successfully, but could not retrieve user details or type", Some(err))
            throw err
        } recoverWith {
        case err: NoActiveSession =>
          warnLogger("No active session could be found. Redirecting user to log in", Some(err))
          Future.successful(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
        case err: InsufficientEnrolments =>
          warnLogger("User does not have sufficient enrolments. Redirecting user to MPS registration", Some(err))
          Future.successful(Redirect(config.mpsRegistrationUrl))
        case err: AuthorisationException =>
          errorLog(methodLoggingContext, idLogString)("An unexpected authorisation error occurred", Some(err))
          Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
      }
    }
  }

  private def isValidUser[A](request: IdentifierRequest[A],
                             block: IdentifierRequest[A] => Future[Result],
                             extraContext: Option[String])
                            (implicit hc: HeaderCarrier): Future[Result] = {
    val methodLoggingContext: String = "isValidUser"
    val idLogString = correlationIdLogString(request.correlationId)

    val infoLogger: String => Unit = infoLog(methodLoggingContext, idLogString, extraContext)
    val warnLogger: (String, Option[Throwable]) => Unit = warnLog(methodLoggingContext, idLogString)

    implicit val correlationId: CorrelationId = request.correlationId

    if(config.privateBetaEnabled) {
      infoLogger("Private beta allowlisting is enabled. Checking that user is permitted to use the service")
      userAllowListConnector.check("psrId", request.userDetails.psrUserId).flatMap {
        case true =>
          infoLogger("User is permitted to use the service. Proceeding with user action")
          block(request)
        case false =>
          warnLogger("User is not permitted to use the service. Blocking user action", None)
          Future.successful(Redirect(routes.PrivateBetaUnauthorisedController.onPageLoad()))
      }
    } else {
      infoLogger("Private beta allowlisting is disabled. Proceeding with user action")
      block(request)
    }
  }

  private val hasValidSession: HeaderCarrier => Boolean = hc => hc.sessionId match {
    case Some(_) => true
    case None => false
  }

  override def parser: BodyParser[AnyContent] = playBodyParsers

  private object IsPSA {
    def unapply(enrolments: Enrolments): Option[EnrolmentIdentifier] =
      enrolments.enrolments
        .find(_.key == Constants.psaEnrolmentKey)
        .flatMap(_.getIdentifier(Constants.psaIdKey))
  }

  private object IsPSP {
    def unapply(enrolments: Enrolments): Option[EnrolmentIdentifier] =
      enrolments.enrolments
        .find(_.key == Constants.pspEnrolmentKey)
        .flatMap(_.getIdentifier(Constants.pspIdKey))
  }
}
