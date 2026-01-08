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

package services

import com.google.inject.ImplementedBy
import models.userAnswers.UserAnswers
import repositories.SessionRepository
import uk.gov.hmrc.http.HeaderCarrier

import javax.inject.{Inject, Singleton}
import scala.concurrent.{ExecutionContext, Future}

@Singleton
class SessionCacheServiceImpl @Inject()(sessionRepository: SessionRepository) extends SessionCacheService {
  override def save(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit] = {
    sessionRepository.set(userAnswers)
  }
  override def clear(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean] = {
    sessionRepository.clear(userAnswers.id)
  }
}

@ImplementedBy(classOf[SessionCacheServiceImpl])
trait SessionCacheService {
  def save(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Unit]
  def clear(userAnswers: UserAnswers)(implicit hc: HeaderCarrier, ec: ExecutionContext): Future[Boolean]
}
