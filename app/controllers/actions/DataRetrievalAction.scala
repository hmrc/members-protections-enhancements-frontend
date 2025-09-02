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

import models.requests.{DataRequest, IdentifierRequest}
import models.userAnswers.UserAnswers
import play.api.mvc.ActionTransformer
import repositories.SessionRepository
import utils.NewLogging

import javax.inject.Inject
import scala.concurrent.{ExecutionContext, Future}

class DataRetrievalActionImpl @Inject()(val sessionRepository: SessionRepository)
                                       (implicit val executionContext: ExecutionContext)
  extends DataRetrievalAction with NewLogging {

  override protected def transform[A](request: IdentifierRequest[A]): Future[DataRequest[A]] = {
    val methodLoggingContext: String = "transform"
    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(request.correlationId))

    infoLogger("Attempting to retrieve existing user data from cache")

    sessionRepository.get(request.userDetails.userId).map {
      case Some(value) =>
        infoLogger("Successfully retrieved existing user data. Proceeding with cached user answers")
        DataRequest(request, request.userDetails, value, request.correlationId)
      case None =>
        infoLogger("No existing user data could be found. Proceeding with empty user answers")
        DataRequest(request, request.userDetails, UserAnswers(request.userDetails.userId), request.correlationId)
    }
  }
}

trait DataRetrievalAction extends ActionTransformer[IdentifierRequest, DataRequest]
