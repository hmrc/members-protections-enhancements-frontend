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
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.requests.UserType.PSA
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}

import scala.concurrent.Future

class MpsDashboardController @Inject()(
                                        identify: IdentifierAction,
                                        getData: DataRetrievalAction,
                                        val controllerComponents: MessagesControllerComponents,
                                        val appConfig: FrontendAppConfig
                                      )  extends MpeBaseController(identify, getData) {

  def redirectToMps(): Action[AnyContent] = handle {
    implicit request =>
      val mpsUrl =
        request.userDetails.psrUserType match {
          case PSA => appConfig.psaOverviewUrl
          case _ => appConfig.pspDashboardUrl
        }
      Future.successful(Redirect(mpsUrl))
  }
}
