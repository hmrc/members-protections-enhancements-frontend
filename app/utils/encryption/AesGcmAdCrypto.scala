package utils.encryption

import config.FrontendAppConfig
import uk.gov.hmrc.crypto.EncryptedValue

import javax.inject.{Inject, Singleton}

@Singleton
class AesGcmAdCrypto @Inject()(appConfig: FrontendAppConfig,
                               aesGcmAdCryptoFactory: AesGcmAdCryptoFactory) {
  private lazy val aesGcmAdCrypto = aesGcmAdCryptoFactory.instance()

  def encrypt(valueToEncrypt: String)
             (implicit associatedText: String): EncryptedValue =
    if (appConfig.useEncryption) {
      aesGcmAdCrypto.encrypt(valueToEncrypt, associatedText)
    } else {
      EncryptedValue(valueToEncrypt, s"$valueToEncrypt-Nonce")
    }

  def decrypt(encryptedValue: EncryptedValue)
             (implicit associatedText: String): String =
    if (appConfig.useEncryption) {
      aesGcmAdCrypto.decrypt(encryptedValue, associatedText)
    } else {
      encryptedValue.value
    }
}
