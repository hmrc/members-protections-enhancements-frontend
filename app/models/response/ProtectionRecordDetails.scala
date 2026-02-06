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

import models.response.RecordStatusMapped.*
import play.api.libs.json.{Json, Reads}

case class ProtectionRecordDetails(protectionRecords: Seq[ProtectionRecord]) {
  def ordered: Seq[ProtectionRecord] = {
    val groupedProtectionRecords = protectionRecords
      .groupBy(_.status)

    groupedProtectionRecords.getOrElse(Active, Nil) ++
      groupedProtectionRecords.getOrElse(Dormant, Nil) ++
      groupedProtectionRecords.getOrElse(Withdrawn, Nil)
  }
}

object ProtectionRecordDetails {
  implicit val reads: Reads[ProtectionRecordDetails] = Json.reads[ProtectionRecordDetails]
}
