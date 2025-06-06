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
import controllers.routes
import handlers.ErrorHandler
import models.requests.IdentifierRequest.{AdministratorRequest, PractitionerRequest}
import models.requests.{IdentifierRequest, UserType}
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, authorisedEnrolments, internalId}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AuthenticatedIdentifierAction])
trait IdentifierAction extends ActionBuilder[IdentifierRequest, AnyContent]

@Singleton
class AuthenticatedIdentifierAction @Inject()(override val authConnector: AuthConnector,
                                              config: FrontendAppConfig,
                                              playBodyParsers: BodyParsers.Default,
                                              errorHandler: ErrorHandler)
                                             (implicit override val executionContext: ExecutionContext)
  extends IdentifierAction with AuthorisedFunctions with Logging {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    val logContext: String = "[AuthenticatedIdentifierAction][invokeBlock] - "
    implicit val hc: HeaderCarrier = HeaderCarrierConverter.fromRequestAndSession(request, request.session)

    authorised(Enrolment(Constants.psaEnrolmentKey).or(Enrolment(Constants.pspEnrolmentKey)))
      .retrieve(internalId and affinityGroup and authorisedEnrolments) {

        case Some(internalId) ~ Some(affGroup) ~ IsPSA(psaId) if hasValidSession(hc) =>
          block(AdministratorRequest(affGroup, internalId, psaId.value, UserType.PSA, request))
        case Some(internalId) ~ Some(affGroup) ~ IsPSP(pspId) if hasValidSession(hc) =>
          block(PractitionerRequest(affGroup, internalId, pspId.value, UserType.PSP, request))
        case Some(_) ~ Some(_) ~ _ => Future.successful(Redirect(config.loginUrl))
        case _ => Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
      } recoverWith {
      case err: AuthorisationException =>
        logger.warn(logContext + s"An authorisation error occurred with message: ${err.reason}")
        Future.successful(Redirect(config.loginUrl, Map("continue" -> Seq(config.loginContinueUrl))))
      case err: Throwable =>
        errorHandler.onServerError(request, err)
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
