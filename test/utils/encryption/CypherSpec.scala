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

package utils.encryption

import base.SpecBase
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.libs.json.{JsObject, Json}
import uk.gov.hmrc.crypto.EncryptedValue
import utils.encryption.Cypher.{jsObjectCypher, stringCypher}

class CypherSpec extends SpecBase {

  private val encryptedString = mock[EncryptedValue]
  private val encryptedJsObject = mock[EncryptedValue]
  private val encryptedValue = EncryptedValue("some-value", "some-nonce")

  private implicit val aesGcmAdCrypto: AesGcmAdCrypto = mock[AesGcmAdCrypto]
  private implicit val associatedText: String = "some-associated-text"

  "stringCypher" - {
    val stringValue = "some-string-value"
    "encrypt string values" in {
      when(
        aesGcmAdCrypto.encrypt(ArgumentMatchers.any())(ArgumentMatchers.any())
      ).thenReturn(
        encryptedString
      )

      stringCypher.encrypt(stringValue) mustBe encryptedString
    }

    "decrypt to string values" in {
      when(
        aesGcmAdCrypto.decrypt(ArgumentMatchers.any())(ArgumentMatchers.any())
      ).thenReturn(
        stringValue
      )

      stringCypher.decrypt(encryptedValue) mustBe stringValue
    }
  }

  "jsObjectCypher" - {
    val jsValue: JsObject = Json.obj("foo" -> "bar")
    "encrypt JS values" in {
      when(
        aesGcmAdCrypto.encrypt(ArgumentMatchers.any())(ArgumentMatchers.any())
      ).thenReturn(
        encryptedJsObject
      )

      jsObjectCypher.encrypt(jsValue) mustBe encryptedJsObject
    }

    "decrypt to JS values" in {
      when(
        aesGcmAdCrypto.decrypt(ArgumentMatchers.any())(ArgumentMatchers.any())
      ).thenReturn(
        jsValue.toString()
      )

      jsObjectCypher.decrypt(encryptedValue) mustBe jsValue
    }
  }

}