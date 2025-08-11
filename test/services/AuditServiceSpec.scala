/*
 * Copyright 2023 HM Revenue & Customs
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

import base.SpecBase
import models.audit.AuditEvent
import org.mockito.ArgumentMatchers.any
import org.mockito.Mockito.when
import play.api.Configuration
import uk.gov.hmrc.http.HeaderCarrier
import uk.gov.hmrc.play.audit.http.connector.AuditResult.Success
import uk.gov.hmrc.play.audit.http.connector.{AuditConnector, AuditResult}
import uk.gov.hmrc.play.audit.model.ExtendedDataEvent

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class AuditServiceSpec extends SpecBase {

  implicit val hc: HeaderCarrier = HeaderCarrier()

  private trait Test {
    private val mockedAppName: String              = "sample-application"
    val mockAuditConnector: AuditConnector = mock[AuditConnector]
    val auditType: String = "auditType"
    val transactionName = "transactionName"
    implicit val correlationId: String = "X-123"
    private val mockConfig: Configuration          = mock[Configuration]

    when(mockConfig.get[String]("appName")).thenReturn(mockedAppName)

    lazy val target = new AuditService(mockAuditConnector, mockConfig)
  }

  "AuditService" - {
    "auditing an event" - {
      "return a successful audit result" in new Test {

        val expected: Future[AuditResult] = Future.successful(Success)

        when(mockAuditConnector
          .sendExtendedEvent(any[ExtendedDataEvent]())(any(), any()))
          .thenReturn(expected)

        val event: AuditEvent[String] = AuditEvent(auditType, transactionName = transactionName,"/foo", "{}")
        target.auditEvent(event) mustBe expected
      }
    }
  }
}
