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
