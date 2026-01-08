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
import config.FrontendAppConfig
import org.mockito.Mockito.when
import uk.gov.hmrc.crypto.EncryptedValue

class AesGcmAdCryptoSpec extends SpecBase with MockAesGcmAdCrypto {
  private val mockAesGcmAdCryptoFactory: AesGcmAdCryptoFactory = mock[AesGcmAdCryptoFactory]
  private val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  private def underTest: AesGcmAdCrypto = new AesGcmAdCryptoImpl(mockAppConfig, mockAesGcmAdCryptoFactory)

  ".encrypt" - {
    "useEncryption is true" - {
      "return encrypted value" in {
        when(mockAppConfig.useEncryption).thenReturn(true)
        when(mockAesGcmAdCryptoFactory.instance()).thenReturn(mockEncrypterDecrypter)
        underTest.encrypt(valueToEncrypt) mustBe EncryptedValue("some-value", nonce)
      }
    }

    "useEncryption is false" - {
      "return raw value" in {
        when(mockAppConfig.useEncryption).thenReturn(false)
        underTest.encrypt(valueToEncrypt) mustBe EncryptedValue(valueToEncrypt, valueToEncrypt + "-Nonce")
      }
    }
  }

  ".decrypt" - {
    "useEncryption is true" - {
      "return encrypted value" in {
        when(mockAppConfig.useEncryption).thenReturn(true)
        when(mockAesGcmAdCryptoFactory.instance()).thenReturn(mockEncrypterDecrypter)
        underTest.decrypt(EncryptedValue("value-to-decrypt", nonce)) mustBe decryptedValue
      }
    }

    "useEncryption is false" - {
      "return raw value" in {
        when(mockAppConfig.useEncryption).thenReturn(false)
        underTest.decrypt(EncryptedValue(valueToEncrypt, nonce)) mustBe valueToEncrypt
      }
    }
  }
}
