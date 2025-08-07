package utils.encryption

import config.FrontendAppConfig
import uk.gov.hmrc.crypto.{AdDecrypter, AdEncrypter, SymmetricCryptoFactory}

import javax.inject.{Inject, Singleton}

@Singleton
class AesGcmAdCryptoFactory @Inject()(appConfig: FrontendAppConfig) {
  private lazy val aesGcmAdCrypto = SymmetricCryptoFactory.aesGcmAdCrypto(appConfig.encryptionKey)

  def instance(): AdEncrypter with AdDecrypter = aesGcmAdCrypto
}