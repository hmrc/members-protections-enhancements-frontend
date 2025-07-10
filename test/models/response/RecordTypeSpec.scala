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
import models.response.RecordType._
import play.api.libs.json.{JsError, JsResult, JsString, JsonValidationError}

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
      "PENSION CREDIT RIGHTS P18" -> `PENSION CREDIT RIGHTS P18`,
      "PENSION CREDIT RIGHTS S220" -> `PENSION CREDIT RIGHTS S220`,
      "INTERNATIONAL ENHANCEMENT S221" -> `INTERNATIONAL ENHANCEMENT S221`,
      "INTERNATIONAL ENHANCEMENT S224" -> `INTERNATIONAL ENHANCEMENT S224`,
      "PENSION CREDIT RIGHTS" -> `PENSION CREDIT RIGHTS`,
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
    val result: JsResult[RecordType] = JsString("PENSION CREDIT RIGHTS P18 LTA").validate[RecordType]
    result mustBe a[JsError]
    result mustBe JsError(JsonValidationError("error.expected.RecordType"))
  }

}
