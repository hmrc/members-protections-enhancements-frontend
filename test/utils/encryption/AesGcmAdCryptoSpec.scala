package utils.encryption

import base.SpecBase
import config.FrontendAppConfig
import org.mockito.Mockito.when
import uk.gov.hmrc.crypto.{AdDecrypter, AdEncrypter, EncryptedValue}

class AesGcmAdCryptoSpec extends SpecBase {
  implicit private val associatedText: String = "some-associated-text"

  private val mockAesGcmAdCryptoFactory: AesGcmAdCryptoFactory = mock[AesGcmAdCryptoFactory]
  private val nonce = "some-nonce"
  private val valueToEncrypt = "value-to-encrypt"
  private val decryptedValue = "decrypted-value"

  private val mockAesGcmAdCrypto = new AdEncrypter with AdDecrypter {
    override def encrypt(valueToEncrypt: String, associatedText: String): EncryptedValue = EncryptedValue(
      value = "some-value",
      nonce = nonce
    )

    override def decrypt(valueToDecrypt: EncryptedValue, associatedText: String): String = decryptedValue
  }

  val mockAppConfig: FrontendAppConfig = mock[FrontendAppConfig]

  ".encrypt" - {
    "useEncryption is true" - {
      def underTest: AesGcmAdCrypto = new AesGcmAdCrypto(mockAppConfig, mockAesGcmAdCryptoFactory)
      "return encrypted value" in {
        when(mockAppConfig.useEncryption).thenReturn(true)
        when(mockAesGcmAdCryptoFactory.instance()).thenReturn(mockAesGcmAdCrypto)
        underTest.encrypt(valueToEncrypt) mustBe EncryptedValue("some-value", nonce)
      }
    }

    "useEncryption is false" - {
      def underTest: AesGcmAdCrypto = new AesGcmAdCrypto(mockAppConfig, mockAesGcmAdCryptoFactory)
      "return encrypted value" in {
        when(mockAppConfig.useEncryption).thenReturn(false)
        underTest.encrypt(valueToEncrypt) mustBe EncryptedValue(valueToEncrypt, valueToEncrypt + "-Nonce")
      }
    }
  }

  ".decrypt" - {
    "useEncryption is true" - {
      def underTest: AesGcmAdCrypto = new AesGcmAdCrypto(mockAppConfig, mockAesGcmAdCryptoFactory)
      "return encrypted value" in {
        when(mockAppConfig.useEncryption).thenReturn(true)
        when(mockAesGcmAdCryptoFactory.instance()).thenReturn(mockAesGcmAdCrypto)
        underTest.decrypt(EncryptedValue("value-to-decrypt", nonce)) mustBe decryptedValue
      }
    }

    "useEncryption is false" - {
      val underTest: AesGcmAdCrypto = new AesGcmAdCrypto(mockAppConfig, mockAesGcmAdCryptoFactory)
      "def encrypted value" in {
        when(mockAppConfig.useEncryption).thenReturn(false)
        underTest.decrypt(EncryptedValue(valueToEncrypt, nonce)) mustBe valueToEncrypt
      }
    }
  }
}
