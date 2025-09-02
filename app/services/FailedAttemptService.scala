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

package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import models.mongo.CacheUserDetails
import models.requests.IdentifierRequest
import play.api.mvc.Result
import repositories.{FailedAttemptCountRepository, FailedAttemptLockoutRepository}
import utils.Logging

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[FailedAttemptServiceImpl])
trait FailedAttemptService {
  def checkForLockout()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Boolean]

  def handleFailedAttempt(lockoutResult: Result)(noLockoutResult: Result)
                         (implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Result]

  def getLockoutExpiry()(implicit request: IdentifierRequest[_]): Future[Option[Instant]]
}

@Singleton
class FailedAttemptServiceImpl @Inject()(lockoutRepo: FailedAttemptLockoutRepository,
                                         countRepo: FailedAttemptCountRepository,
                                         appConfig: FrontendAppConfig) extends FailedAttemptService with Logging {
  
  def checkForLockout()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Boolean] = {
    import request.userDetails._

    val methodLoggingContext: String = "checkForLockout"
    val idLogString: String = correlationIdLogString(request.correlationId)
    val infoLogger: String => Unit = infoLog(methodLoggingContext, idLogString)
    val warnLogger: (String, Option[Throwable]) => Unit = warnLog(methodLoggingContext, idLogString)

    infoLogger("Received request to check for existing multiple attempts lockout for user")

    lockoutRepo.getFromCache(request.userDetails.psrUserId).map {
      case Some(value) if value.psrUserType == psrUserType =>
        warnLogger("Existing multiple attempts lockout was found for user", None)
        true
      case Some(_) =>
        val err: IllegalStateException = new IllegalStateException("User lockout has conflicting PSR details")
        warnLogger("Existing multiple attempts lockout was found for user with conflicting PSR details", Some(err))
        throw err
      case None =>
        infoLogger("No existing multiple attempts lockout found was for user")
        false
    }
  }

  private def checkThresholdExceeded(context: String)(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Boolean] = {
    val methodLoggingContext: String = "checkThresholdExceeded"
    val idLogString: String = correlationIdLogString(request.correlationId)
    val infoLogger: String => Unit = infoLog(methodLoggingContext, idLogString, Some(context))

    val warnLogger: (String, Option[Throwable]) => Unit = warnLog(
      secondaryContext = methodLoggingContext,
      dataLog = idLogString,
      extraContext = Some(context)
    )

    infoLogger("Attempting to check if failed attempt threshold has been exceeded for user")

    countRepo.countFailedAttempts().map {
      case count if count < appConfig.lockoutThreshold =>
        infoLogger("Failed attempt threshold has not been exceeded")
        false
      case count =>
        warnLogger(s"User has exceeded threshold for failed attempts, with amount: $count", None)
        true
    }
  }

  private def createLockout()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Unit] = {
    import request.userDetails._

    val methodLoggingContext: String = "createLockout"
    val idLogString: String = correlationIdLogString(request.correlationId)
    val infoLogger: String => Unit = infoLog(methodLoggingContext, idLogString)

    infoLogger("Attempting to create multiple attempts lockout for user")

    lockoutRepo.putCache(psrUserId)(
      CacheUserDetails(userDetails = request.userDetails, withPsrUserId = false)
    )
  }

  def handleFailedAttempt(lockoutResult: Result)
                         (noLockoutResult: Result)
                         (implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Result] = {
    val methodLoggingContext: String = "createLockout"
    val idLogString: String = correlationIdLogString(request.correlationId)
    val infoLogger: String => Unit = infoLog(methodLoggingContext, idLogString)

    infoLogger("Received request to handle failed attempt for user")

    countRepo
      .addFailedAttempt()
      .flatMap(_ => checkThresholdExceeded(methodLoggingContext))
      .flatMap {
        case false =>
          infoLogger("User has not exceeded failed attempt threshold. Returning 'no lockout' result")
          Future.successful(noLockoutResult)
        case true =>
          createLockout().map(_ => {
            infoLogger("User has exceeded failed attempt threshold. Creating lockout and returning 'lockout' result")
            lockoutResult
          })
      }
  }

  override def getLockoutExpiry()(implicit request: IdentifierRequest[_]): Future[Option[Instant]] = {
    val methodLoggingContext: String = "getLockoutExpiry"
    val idLogString: String = correlationIdLogString(request.correlationId)
    val infoLogger: String => Unit = infoLog(methodLoggingContext, idLogString)

    infoLogger(s"Received request to retrieve multiple attempts lockout expiry for user")

    lockoutRepo
      .getLockoutExpiry(request.userDetails.psrUserId)(request.correlationId)
  }
}
