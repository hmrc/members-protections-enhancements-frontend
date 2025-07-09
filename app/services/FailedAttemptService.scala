package services

import com.google.inject.{Inject, Singleton}
import config.FrontendAppConfig
import models.mongo.CacheUserDetails
import models.requests.IdentifierRequest
import play.api.Logging
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

  def handleFailedAttempt()(implicit request: IdentifierRequest[_], ec: ExecutionContext): Future[Boolean] = {
    failedAttemptCountRepository
      .addFailedAttempt()
      .flatMap(
        _ => checkThresholdExceeded()
      )
      .map {
        case true =>
          createLockout()
          true
        case false =>
          false
      }

  }


}
