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

import models.response.RecordStatusMapped.{Active, Dormant, Withdrawn}
import play.api.libs.json.Reads
import utils.enums.Enums

sealed trait RecordStatus {
  def toMapped: RecordStatusMapped
}

object RecordStatus {
  case object OPEN extends RecordStatus {
    override def toMapped: RecordStatusMapped = Active
  }

  case object DORMANT extends RecordStatus {
    override def toMapped: RecordStatusMapped = Dormant
  }

  case object WITHDRAWN extends RecordStatus{
    override def toMapped: RecordStatusMapped = Withdrawn
  }

  implicit val reads: Reads[RecordStatus] = Enums.reads[RecordStatus]
}
