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

package generators

import models.CorrelationId
import models.PensionSchemeId.{PsaId, PspId}
import models.requests.{IdentifierRequest, UserDetails}
import models.requests.IdentifierRequest.{AdministratorRequest, PractitionerRequest}
import models.requests.UserType.PSA
import org.scalacheck.Gen
import play.api.mvc.Request
import uk.gov.hmrc.auth.core.AffinityGroup

trait ModelGenerators extends Generators {

  val psaIdGen: Gen[PsaId] = nonEmptyString.map(PsaId)
  val pspIdGen: Gen[PspId] = nonEmptyString.map(PspId)
  val correlationIdGen: Gen[CorrelationId] = nonEmptyString.map(CorrelationId(_))

  def administratorRequestGen[A](request: Request[A]): Gen[AdministratorRequest[A]] = {
    for {
      userId <- nonEmptyString
      psaId <- psaIdGen
      correlationId <- correlationIdGen
    } yield AdministratorRequest(request, UserDetails(PSA, psaId.value, userId, AffinityGroup.Individual), correlationId)
  }

  def practitionerRequestGen[A](request: Request[A]): Gen[PractitionerRequest[A]] = {
    for {
      userId <- nonEmptyString
      pspId <- pspIdGen
      correlationId <- correlationIdGen
    } yield PractitionerRequest(request, UserDetails(PSA, pspId.value, userId, AffinityGroup.Individual), correlationId)
  }

  def identifierRequestGen[A](request: Request[A]): Gen[IdentifierRequest[A]] = Gen.oneOf(
    administratorRequestGen[A](request),
    practitionerRequestGen[A](request)
  )

}
