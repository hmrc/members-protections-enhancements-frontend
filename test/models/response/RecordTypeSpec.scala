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
import models.response.RecordType._
import play.api.libs.json.{JsError, JsResult, JsString, JsSuccess, JsValue, Json, JsonValidationError}

class RecordTypeSpec extends SpecBase {
  "round test" -> {
    val values: Seq[(String, RecordType)] = Seq(
      "FIXED PROTECTION" -> `FIXED PROTECTION`,
      "FIXED PROTECTION 2014" -> `FIXED PROTECTION 2014`,
      "FIXED PROTECTION 2016" -> `FIXED PROTECTION 2016`,
      "INDIVIDUAL PROTECTION 2014" -> `INDIVIDUAL PROTECTION 2014`,
      "INDIVIDUAL PROTECTION 2016" -> `INDIVIDUAL PROTECTION 2016`,
      "ENHANCED PROTECTION" -> `ENHANCED PROTECTION`,
      "PRIMARY PROTECTION" -> `PRIMARY PROTECTION`,
      "INTERNATIONAL ENHANCEMENT S221" -> `INTERNATIONAL ENHANCEMENT S221`,
      "INTERNATIONAL ENHANCEMENT S224" -> `INTERNATIONAL ENHANCEMENT S224`,
      // Also check that LTA protections are correctly read and mapped
      "FIXED PROTECTION LTA" -> `FIXED PROTECTION`,
      "FIXED PROTECTION 2014 LTA" -> `FIXED PROTECTION 2014`,
      "FIXED PROTECTION 2016 LTA" -> `FIXED PROTECTION 2016`,
      "INDIVIDUAL PROTECTION 2014 LTA" -> `INDIVIDUAL PROTECTION 2014`,
      "INDIVIDUAL PROTECTION 2016 LTA" -> `INDIVIDUAL PROTECTION 2016`,
      "ENHANCED PROTECTION LTA" -> `ENHANCED PROTECTION`,
      "PRIMARY PROTECTION LTA" -> `PRIMARY PROTECTION`
    )

    for ((stringValue, expectedModel) <- values) enumRoundTest(stringValue, expectedModel)
  }

  "should not read an enhancement with the LTA suffix" in {
    val result: JsResult[RecordType] = JsString("PENSION CREDIT RIGHTS LTA").validate[RecordType]
    result mustBe a[JsError]
    result mustBe JsError(JsonValidationError("error.expected.RecordType"))
  }

  "pcrReads" - {
    "should read for PENSION CREDIT RIGHTS when P18 legislation field is included" in {
      val json: JsValue = Json.parse(
        """
          |{
          | "type": "PENSION CREDIT RIGHTS",
          | "pensionCreditLegislation": "PARAGRAPH 18 SCHEDULE 36 FINANCE ACT 2004"
          |}
        """.stripMargin
      )

      val result = json.validate[`PENSION CREDIT RIGHTS`]
      result mustBe a[JsSuccess[_]]
      result.get mustBe `PENSION CREDIT RIGHTS`(`PARAGRAPH 18 SCHEDULE 36 FINANCE ACT 2004`)
    }

    "should read for PENSION CREDIT RIGHTS when Section 220 legislation field is included" in {
      val json: JsValue = Json.parse(
        """
          |{
          | "type": "PENSION CREDIT RIGHTS",
          | "pensionCreditLegislation": "SECTION 220 FINANCE ACT 2004"
          |}
        """.stripMargin
      )

      val result = json.validate[`PENSION CREDIT RIGHTS`]
      result mustBe a[JsSuccess[_]]
      result.get mustBe `PENSION CREDIT RIGHTS`(`SECTION 220 FINANCE ACT 2004`)
    }

    "should throw an error when legislation field is missing" in {
      val json: JsValue = Json.parse(
        """
          |{
          | "type": "PENSION CREDIT RIGHTS"
          |}
        """.stripMargin
      )

      val result = json.validate[`PENSION CREDIT RIGHTS`]
      result mustBe a[JsError]
      result.recover {
        case err: JsError =>
          err.errors must have length 1
          val (jsPath, validationErrors) = err.errors.head
          jsPath.toString() mustBe "/pensionCreditLegislation"
          validationErrors.head.messages mustBe List("error.path.missing")
      }
    }

    "should throw an error when legislation field is unparsable" in {
      val json: JsValue = Json.parse(
        """
          |{
          | "type": "PENSION CREDIT RIGHTS",
          | "pensionCreditLegislation": "N/A"
          |}
        """.stripMargin
      )

      val result = json.validate[`PENSION CREDIT RIGHTS`]
      result mustBe a[JsError]
      result.recover {
        case err: JsError =>
          val (jsPath, validationErrors) = err.errors.head
          jsPath.toString() mustBe "/pensionCreditLegislation"
          validationErrors.head.messages mustBe List("error.expected.PensionCreditLegislation")
      }
    }

    "should throw an error when type field is missing" in {
      val json: JsValue = Json.parse(
        """
          |{
          | "pensionCreditLegislation": "SECTION 220 FINANCE ACT 2004"
          |}
        """.stripMargin
      )

      val result = json.validate[`PENSION CREDIT RIGHTS`]
      result mustBe a[JsError]
      result.recover {
        case err: JsError =>
          val (jsPath, validationErrors) = err.errors.head
          jsPath.toString() mustBe "/type"
          validationErrors.head.messages mustBe List("error.path.missing")
      }
    }

    "should throw an error when type field is unparsable" in {
      val json: JsValue = Json.parse(
        """
          |{
          | "type": "CREDIT RIGHTS",
          | "pensionCreditLegislation": "SECTION 220 FINANCE ACT 2004"
          |}
        """.stripMargin
      )

      val result = json.validate[`PENSION CREDIT RIGHTS`]
      result mustBe a[JsError]
      result.recover {
        case err: JsError =>
          val (jsPath, _) = err.errors.head
          jsPath.toString() mustBe "/type"
      }
    }
  }
}
