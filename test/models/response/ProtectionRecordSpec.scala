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

package models.response

import base.SpecBase
import models.response.RecordStatusMapped.Active
import models.response.RecordTypeMapped.{FixedProtection2016, PcrPreCommencement}
import play.api.libs.json.*

class ProtectionRecordSpec extends SpecBase {
  val testModel: ProtectionRecord = ProtectionRecord(
    protectionReference = Some("some-id"),
    `type` = FixedProtection2016,
    status = Active,
    protectedAmount = Some(1),
    lumpSumAmount = Some(1),
    lumpSumPercentage = Some(1),
    enhancementFactor = Some(0.5)
  )

  val testJson: JsValue = Json.parse(
    """
      |{
      | "protectionReference": "some-id",
      | "type": "FIXED PROTECTION 2016",
      | "status": "OPEN",
      | "protectedAmount": 1,
      | "lumpSumAmount": 1,
      | "lumpSumPercentage": 1,
      | "enhancementFactor": 0.5
      |}
    """.stripMargin
  )

  "reads" -> {
    "return a JsError when reading from invalid JSON" in {
      JsObject.empty.validate[ProtectionRecord] mustBe a[JsError]
    }

    "return a JsSuccess when reading from valid JSON" in {
      val result = testJson.validate[ProtectionRecord]
      result mustBe a[JsSuccess[_]]
      result.get mustBe testModel
    }

    "work for a valid pension credit rights entry" in {
      val testJson: JsValue = Json.parse(
        """
          |{
          | "protectionReference": "some-id",
          | "type": "PENSION CREDIT RIGHTS",
          | "status": "OPEN",
          | "protectedAmount": 1,
          | "lumpSumAmount": 1,
          | "lumpSumPercentage": 1,
          | "enhancementFactor": 0.5,
          | "pensionCreditLegislation": "PARAGRAPH 18 SCHEDULE 36 FINANCE ACT 2004"
          |}
        """.stripMargin
      )

      val result = testJson.validate[ProtectionRecord]
      result mustBe a[JsSuccess[_]]
      result.get mustBe testModel.copy(`type` = PcrPreCommencement)
    }

    "not work when pension credit legislation is missing for a `PENSION CREDIT RIGHTS` type" in {
      val testJson: JsValue = Json.parse(
        """
          |{
          | "protectionReference": "some-id",
          | "type": "PENSION CREDIT RIGHTS",
          | "status": "OPEN",
          | "protectedAmount": 1,
          | "lumpSumAmount": 1,
          | "lumpSumPercentage": 1,
          | "enhancementFactor": 0.5
          |}
        """.stripMargin
      )

      val result = testJson.validate[ProtectionRecord]
      result mustBe a[JsError]
      val errorResult = result.asInstanceOf[JsError]
      (errorResult.errors must have).length(1)
      val (path, msgs) = errorResult.errors.head
      path.toString() mustBe "/pensionCreditLegislation"
      (msgs must have).length(1)
      msgs.head.message mustBe "error.path.missing"

    }
  }
}
