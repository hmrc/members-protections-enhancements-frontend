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

package utils.encryption

import uk.gov.hmrc.crypto.{AdDecrypter, AdEncrypter, EncryptedValue}

trait MockAesGcmAdCrypto {
  val nonce: String = "some-nonce"
  val valueToEncrypt: String = "value-to-encrypt"
  val encryptedValue: String = "some-value"
  val decryptedValue: String = "decrypted-value"

  val mockEncrypterDecrypter: AdEncrypter with AdDecrypter = new AdEncrypter with AdDecrypter {
    override def encrypt(valueToEncrypt: String, associatedText: String): EncryptedValue = EncryptedValue(
      value = encryptedValue,
      nonce = nonce
    )

    override def decrypt(valueToDecrypt: EncryptedValue, associatedText: String): String = decryptedValue
  }

  implicit val mockAesGcmAdCrypto: AesGcmAdCrypto = new AesGcmAdCrypto {
    override def encrypt(valueToEncrypt: String)
                        (implicit associatedText: String): EncryptedValue = EncryptedValue(
      value = encryptedValue,
      nonce = nonce
    )

    override def decrypt(encryptedValue: EncryptedValue)
                        (implicit associatedText: String): String =
      """
        |{
        | "foo": "bar"
        |}
      """.stripMargin
  }

  implicit val associatedText: String = "some-associated-text"

}
