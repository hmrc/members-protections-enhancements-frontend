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
import models.requests.IdentifierRequest.{AdministratorRequest, PractitionerRequest}
import models.requests.{IdentifierRequest, UserType}
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, authorisedEnrolments, internalId}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter
import utils.Logging

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AuthenticatedIdentifierAction])
trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent]

@Singleton
class AuthenticatedIdentifierAction @Inject()(override val authConnector: AuthConnector,
                                              userAllowListConnector: UserAllowListConnector,
                                              config: FrontendAppConfig,
                                              playBodyParsers: BodyParsers.Default)
                                             (implicit override val executionContext: ExecutionContext)
  extends IdentifierAction with AuthorisedFunctions with Logging {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    val logContext: String = "[AuthenticatedIdentifierAction][invokeBlock] - "
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(Enrolment(Constants.psaEnrolmentKey).or(Enrolment(Constants.pspEnrolmentKey)))
      .retrieve(internalId and affinityGroup and authorisedEnrolments) {

        case Some(internalId) ~ Some(affGroup) ~ IsPSA(psaId) if hasValidSession(hc) =>
          isValidUser(AdministratorRequest(affGroup, internalId, psaId.value, UserType.PSA, request), block)
        case Some(internalId) ~ Some(affGroup) ~ IsPSP(pspId) if hasValidSession(hc) =>
          isValidUser(PractitionerRequest(affGroup, internalId, pspId.value, UserType.PSP, request), block)
        case Some(_) ~ Some(_) ~ _ if !hasValidSession(hc) =>
          Future.successful(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
        case _ =>
          throw InternalError("Unknown error occurred in the auth process")
      } recoverWith {
      case _: NoActiveSession =>
        Future.successful(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
      case _: InsufficientEnrolments =>
        Future.successful(Redirect(config.mpsRegistrationUrl))
      case err: AuthorisationException =>
        logger.error(logContext + s"An authorisation error occurred with message", err)
        Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
    }
  }

  private def isValidUser[A](request: IdentifierRequest[A], block: IdentifierRequest[A] => Future[Result])
                                 (implicit hc: HeaderCarrier): Future[Result] =
    if(config.privateBetaEnabled) {
      userAllowListConnector.check("psrId", request.userDetails.psrUserId) flatMap {
        case true => block(request)
        case false => Future.successful(Redirect(routes.PrivateBetaUnauthorisedController.onPageLoad()))
      }
    }else {
      block(request)
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
