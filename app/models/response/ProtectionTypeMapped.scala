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

import play.api.libs.json.Reads

sealed trait ProtectionTypeMapped {
  val messagesKey: String

  lazy val toContentString: String = s"results.protection.$messagesKey"
}

object ProtectionTypeMapped {
  case object FixedProtection extends ProtectionTypeMapped {
    override val messagesKey: String = "FP"
  }

  case object FixedProtection2014  extends ProtectionTypeMapped {
    override val messagesKey: String = "FP.2014"
  }

  case object FixedProtection2016 extends ProtectionTypeMapped {
    override val messagesKey: String = "FP.2016"
  }

  case object IndividualProtection2014 extends ProtectionTypeMapped {
    override val messagesKey: String = "IP.2014"
  }

  case object IndividualProtection2016 extends ProtectionTypeMapped {
    override val messagesKey: String = "IP.2016"
  }

  case object PrimaryProtection extends ProtectionTypeMapped {
    override val messagesKey: String = "PP"
  }

  case object EnhancedProtection extends ProtectionTypeMapped {
    override val messagesKey: String = "EP"
  }

  case object PensionCreditRightsPreCommencement extends ProtectionTypeMapped {
    override val messagesKey: String = "PCR.COM"
  }

  case object PensionCreditRightsPreviouslyCrystallised extends ProtectionTypeMapped {
    override val messagesKey: String = "PCR.CRY"
  }

  case object InternationalEnhancementRelevantIndividual extends ProtectionTypeMapped {
    override val messagesKey: String = "IE.RI"
  }

  case object InternationalEnhancementTransfer extends ProtectionTypeMapped {
    override val messagesKey: String = "IE.T"
  }

  implicit val reads: Reads[ProtectionTypeMapped] = ProtectionType.reads.map(_.toMapped)
}
