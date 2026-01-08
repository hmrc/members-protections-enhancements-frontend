/*
 * Copyright 2026 HM Revenue & Customs
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

import generators.ModelGenerators
import models.requests.IdentifierRequest
import models.requests.IdentifierRequest.PractitionerRequest
import models.requests.UserType.Psp
import play.api.mvc.*
import uk.gov.hmrc.auth.core.AffinityGroup

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class FakePspIdentifierAction @Inject()(bodyParsers: BodyParsers.Default) extends IdentifierAction with ModelGenerators {

  override def invokeBlock[A](request: Request[A], block: IdentifierRequest[A] => Future[Result]): Future[Result] = {
    block(PractitionerRequest(AffinityGroup.Individual, "id","21000002", Psp, request))
  }

  override def parser: BodyParser[AnyContent] = bodyParsers

  override protected def executionContext: ExecutionContext =
    scala.concurrent.ExecutionContext.Implicits.global
}
