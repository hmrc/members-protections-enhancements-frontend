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

import models.response.RecordTypeMapped._
import play.api.libs.json.{JsError, JsResult, JsString, JsValue, JsonValidationError, Reads}
import utils.enums.Enums

sealed trait RecordType {
  def toMapped: RecordTypeMapped
}

sealed trait Protection extends RecordType
sealed trait Enhancement extends RecordType

object RecordType {
  /**
   * Types of Protections & Enhancements
   *
   * "FIXED PROTECTION 2016"
   * "INDIVIDUAL PROTECTION 2014"
   * "INDIVIDUAL PROTECTION 2016"
   * "PRIMARY PROTECTION"
   * "ENHANCED PROTECTION"
   * "FIXED PROTECTION"
   * "FIXED PROTECTION 2014"
   * "PENSION CREDIT RIGHTS"
   * "INTERNATIONAL ENHANCEMENT (S221)"
   * "INTERNATIONAL ENHANCEMENT (S224)"
   * "FIXED PROTECTION 2016 LTA"
   * "INDIVIDUAL PROTECTION 2014 LTA"
   * "INDIVIDUAL PROTECTION 2016 LTA"
   * "PRIMARY PROTECTION LTA"
   * "ENHANCED PROTECTION LTA"
   * "FIXED PROTECTION LTA"
   * "FIXED PROTECTION 2014 LTA"
   */
  case object `FIXED PROTECTION` extends Protection {
    override def toMapped: RecordTypeMapped = FixedProtection
  }

  case object `FIXED PROTECTION 2014` extends Protection {
    override def toMapped: RecordTypeMapped = FixedProtection2014
  }
  case object `FIXED PROTECTION 2016` extends Protection {
    override def toMapped: RecordTypeMapped = FixedProtection2016
  }

  case object `INDIVIDUAL PROTECTION 2014` extends Protection {
    override def toMapped: RecordTypeMapped = IndividualProtection2014
  }

  case object `INDIVIDUAL PROTECTION 2016` extends Protection {
    override def toMapped: RecordTypeMapped = IndividualProtection2016
  }

  case object `PRIMARY PROTECTION` extends Protection {
    override def toMapped: RecordTypeMapped = PrimaryProtection
  }

  case object `ENHANCED PROTECTION` extends Protection {
    override def toMapped: RecordTypeMapped = EnhancedProtection
  }

  case object `PENSION CREDIT RIGHTS P18` extends Enhancement {
    override def toMapped: RecordTypeMapped = PensionCreditRightsPreCommencement
  }

  case object `PENSION CREDIT RIGHTS` extends Enhancement {
    override def toMapped: RecordTypeMapped = PensionCreditRights
  }

  case object `PENSION CREDIT RIGHTS S220` extends Enhancement {
    override def toMapped: RecordTypeMapped = PensionCreditRightsPreviouslyCrystallised
  }

  case object `INTERNATIONAL ENHANCEMENT S221` extends Enhancement {
    override def toMapped: RecordTypeMapped = InternationalEnhancementRelevantIndividual
  }

  case object `INTERNATIONAL ENHANCEMENT S224` extends Enhancement {
    override def toMapped: RecordTypeMapped = InternationalEnhancementTransfer
  }

  implicit val reads: Reads[RecordType] = (json: JsValue) => {
    val enumReadsProtection: Reads[Protection] = Enums.reads[Protection]
    val enumReadsEnhancement: Reads[Enhancement] = Enums.reads[Enhancement]

    val protectionCombinedReads: JsResult[Protection] =
      json
        .validate[Protection](enumReadsProtection)
        .orElse(
          json.validate[JsString]
            .map(jsString =>
              JsString(jsString.value.replace(" LTA", "").replaceAll("\\((.*?)\\)", "$1")))
            .flatMap(_.validate[Protection](enumReadsProtection))
        )

    json.validate[Enhancement](enumReadsEnhancement)
      .orElse(
        json.validate[JsString]
          .map(jsString =>
            JsString(jsString.value.replaceAll("\\((.*?)\\)", "$1")))
          .flatMap(_.validate[Enhancement](enumReadsEnhancement)))
      .orElse(protectionCombinedReads)
      .orElse(JsError(JsonValidationError("error.expected.RecordType")))
  }
}
