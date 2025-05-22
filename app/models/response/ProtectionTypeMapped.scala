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


sealed trait ProtectionTypeMapped

object ProtectionTypeMapped {
  case object FixedProtection extends ProtectionTypeMapped
  case object FixedProtection2014  extends ProtectionTypeMapped
  case object FixedProtection2016 extends ProtectionTypeMapped
  case object IndividualProtection2014 extends ProtectionTypeMapped
  case object IndividualProtection2016 extends ProtectionTypeMapped
  case object PrimaryProtection extends ProtectionTypeMapped
  case object EnhancedProtection extends ProtectionTypeMapped
  case object PensionCreditRightsPreCommencement extends ProtectionTypeMapped
  case object PensionCreditRightsPreviouslyCrystallised extends ProtectionTypeMapped
  case object InternationalEnhancementRelevantIndividual extends ProtectionTypeMapped
  case object InternationalEnhancementTransfer extends ProtectionTypeMapped

  implicit val reads: Reads[ProtectionTypeMapped] = ProtectionType.reads.map(_.toMapped)
}
