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

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import models.mongo.CacheUserDetails
import models.requests.UserDetails
import play.api.Logging
import play.api.mvc.Result
import repositories.{FailedAttemptCountRepository, FailedAttemptLockoutRepository}

import java.time.Instant
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FailedAttemptService @Inject() (
  failedAttemptLockoutRepository: FailedAttemptLockoutRepository,
  failedAttemptCountRepository: FailedAttemptCountRepository,
  frontendAppConfig: FrontendAppConfig
) extends Logging {

  def checkForLockout()(implicit userDetails: UserDetails, ec: ExecutionContext): Future[Boolean] =
    failedAttemptLockoutRepository.getFromCache(userDetails.psrUserId).map {
      case Some(value) if value.psrUserType == userDetails.psrUserType =>
        logger.warn("User has been locked out")
        true
      case Some(_) =>
        logger.warn("Existing lockout was found for user with conflicting PSR details")
        throw new IllegalStateException("User lockout has conflicting PSR details")
      case None =>
        false
    }

  def handleFailedAttempt(
    lockoutResult: Result
  )(noLockoutResult: Result)(implicit userDetails: UserDetails, ec: ExecutionContext): Future[Result] =
    failedAttemptCountRepository
      .addFailedAttempt()
      .flatMap(_ =>
        failedAttemptCountRepository.countFailedAttempts().map {
          case count if count < frontendAppConfig.lockoutThreshold =>
            noLockoutResult
          case count =>
            logger.warn(s"User has exceeded threshold for failed attempts, with amount: $count")
            failedAttemptLockoutRepository.putCache(userDetails.psrUserId)(
              CacheUserDetails(userDetails = userDetails, withPsrUserId = false)
            )
            failedAttemptCountRepository.removeFailedAttempts()
            lockoutResult
        }
      )

  def getLockoutExpiry()(implicit userDetails: UserDetails): Future[Option[Instant]] =
    failedAttemptLockoutRepository
      .getLockoutExpiry(userDetails.psrUserId)
}
