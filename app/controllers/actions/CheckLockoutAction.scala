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

package controllers.actions

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import controllers.routes
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import repositories.SessionRepository
import services.FailedAttemptService

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[CheckLockoutActionImpl])
trait CheckLockoutAction extends ActionFilter[IdentifierRequest]

@Singleton
class CheckLockoutActionImpl @Inject()(val config: FrontendAppConfig,
                                       failedAttemptService: FailedAttemptService,
                                       sessionRepository: SessionRepository)
                                      (implicit override val executionContext: ExecutionContext)
  extends CheckLockoutAction with Logging {

  val classLoggingContext: String = "CheckLockoutAction"

  override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = {
    val methodLoggingContext: String = "filter"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$fullLoggingContext - Checking to see to user has been locked out for failed attempts")
    failedAttemptService.checkForLockout()(request.userDetails, executionContext).flatMap {
      case false =>
        logger.info(s"$fullLoggingContext - User has not been locked out. Continuing with request")
        Future.successful(None)
      case true =>
        logger.warn(s"$fullLoggingContext - User has been locked out. Redirecting to lockout page")
        sessionRepository
          .clear(request.userDetails.userId)
          .map(_ => Some(Redirect(routes.LockedOutController.onPageLoad())))
    }
  }
}
