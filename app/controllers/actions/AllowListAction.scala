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
import config.FrontendAppConfig
import models.requests.IdentifierRequest
import play.api.Logging
import play.api.mvc.Results.Redirect
import play.api.mvc.{ActionFilter, Result}

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[AllowListActionImpl])
trait AllowListAction extends ActionFilter[IdentifierRequest]

@Singleton
class AllowListActionImpl @Inject()(frontendAppConfig: FrontendAppConfig)
                                   (implicit val executionContext: ExecutionContext)
  extends AllowListAction with Logging {
  private val classLoggingContext: String = "AllowListAction"

  override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = {
    val methodLoggingContext: String = "filter"
    val fullLoggingContext: String = s"[$classLoggingContext][$methodLoggingContext]"

    logger.info(s"$fullLoggingContext - Received request to handle allow listing for user")

    Future.successful(
      if (frontendAppConfig.allowListEnabled) {
        logger.info(s"$fullLoggingContext - Allow listing is disabled. Checking user against allowed PSR IDs")

        if (frontendAppConfig.allowedPsrIds.contains(request.userDetails.psrUserId)){
          logger.info(s"$fullLoggingContext - User PSR ID is allowed. Continuing with request")
          None
        } else {
          logger.error(s"$fullLoggingContext - User PSR ID is not allowed. Returning Unauthorised view")
          Some(Redirect(controllers.routes.UnauthorisedController.onPageLoad()))
        }
      } else {
        logger.info(s"$fullLoggingContext - Allow listing is disabled. Continuing with request")
        None
      }
    )
  }

}
