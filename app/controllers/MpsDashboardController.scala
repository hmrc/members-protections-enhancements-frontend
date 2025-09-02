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

package controllers

import com.google.inject.Inject
import config.FrontendAppConfig
import controllers.actions.{CheckLockoutAction, DataRetrievalAction, IdentifierAction}
import models.requests.UserType.PSA
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import uk.gov.hmrc.play.bootstrap.frontend.controller.FrontendBaseController
import utils.NewLogging

import scala.concurrent.Future

class MpsDashboardController @Inject()(identify: IdentifierAction,
                                       checkLockout: CheckLockoutAction,
                                       getData: DataRetrievalAction,
                                       val controllerComponents: MessagesControllerComponents,
                                       val appConfig: FrontendAppConfig)
  extends FrontendBaseController with NewLogging {

  def redirectToMps(): Action[AnyContent] = (identify andThen checkLockout andThen getData).async { implicit request =>
    val methodLoggingContext: String = "redirectToMps"
    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(request.correlationId))

    Future.successful(Redirect{
      request.userDetails.psrUserType match {
        case PSA =>
          infoLogger("Attempting to redirect PSA user to MPS dashboard")
          appConfig.psaOverviewUrl
        case _ =>
          infoLogger("Attempting to redirect PSP user to MPS dashboard")
          appConfig.pspDashboardUrl
      }
    })
  }
}
