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
import models.response.PensionCreditLegislation.{
  `PARAGRAPH 18 SCHEDULE 36 FINANCE ACT 2004`,
  `SECTION 220 FINANCE ACT 2004`
}

class PensionCreditLegislationSpec extends SpecBase {
  "round test" -> {
    val values: Seq[(String, PensionCreditLegislation)] = Seq(
      "PARAGRAPH 18 SCHEDULE 36 FINANCE ACT 2004" -> `PARAGRAPH 18 SCHEDULE 36 FINANCE ACT 2004`,
      "SECTION 220 FINANCE ACT 2004" -> `SECTION 220 FINANCE ACT 2004`
    )

    for ((stringValue, expectedModel) <- values) enumRoundTest(stringValue, expectedModel)
  }

}
