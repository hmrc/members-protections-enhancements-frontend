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

sealed trait ProtectionStatusMapped {
  val messagesKey: String
  val colourString: String

  lazy val toNameContentString: String = messagesKey + ".name"
  lazy val toMessageContentString: String = messagesKey + ".message"
}

object ProtectionStatusMapped {
  case object Active extends ProtectionStatusMapped {
    override val messagesKey: String = "results.status.active"
    override val colourString: String = "green"
  }

  case object Dormant extends ProtectionStatusMapped {
    override val messagesKey: String = "results.status.dormant"
    override val colourString: String = "yellow"
  }

  case object Withdrawn extends ProtectionStatusMapped {
    override val messagesKey: String = "results.status.withdrawn"
    override val colourString: String = "red"
  }

  implicit val reads: Reads[ProtectionStatusMapped] = ProtectionStatus.reads.map(_.toMapped)
}
