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
import models.requests.IdentifierRequest.{AdministratorRequest, PractitionerRequest}
import models.requests.{IdentifierRequest, UserType}
import play.api.Logging
import play.api.mvc.Results._
import play.api.mvc._
import services.FailedAttemptService
import uk.gov.hmrc.auth.core._
import uk.gov.hmrc.auth.core.retrieve.v2.Retrievals.{affinityGroup, authorisedEnrolments, internalId}
import uk.gov.hmrc.auth.core.retrieve.~
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.http.HeaderCarrierConverter

import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[CheckLockoutActionImpl])
trait CheckLockoutAction extends ActionFilter[IdentifierRequest]

@Singleton
class CheckLockoutActionImpl @Inject()(val config: FrontendAppConfig,
                                       failedAttemptService: FailedAttemptService)
                                      (implicit override val executionContext: ExecutionContext)
  extends CheckLockoutAction with Logging {

  override protected def filter[A](request: IdentifierRequest[A]): Future[Option[Result]] = if (config.lockoutEnabled) {
    failedAttemptService.checkForLockout()(request, executionContext).map {
      case false => None
      case true => Some(Redirect(routes.UnauthorisedController.onPageLoad())) //TODO - replace with real page
    }
  } else {
    Future.successful(None)
  }
}
