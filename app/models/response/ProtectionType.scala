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

import models.response.ProtectionTypeMapped._
import play.api.libs.json.Reads
import utils.enums.Enums

sealed trait ProtectionType {
  def toMapped: ProtectionTypeMapped
}

object ProtectionType {
  case object `FIXED PROTECTION` extends ProtectionType {
    override def toMapped: ProtectionTypeMapped = FixedProtection
  }

  case object `FIXED PROTECTION 2014` extends ProtectionType {
    override def toMapped: ProtectionTypeMapped = FixedProtection2014
  }
  case object `FIXED PROTECTION 2016` extends ProtectionType {
    override def toMapped: ProtectionTypeMapped = FixedProtection2016
  }

  case object `INDIVIDUAL PROTECTION 2014` extends ProtectionType {
    override def toMapped: ProtectionTypeMapped = IndividualProtection2014
  }

  case object `INDIVIDUAL PROTECTION 2016` extends ProtectionType {
    override def toMapped: ProtectionTypeMapped = IndividualProtection2016
  }

  case object `PRIMARY PROTECTION` extends ProtectionType {
    override def toMapped: ProtectionTypeMapped = PrimaryProtection
  }

  case object `ENHANCED PROTECTION` extends ProtectionType {
    override def toMapped: ProtectionTypeMapped = EnhancedProtection
  }

  case object `PENSION CREDIT RIGHTS P18` extends ProtectionType {
    override def toMapped: ProtectionTypeMapped = PensionCreditRightsPreCommencement
  }

  case object `PENSION CREDIT RIGHTS S220` extends ProtectionType {
    override def toMapped: ProtectionTypeMapped = PensionCreditRightsPreviouslyCrystallised
  }

  case object `INTERNATIONAL ENHANCEMENT S221` extends ProtectionType {
    override def toMapped: ProtectionTypeMapped = InternationalEnhancementRelevantIndividual
  }

  case object `INTERNATIONAL ENHANCEMENT S224` extends ProtectionType {
    override def toMapped: ProtectionTypeMapped = InternationalEnhancementTransfer
  }

  implicit val reads: Reads[ProtectionType] = Enums.reads[ProtectionType]
}
