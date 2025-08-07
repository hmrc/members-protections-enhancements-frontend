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

package models.userAnswers

import play.api.libs.functional.syntax._
import play.api.libs.json._
import uk.gov.hmrc.crypto.EncryptedValue
import uk.gov.hmrc.mongo.play.json.formats.MongoJavatimeFormats
import utils.encryption.AesGcmAdCrypto
import utils.encryption.Cypher.jsObjectCypher
import utils.encryption.CypherSyntax._

import java.time.Instant

final case class EncryptedUserAnswers(id: String,
                                      encryptedValue: EncryptedValue,
                                      lastUpdated: Instant = Instant.now) {
  def toUserAnswers(implicit aesGcmAdCrypto: AesGcmAdCrypto, associatedText: String): UserAnswers = UserAnswers(
    id = id,
    data = encryptedValue.decrypted[JsObject],
    lastUpdated = lastUpdated
  )
}

object EncryptedUserAnswers {
  implicit lazy val encryptedValueOFormat: OFormat[EncryptedValue] = Json.format[EncryptedValue]

  val reads: Reads[EncryptedUserAnswers] = (
    (__ \ "_id").read[String] and
      (__ \ "data").read[EncryptedValue] and
      (__ \ "lastUpdated").read(MongoJavatimeFormats.instantFormat)
    ) (EncryptedUserAnswers.apply _)

  val writes: OWrites[EncryptedUserAnswers] =
    (
      (__ \ "_id").write[String] and
        (__ \ "data").write[EncryptedValue] and
        (__ \ "lastUpdated").write(MongoJavatimeFormats.instantFormat)
      ) (ua => (ua.id, ua.encryptedValue, ua.lastUpdated))

  implicit val format: OFormat[EncryptedUserAnswers] = OFormat(reads, writes)
}