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
      "INTERNATIONAL ENHANCEMENT S224" -> `INTERNATIONAL ENHANCEMENT S224`
    )

    for ((stringValue, expectedModel) <- values) enumRoundTest(stringValue, expectedModel)
  }

}
