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

package services

import com.google.inject.ImplementedBy
import models.CorrelationId
import models.userAnswers.UserAnswers
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier
import utils.NewLogging

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@ImplementedBy(classOf[SessionCacheServiceImpl])
trait SessionCacheService {
  def save(userAnswers: UserAnswers)(implicit hc: HeaderCarrier,
                                     ec: ExecutionContext,
                                     correlationId: CorrelationId): Future[Unit]

  def clear(userAnswers: UserAnswers)(implicit hc: HeaderCarrier,
                                      ec: ExecutionContext,
                                      correlationId: CorrelationId): Future[Boolean]
}

@Singleton
class SessionCacheServiceImpl @Inject()(sessionRepository: SessionRepository) extends SessionCacheService with NewLogging {
  override def save(userAnswers: UserAnswers)(implicit hc: HeaderCarrier,
                                              ec: ExecutionContext,
                                              correlationId: CorrelationId): Future[Unit] = {
    val methodLoggingContext: String = "save"

    logger.info(
      secondaryContext = methodLoggingContext,
      message = "Attempting to save user answers",
      dataLog = correlationIdLogString(correlationId)
    )
    sessionRepository.set(userAnswers)
  }

  override def clear(userAnswers: UserAnswers)(implicit hc: HeaderCarrier,
                                               ec: ExecutionContext,
                                               correlationId: CorrelationId): Future[Boolean] = {
    val methodLoggingContext: String = "clear"

    logger.info(
      secondaryContext = methodLoggingContext,
      message = "Attempting to clear user answers",
      dataLog = correlationIdLogString(correlationId)
    )
    sessionRepository.clear(userAnswers.id)
  }
}
