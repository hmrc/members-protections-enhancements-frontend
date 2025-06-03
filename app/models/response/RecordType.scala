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
import play.api.libs.json.Reads
import utils.enums.Enums

sealed trait RecordType {
  def toMapped: RecordTypeMapped
}

object RecordType {
  case object `FIXED PROTECTION` extends RecordType {
    override def toMapped: RecordTypeMapped = FixedProtection
  }

  case object `FIXED PROTECTION 2014` extends RecordType {
    override def toMapped: RecordTypeMapped = FixedProtection2014
  }
  case object `FIXED PROTECTION 2016` extends RecordType {
    override def toMapped: RecordTypeMapped = FixedProtection2016
  }

  case object `INDIVIDUAL PROTECTION 2014` extends RecordType {
    override def toMapped: RecordTypeMapped = IndividualProtection2014
  }

  case object `INDIVIDUAL PROTECTION 2016` extends RecordType {
    override def toMapped: RecordTypeMapped = IndividualProtection2016
  }

  case object `PRIMARY PROTECTION` extends RecordType {
    override def toMapped: RecordTypeMapped = PrimaryProtection
  }

  case object `ENHANCED PROTECTION` extends RecordType {
    override def toMapped: RecordTypeMapped = EnhancedProtection
  }

  case object `PENSION CREDIT RIGHTS P18` extends RecordType {
    override def toMapped: RecordTypeMapped = PensionCreditRightsPreCommencement
  }

  case object `PENSION CREDIT RIGHTS S220` extends RecordType {
    override def toMapped: RecordTypeMapped = PensionCreditRightsPreviouslyCrystallised
  }

  case object `INTERNATIONAL ENHANCEMENT S221` extends RecordType {
    override def toMapped: RecordTypeMapped = InternationalEnhancementRelevantIndividual
  }

  case object `INTERNATIONAL ENHANCEMENT S224` extends RecordType {
    override def toMapped: RecordTypeMapped = InternationalEnhancementTransfer
  }

  implicit val reads: Reads[RecordType] = Enums.reads[RecordType]
}
