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

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import models.mongo.CacheUserDetails
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Result
import repositories.{FailedAttemptCountRepository, FailedAttemptLockoutRepository}

import scala.concurrent.{ExecutionContext, Future}

@Singleton
class FailedAttemptService @Inject()(failedAttemptLockoutRepository: FailedAttemptLockoutRepository,
                                     failedAttemptCountRepository: FailedAttemptCountRepository,
                                     frontendAppConfig: FrontendAppConfig) extends Logging {
  private val classLoggingContext: String = "FailedAttemptService"

  def checkForLockout()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Boolean] = {
    import request.userDetails._

    val methodLoggingContext: String = "checkForLockout"
    val loggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$loggingContext - ...")

    failedAttemptLockoutRepository.getFromCache(request.userDetails.userId).map {
      case Some(value) if value.psrUserType == psrUserType && value.psrUserId == psrUserId =>
        logger.warn(s"$loggingContext - ...")
        true
      case Some(_) =>
        logger.error(s"$methodLoggingContext - ...")
        ???
      case None =>
        logger.info(s"$loggingContext - ...")
        false
    }
  }

  private def checkThresholdExceeded()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Boolean] = {
    failedAttemptCountRepository.countFailedAttempts().map {
      case count if count >= frontendAppConfig.lockoutThreshold => true
      case _ => false
    }
  }

  private def createLockout()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Unit] = {
    import request.userDetails._

    val methodLoggingContext: String = "createLockout"
    val loggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$loggingContext - ...")

    failedAttemptLockoutRepository.putCache(userId)(
      CacheUserDetails(userDetails = request.userDetails, withInternalId = false)
    )
  }

  def handleFailedAttempt(lockoutResult: Result)(noLockoutResult: Result)
                         (implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Result] = {
    failedAttemptCountRepository
      .addFailedAttempt()
      .flatMap(
        _ => checkThresholdExceeded()
      )
      .map {
        case true =>
          createLockout()
          lockoutResult
        case false =>
          noLockoutResult
      }

  }


}
