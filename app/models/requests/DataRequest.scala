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

package models.requests

import models.UserAnswers
import models.requests.IdentifierRequest.{AdministratorRequest, PractitionerRequest}
import play.api.mvc.{Request, WrappedRequest}

case class DataRequest[A] (request: Request[A],
                           userDetails: UserDetails,
                           userAnswers: UserAnswers,
                           correlationId: Option[String] = None) extends WrappedRequest[A](request) {
  def toIdentifierRequest: IdentifierRequest[A] = userDetails.psrUserType match {
    case UserType.PSA => new AdministratorRequest[A](userDetails, request)
    case UserType.PSP => new PractitionerRequest[A](userDetails, request)
  }
}

