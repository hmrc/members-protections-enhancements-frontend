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

sealed abstract case class ProtectionStatusMapped(messagesKey: String, colourString: String) {
  private val baseMessagesString: String = "results.status"

  val toNameMessagesString: String = baseMessagesString + messagesKey + ".name"
  val toDescriptionMessagesString: String = baseMessagesString + messagesKey + ".message"
}

object ProtectionStatusMapped {
  object Active extends ProtectionStatusMapped(messagesKey = "active", colourString = "green")
  object Dormant extends ProtectionStatusMapped(messagesKey = "dormant", colourString = "yellow")
  object Withdrawn extends ProtectionStatusMapped(messagesKey = "withdrawn", colourString = "red")

  implicit val reads: Reads[ProtectionStatusMapped] = ProtectionStatus.reads.map(_.toMapped)
}
