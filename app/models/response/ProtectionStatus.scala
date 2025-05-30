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

import models.response.ProtectionStatusMapped.{Active, Dormant, Withdrawn}
import play.api.libs.json.Reads
import utils.enums.Enums

sealed trait ProtectionStatus {
  def toMapped: ProtectionStatusMapped
}

object ProtectionStatus {
  case object OPEN extends ProtectionStatus {
    override def toMapped: ProtectionStatusMapped = Active
  }

  case object DORMANT extends ProtectionStatus {
    override def toMapped: ProtectionStatusMapped = Dormant
  }

  case object WITHDRAWN extends ProtectionStatus{
    override def toMapped: ProtectionStatusMapped = Withdrawn
  }

  implicit val reads: Reads[ProtectionStatus] = Enums.reads[ProtectionStatus]
}
