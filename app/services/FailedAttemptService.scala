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

package services

import com.google.inject.{ImplementedBy, Inject, Singleton}
import config.FrontendAppConfig
import models.mongo.CacheUserDetails
import models.requests.UserDetails
import play.api.Logging
import play.api.mvc.Result
import repositories.{FailedAttemptCountRepository, FailedAttemptLockoutRepository}

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[FailedAttemptServiceImpl])
trait FailedAttemptService {
  def checkForLockout()(implicit userDetails: UserDetails, ec: ExecutionContext): Future[Boolean]

  def handleFailedAttempt(lockoutResult: Result)(noLockoutResult: Result)
                         (implicit userDetails: UserDetails, ec: ExecutionContext): Future[Result]

  def getLockoutExpiry()(implicit userDetails: UserDetails): Future[Option[Instant]]
}

@Singleton
class FailedAttemptServiceImpl @Inject()(failedAttemptLockoutRepository: FailedAttemptLockoutRepository,
                                         failedAttemptCountRepository: FailedAttemptCountRepository,
                                         frontendAppConfig: FrontendAppConfig)
  extends FailedAttemptService with Logging {

  private val classLoggingContext: String = "FailedAttemptService"

  def checkForLockout()(implicit userDetails: UserDetails, ec: ExecutionContext): Future[Boolean] = {
    import userDetails._

    val methodLoggingContext: String = "checkForLockout"
    val loggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$loggingContext - Received request to check for matching lockout for user")

    failedAttemptLockoutRepository.getFromCache(userDetails.psrUserId).map {
      case Some(value) if value.psrUserType == psrUserType =>
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

  private def checkThresholdExceeded()(implicit userDetails: UserDetails, ec: ExecutionContext): Future[Boolean] = {
    val methodLoggingContext: String = "checkThresholdExceeded"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$fullLoggingContext - Attempting to check if failed attempt threshold has been exceeded for user")

    failedAttemptCountRepository.countFailedAttempts().map {
      case count if count < frontendAppConfig.lockoutThreshold =>
        logger.info(s"$fullLoggingContext - Failed attempt threshold not exceeded")
        false
      case count =>
        logger.warn(s"$fullLoggingContext - User has exceeded threshold for failed attempts, with amount: $count")
        true
    }
  }

  private def createLockout()(implicit userDetails: UserDetails, ec: ExecutionContext): Future[Unit] = {
    import userDetails._

    val methodLoggingContext: String = "createLockout"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$fullLoggingContext - Attempting to create lockout for user")

    failedAttemptLockoutRepository.putCache(psrUserId)(
      CacheUserDetails(userDetails = userDetails, withPsrUserId = false)
    )
  }

  def handleFailedAttempt(lockoutResult: Result)
                         (noLockoutResult: Result)
                         (implicit userDetails: UserDetails, ec: ExecutionContext): Future[Result] = {
    val methodLoggingContext: String = "createLockout"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$fullLoggingContext - Received request to handle failed attempt for user")

    failedAttemptCountRepository
      .addFailedAttempt()
      .flatMap(_ => checkThresholdExceeded())
      .flatMap {
        case false =>
          logger.info(s"$fullLoggingContext - Returning no lockout result")
          Future.successful(noLockoutResult)
        case true =>
          createLockout().map(_ => {
            logger.info(s"$fullLoggingContext - Returning lockout result")
            lockoutResult
          })
      }

  }

  override def getLockoutExpiry()(implicit userDetails: UserDetails): Future[Option[Instant]] = {
    val methodLoggingContext: String = "getLockoutExpiry"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$fullLoggingContext - Received request to retrieve lockout expiry for user")

    failedAttemptLockoutRepository
      .getLockoutExpiry(userDetails.psrUserId)
  }
}
