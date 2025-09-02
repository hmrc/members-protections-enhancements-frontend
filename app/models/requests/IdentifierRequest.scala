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
import play.api.mvc.{Request, WrappedRequest}
import uk.gov.hmrc.auth.core.AffinityGroup

sealed abstract class IdentifierRequest[A](request: Request[A]) extends WrappedRequest[A](request) { self =>
  val userDetails: UserDetails
  val correlationId: CorrelationId
}

object IdentifierRequest {
  case class AdministratorRequest[A](request: Request[A],
                                     userDetails: UserDetails,
                                     correlationId: CorrelationId) extends IdentifierRequest[A](request)

  object AdministratorRequest {
    def apply[A](affGroup: AffinityGroup,
                 userId: String,
                 psaId: String,
                 psrUserType: UserType,
                 request: RequestWithCorrelationId[A]): IdentifierRequest[A] =
      AdministratorRequest(request, UserDetails(psrUserType, psaId, userId, affGroup), request.correlationId)
  }

  case class PractitionerRequest[A](request: Request[A],
                                    userDetails: UserDetails,
                                    correlationId: CorrelationId) extends IdentifierRequest[A](request)

  object PractitionerRequest {
    def apply[A](affGroup: AffinityGroup,
                 userId: String,
                 pspId: String,
                 psrUserType: UserType,
                 request: RequestWithCorrelationId[A]): IdentifierRequest[A] =
      PractitionerRequest(request, UserDetails(psrUserType, pspId, userId, affGroup), request.correlationId)
  }

}