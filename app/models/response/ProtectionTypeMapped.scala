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

sealed abstract case class ProtectionTypeMapped(messagesKey: String) {
  val toMessagesString: String = s"results.protection.$messagesKey"
}

object ProtectionTypeMapped {
  object FixedProtection extends ProtectionTypeMapped("FP")
  object FixedProtection2014  extends ProtectionTypeMapped("FP.2014")
  object FixedProtection2016 extends ProtectionTypeMapped("FP.2016")
  object IndividualProtection2014 extends ProtectionTypeMapped("IP.2014")
  object IndividualProtection2016 extends ProtectionTypeMapped("IP.2016")
  object PrimaryProtection extends ProtectionTypeMapped("PP")
  object EnhancedProtection extends ProtectionTypeMapped("EP")
  object PensionCreditRightsPreCommencement extends ProtectionTypeMapped("PCR.COM")
  object PensionCreditRightsPreviouslyCrystallised extends ProtectionTypeMapped("PCR.CRY")
  object InternationalEnhancementRelevantIndividual extends ProtectionTypeMapped("IE.RI")
  object InternationalEnhancementTransfer extends ProtectionTypeMapped("IE.T")

  implicit val reads: Reads[ProtectionTypeMapped] = ProtectionType.reads.map(_.toMapped)
}
