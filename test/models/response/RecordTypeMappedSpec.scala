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

package models.response

import base.SpecBase
import models.response.PensionCreditLegislation.{`PARAGRAPH 18 SCHEDULE 36 FINANCE ACT 2004`, `SECTION 220 FINANCE ACT 2004`}
import models.response.RecordType.`PENSION CREDIT RIGHTS`
import models.response.RecordTypeMapped._
import play.api.libs.json.{JsError, JsResult, JsString, JsSuccess, JsValue, Json, JsonValidationError}

class RecordTypeMappedSpec extends SpecBase {
  def toTypeJsString: String => JsValue = str => Json.parse(s"""{"type": "$str"}""")

  "round test" -> {
    val values: Seq[(String, RecordTypeMapped)] = Seq(
      "FIXED PROTECTION" -> FixedProtection,
      "FIXED PROTECTION 2014" -> FixedProtection2014,
      "FIXED PROTECTION 2016" -> FixedProtection2016,
      "INDIVIDUAL PROTECTION 2014" -> IndividualProtection2014,
      "INDIVIDUAL PROTECTION 2016" -> IndividualProtection2016,
      "ENHANCED PROTECTION" -> EnhancedProtection,
      "PRIMARY PROTECTION" -> PrimaryProtection,
      "INTERNATIONAL ENHANCEMENT S221" -> InternationalEnhancementRelevantIndividual,
      "INTERNATIONAL ENHANCEMENT S224" -> InternationalEnhancementTransfer,
      // Also check that LTA protections are correctly read and mapped
      "FIXED PROTECTION LTA" -> FixedProtection,
      "FIXED PROTECTION 2014 LTA" -> FixedProtection2014,
      "FIXED PROTECTION 2016 LTA" -> FixedProtection2016,
      "INDIVIDUAL PROTECTION 2014 LTA" -> IndividualProtection2014,
      "INDIVIDUAL PROTECTION 2016 LTA" -> IndividualProtection2016,
      "ENHANCED PROTECTION LTA" -> EnhancedProtection,
      "PRIMARY PROTECTION LTA" -> PrimaryProtection,
    )

    for ((stringValue, expectedModel) <- values) enumRoundTest(stringValue, toTypeJsString, expectedModel)
  }

  "should not read an enhancement with the LTA suffix" in {
    val result: JsResult[RecordTypeMapped] = toTypeJsString("PENSION CREDIT RIGHTS P18 LTA").validate[RecordTypeMapped]
    result mustBe a[JsError]
    result.recover {
      case err: JsError =>
        err.errors must have length 1
        val (path, msgs) = err.errors.head
        path.toString() mustBe "/type"
        msgs must have length 1
        msgs.head.message mustBe "error.expected.RecordType"
    }
  }

  "for a type of `PENSION CREDIT RIGHTS`" - {
    "should read when P18 legislation field is included" in {
      val json: JsValue = Json.parse(
        """
          |{
          | "type": "PENSION CREDIT RIGHTS",
          | "pensionCreditLegislation": "PARAGRAPH 18 SCHEDULE 36 FINANCE ACT 2004"
          |}
        """.stripMargin
      )

      val result = json.validate[RecordTypeMapped]
      result mustBe a[JsSuccess[_]]
      result.get mustBe PcrPreCommencement
    }

    "should read when Section 220 legislation field is included" in {
      val json: JsValue = Json.parse(
        """
          |{
          | "type": "PENSION CREDIT RIGHTS",
          | "pensionCreditLegislation": "SECTION 220 FINANCE ACT 2004"
          |}
        """.stripMargin
      )

      val result = json.validate[RecordTypeMapped]
      result mustBe a[JsSuccess[_]]
      result.get mustBe PcrPreviouslyCrystallised
    }
  }
}
