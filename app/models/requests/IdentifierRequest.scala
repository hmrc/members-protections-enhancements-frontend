/*
 * Copyright 2024 HM Revenue & Customs
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

import models.PensionSchemeId.{PsaId, PspId}
import play.api.mvc.{Request, WrappedRequest}

sealed abstract class IdentifierRequest[A] (request: Request[A]) extends WrappedRequest[A](request) { self =>
  val userId: String
}

object IdentifierRequest {
  case class AdministratorRequest[A](userId: String,
                                     request: Request[A],
                                     psaId: PsaId) extends IdentifierRequest[A](request)

  object AdministratorRequest {
    def apply[A](userId: String, request: Request[A], psaId: String): IdentifierRequest[A] =
      AdministratorRequest(userId, request, PsaId(psaId))
  }

  case class PractitionerRequest[A](userId: String,
                                    request: Request[A],
                                    pspId: PspId) extends IdentifierRequest[A](request)

  object PractitionerRequest {
    def apply[A](userId: String, request: Request[A], pspId: String): IdentifierRequest[A] =
      PractitionerRequest(userId, request, PspId(pspId))
  }

}