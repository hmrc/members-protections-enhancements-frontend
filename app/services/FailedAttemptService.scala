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
import play.api.Logging
import play.api.mvc.Result
import repositories.{FailedAttemptCountRepository, FailedAttemptLockoutRepository}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[FailedAttemptServiceImpl])
trait FailedAttemptService {
  def checkForLockout()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Boolean]

  def handleFailedAttempt(lockoutResult: Result)
                         (noLockoutResult: Result)
                         (implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Result]
}

@Singleton
class FailedAttemptServiceImpl @Inject()(failedAttemptLockoutRepository: FailedAttemptLockoutRepository,
                                         failedAttemptCountRepository: FailedAttemptCountRepository,
                                         frontendAppConfig: FrontendAppConfig)
  extends FailedAttemptService with Logging {

  private val classLoggingContext: String = "FailedAttemptService"

  def checkForLockout()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Boolean] = {
    import request.userDetails._

    val methodLoggingContext: String = "checkForLockout"
    val loggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$loggingContext - Received request to check for matching lockout for user")

    failedAttemptLockoutRepository.getFromCache(request.userDetails.userId).map {
      case Some(value) if value.psrUserType == psrUserType && value.psrUserId == psrUserId =>
        logger.warn(s"$loggingContext - User has been locked out")
        true
      case Some(_) =>
        logger.warn(s"$methodLoggingContext - Existing lockout was found for user with conflicting PSR details")
        throw new IllegalStateException("User lockout has conflicting PSR details")
      case None =>
        logger.info(s"$loggingContext - No existing lockout found for user")
        false
    }
  }

  private def checkThresholdExceeded()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Boolean] = {
    val methodLoggingContext: String = "checkThresholdExceeded"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$fullLoggingContext - Attempting to check if failed attempt threshold has been exceeded for user")

    failedAttemptCountRepository.countFailedAttempts().map {
      case count if count <= frontendAppConfig.lockoutThreshold =>
        logger.info(s"$fullLoggingContext - Failed attempt threshold not exceeded")
        false
      case count =>
        logger.warn(s"$fullLoggingContext - User has exceeded threshold for failed attempts, with amount: $count")
        true
    }
  }

  private def createLockout()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Unit] = {
    import request.userDetails._

    val methodLoggingContext: String = "createLockout"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$fullLoggingContext - Attempting to create lockout for user")

    failedAttemptLockoutRepository.putCache(userId)(
      CacheUserDetails(userDetails = request.userDetails, withInternalId = false)
    )
  }

  def handleFailedAttempt(lockoutResult: Result)
                         (noLockoutResult: Result)
                         (implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Result] = {
    val methodLoggingContext: String = "createLockout"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$fullLoggingContext - Received request to handle failed attempt for user")

    failedAttemptCountRepository
      .addFailedAttempt()
      .flatMap(_ => checkThresholdExceeded())
      .map {
        case false =>
          logger.info(s"$fullLoggingContext - Returning no lockout result")
          noLockoutResult
        case true =>
          createLockout()
          logger.info(s"$fullLoggingContext - Returning lockout result")
          lockoutResult
      }

  }


}
