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
import models.response.RecordStatusMapped.{Active, Dormant, Withdrawn}
import models.response.RecordTypeMapped.FixedProtection2016
import play.api.libs.json._

class ProtectionRecordDetailsSpec extends SpecBase {
  val testModel: ProtectionRecordDetails = ProtectionRecordDetails(Seq(
    ProtectionRecord(
      protectionReference = Some("some-id"),
      `type` = FixedProtection2016,
      status = Active,
      protectedAmount = Some(1),
      lumpSumAmount = Some(1),
      lumpSumPercentage = Some(1),
      enhancementFactor = Some(0.5)
    )
  ))

  val nonActive: ProtectionRecordDetails = ProtectionRecordDetails(Seq(
    ProtectionRecord(
      protectionReference = Some("some-id"),
      `type` = FixedProtection2016,
      status = Dormant,
      protectedAmount = Some(1),
      lumpSumAmount = Some(1),
      lumpSumPercentage = Some(1),
      enhancementFactor = Some(0.5)
    )
  ))

  val testJson: JsValue = Json.parse(
    """
      |{
      | "protectionRecords": [
      |   {
      |     "protectionReference": "some-id",
      |     "type": "FIXED PROTECTION 2016",
      |     "status": "OPEN",
      |     "protectedAmount": 1,
      |     "lumpSumAmount": 1,
      |     "lumpSumPercentage": 1,
      |     "enhancementFactor": 0.5
      |   }
      | ]
      |}
    """.stripMargin
  )


  "reads" -> {
    "return a JsError when reading from invalid JSON" in {
      JsObject.empty.validate[ProtectionRecordDetails] mustBe a[JsError]
    }

    "return a JsSuccess when reading from valid JSON" in {
      val result = testJson.validate[ProtectionRecordDetails]
      result mustBe a[JsSuccess[_]]
      result.get mustBe testModel
    }

    "return a JsSuccess when reading a JSON with empty ProtectionRecords" in {
      val emptyRecordsJson: JsValue = Json.parse(
        """
          |{
          | "protectionRecords": [
          | ]
          |}
    """.stripMargin
      )
      val result = emptyRecordsJson.validate[ProtectionRecordDetails]
      result mustBe a[JsSuccess[_]]
      result.get mustBe ProtectionRecordDetails(Seq.empty)
    }
  }

  "ordered" -> {
    "should order protections and enhancements correctly" in {
      dummyProtectionRecords.ordered.map(_.status) mustBe Seq(Active, Active, Dormant, Withdrawn)
    }

    "should order protections and enhancements with no active record" in {
      nonActive.ordered.map(_.status) mustBe Seq(Dormant)
    }
  }
}
