package utils.encryption

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.Mockito.when
import uk.gov.hmrc.crypto.{AdDecrypter, AdEncrypter, EncryptedValue}

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
