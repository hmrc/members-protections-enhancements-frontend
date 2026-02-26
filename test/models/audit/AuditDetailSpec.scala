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

package models.audit

import base.SpecBase
import play.api.libs.json.{JsValue, Json}

class AuditDetailSpec extends SpecBase {
  "writes" - {
    "should return the expected JSON" in {
      val result: JsValue = Json.toJson(
        AuditDetail(
          journey = "journey",
          firstName = "firstName",
          lastName = "lastName",
          dateOfBirth = "dateOfBirth",
          nino = "nino",
          pensionSchemeMemberCheckReference = "pensionSchemeMemberCheckReference",
          searchAPIMatchResult = Some("searchAPIMatchResult"),
          retrieveAPIFailureReason = Some("retrieveAPIFailureReason"),
          searchAPIFailureReason = Some("searchAPIFailureReason"),
          numberOfProtectionsAndEnhancementsTotal = Some(1),
          numberOfProtectionsAndEnhancementsActive = Some(2),
          numberOfProtectionsAndEnhancementsDormant = Some(3),
          numberOfProtectionsAndEnhancementsWithdrawn = Some(4),
          roleLoggedInAs = "roleLoggedInAs",
          affinityGroup = "affinityGroup",
          requesterIdentifier = "requesterIdentifier",
          correlationId = "correlationId"
        )
      )

      result mustBe Json.parse("""
          |{
          | "journey": "journey",
          | "firstName": "firstName",
          | "lastName": "lastName",
          | "dateOfBirth": "dateOfBirth",
          | "nino": "nino",
          | "pensionSchemeMemberCheckReference": "pensionSchemeMemberCheckReference",
          | "searchAPIMatchResult": "searchAPIMatchResult",
          | "retrieveAPIFailureReason": "retrieveAPIFailureReason",
          | "searchAPIFailureReason": "searchAPIFailureReason",
          | "numberOfProtectionsAndEnhancementsTotal": 1,
          | "numberOfProtectionsAndEnhancementsActive": 2,
          | "numberOfProtectionsAndEnhancementsDormant": 3,
          | "numberOfProtectionsAndEnhancementsWithdrawn": 4,
          | "roleLoggedInAs": "roleLoggedInAs",
          | "affinityGroup": "affinityGroup",
          | "requesterIdentifier": "requesterIdentifier",
          | "correlationId": "correlationId"
          |}
        """.stripMargin)
    }
  }
}
