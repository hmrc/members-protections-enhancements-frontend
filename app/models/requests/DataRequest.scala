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

import models.CorrelationId
import models.requests.IdentifierRequest.{AdministratorRequest, PractitionerRequest}
import models.userAnswers.UserAnswers
import play.api.mvc.{Request, WrappedRequest}

case class DataRequest[A] (request: Request[A],
                           userDetails: UserDetails,
                           userAnswers: UserAnswers,
                           correlationId: CorrelationId) extends WrappedRequest[A](request) {
  def toIdentifierRequest: IdentifierRequest[A] = userDetails.psrUserType match {
    case UserType.PSA => new AdministratorRequest[A](request, userDetails, correlationId)
    case UserType.PSP => new PractitionerRequest[A](request, userDetails, correlationId)
  }
}

