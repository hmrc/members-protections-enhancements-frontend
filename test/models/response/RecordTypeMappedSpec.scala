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
import models.response.RecordTypeMapped._
import play.api.libs.json.{JsError, JsResult, JsString, JsonValidationError}

class RecordTypeMappedSpec extends SpecBase {
  "round test" -> {
    val values: Seq[(String, RecordTypeMapped)] = Seq(
      "FIXED PROTECTION" -> FixedProtection,
      "FIXED PROTECTION 2014" -> FixedProtection2014,
      "FIXED PROTECTION 2016" -> FixedProtection2016,
      "INDIVIDUAL PROTECTION 2014" -> IndividualProtection2014,
      "INDIVIDUAL PROTECTION 2016" -> IndividualProtection2016,
      "ENHANCED PROTECTION" -> EnhancedProtection,
      "PRIMARY PROTECTION" -> PrimaryProtection,
      "PENSION CREDIT RIGHTS P18" -> PensionCreditRightsPreCommencement,
      "PENSION CREDIT RIGHTS S220" -> PensionCreditRightsPreviouslyCrystallised,
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

    for ((stringValue, expectedModel) <- values) enumRoundTest(stringValue, expectedModel)
  }

  "should not read an enhancement with the LTA suffix" in {
    val result: JsResult[RecordType] = JsString("PENSION CREDIT RIGHTS P18 LTA").validate[RecordType]
    result mustBe a[JsError]
    result mustBe JsError(JsonValidationError("error.expected.RecordType"))
  }
}
