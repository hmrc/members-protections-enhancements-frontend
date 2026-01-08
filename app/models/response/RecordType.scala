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

import models.response.PensionCreditLegislation.{`PARAGRAPH 18 SCHEDULE 36 FINANCE ACT 2004`, `SECTION 220 FINANCE ACT 2004`}
import models.response.RecordTypeMapped._
import play.api.libs.functional.syntax.toFunctionalBuilderOps
import play.api.libs.json._

sealed trait RecordType {
  val mapping: RecordTypeMapped
}

sealed trait Protection extends RecordType
sealed trait Enhancement extends RecordType

object RecordType {
  /**
   * Types of Protections & Enhancements
   *
   * "INDIVIDUAL PROTECTION 2014"
   * "INDIVIDUAL PROTECTION 2014 LTA"
   * "INDIVIDUAL PROTECTION 2016"
   * "INDIVIDUAL PROTECTION 2016 LTA"
   * "PRIMARY PROTECTION"
   * "PRIMARY PROTECTION LTA"
   * "ENHANCED PROTECTION"
   * "ENHANCED PROTECTION LTA"
   * "FIXED PROTECTION"
   * "FIXED PROTECTION LTA"
   * "FIXED PROTECTION 2014"
   * "FIXED PROTECTION 2014 LTA"
   * "FIXED PROTECTION 2016"
   * "FIXED PROTECTION 2016 LTA"
   * "PENSION CREDIT RIGHTS"
   * "INTERNATIONAL ENHANCEMENT (S221)"
   * "INTERNATIONAL ENHANCEMENT (S224)"
   */
  case object `FIXED PROTECTION` extends Protection {
    override val mapping: RecordTypeMapped = FixedProtection
  }

  case object `FIXED PROTECTION 2014` extends Protection {
    override val mapping: RecordTypeMapped = FixedProtection2014
  }
  case object `FIXED PROTECTION 2016` extends Protection {
    override val mapping: RecordTypeMapped = FixedProtection2016
  }

  case object `INDIVIDUAL PROTECTION 2014` extends Protection {
    override val mapping: RecordTypeMapped = IndividualProtection2014
  }

  case object `INDIVIDUAL PROTECTION 2016` extends Protection {
    override val mapping: RecordTypeMapped = IndividualProtection2016
  }

  case object `PRIMARY PROTECTION` extends Protection {
    override val mapping: RecordTypeMapped = PrimaryProtection
  }

  case object `ENHANCED PROTECTION` extends Protection {
    override val mapping: RecordTypeMapped = EnhancedProtection
  }

  case object `INTERNATIONAL ENHANCEMENT S221` extends Enhancement {
    override val mapping: RecordTypeMapped = InternationalEnhancementRelevantIndividual
  }

  case object `INTERNATIONAL ENHANCEMENT S224` extends Enhancement {
    override val mapping: RecordTypeMapped = InternationalEnhancementTransfer
  }

  case class `PENSION CREDIT RIGHTS`(legislation: PensionCreditLegislation) extends RecordType {
    override val mapping: RecordTypeMapped = legislation match {
      case `PARAGRAPH 18 SCHEDULE 36 FINANCE ACT 2004` => PcrPreCommencement
      case `SECTION 220 FINANCE ACT 2004` => PcrPreviouslyCrystallised
    }
  }

  implicit val reads: Reads[RecordType] = (json: JsValue) => {
    val pensionCreditRightsString: String = "PENSION CREDIT RIGHTS"

    val enumReadsProtection: Reads[Protection] = Reads[Protection] {
      case JsString("FIXED PROTECTION") => JsSuccess(`FIXED PROTECTION`)
      case JsString("FIXED PROTECTION 2014") => JsSuccess(`FIXED PROTECTION 2014`)
      case JsString("FIXED PROTECTION 2016") => JsSuccess(`FIXED PROTECTION 2016`)
      case JsString("INDIVIDUAL PROTECTION 2014") => JsSuccess(`INDIVIDUAL PROTECTION 2014`)
      case JsString("INDIVIDUAL PROTECTION 2016") => JsSuccess(`INDIVIDUAL PROTECTION 2016`)
      case JsString("ENHANCED PROTECTION") => JsSuccess(`ENHANCED PROTECTION`)
      case JsString("PRIMARY PROTECTION") => JsSuccess(`PRIMARY PROTECTION`)
      case JsString("FIXED PROTECTION LTA") => JsSuccess(`FIXED PROTECTION`)
      case JsString("FIXED PROTECTION 2014 LTA") => JsSuccess(`FIXED PROTECTION 2014`)
      case JsString("FIXED PROTECTION 2016 LTA") => JsSuccess(`FIXED PROTECTION 2016`)
      case JsString("INDIVIDUAL PROTECTION 2014 LTA") => JsSuccess(`INDIVIDUAL PROTECTION 2014`)
      case JsString("INDIVIDUAL PROTECTION 2016 LTA") => JsSuccess(`INDIVIDUAL PROTECTION 2016`)
      case JsString("ENHANCED PROTECTION LTA") => JsSuccess(`ENHANCED PROTECTION`)
      case JsString("PRIMARY PROTECTION LTA") => JsSuccess(`PRIMARY PROTECTION`)
      case _ => JsError("error.recordType.invalid")
    }

    val enumReadsEnhancement: Reads[Enhancement] = Reads[Enhancement]{
      case JsString("INTERNATIONAL ENHANCEMENT S221") => JsSuccess(`INTERNATIONAL ENHANCEMENT S221`)
      case JsString("INTERNATIONAL ENHANCEMENT S224") => JsSuccess(`INTERNATIONAL ENHANCEMENT S224`)
      case _ => JsError("error.recordType.invalid")
    }

    val typeReads: Reads[JsString] = (JsPath \ "type").read[JsString]
    val fallbackErrorReads: Reads[RecordType] = Reads.failed("error.expected.RecordType")

    val protectionRecordReads: Reads[RecordType] = (json: JsValue) => {
      json
        .validate[Protection](enumReadsProtection)
        .orElse(
          json.validate[JsString]
            .map(jsString => JsString(jsString.value.replace(" LTA", "").replaceAll("\\((.*?)\\)", "$1")))
            .flatMap(_.validate[Protection](enumReadsProtection))
        )
    }

    val enhancementRecordReads: Reads[RecordType] = (json: JsValue) => {
      json
        .validate[Enhancement](enumReadsEnhancement)
        .orElse(
          json.validate[JsString]
            .map(jsString => JsString(jsString.value.replaceAll("\\((.*?)\\)", "$1")))
            .flatMap(_.validate[Enhancement](enumReadsEnhancement))
        )
    }

    val pcrReads: Reads[RecordType] = (
      typeReads and
        (JsPath \ "pensionCreditLegislation").read[PensionCreditLegislation]
      )((_, legislation) => `PENSION CREDIT RIGHTS`(legislation))

    val combinedReads: Reads[RecordType] = typeReads.flatMap {
      case JsString(`pensionCreditRightsString`) => pcrReads
      case _ => typeReads.andThen(enhancementRecordReads orElse protectionRecordReads orElse fallbackErrorReads)
    }

    json.validate[RecordType](combinedReads)
  }
}
