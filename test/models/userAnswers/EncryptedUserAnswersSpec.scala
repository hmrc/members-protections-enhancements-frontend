package models.userAnswers

import base.SpecBase
import play.api.libs.json._
import uk.gov.hmrc.crypto.EncryptedValue
import utils.encryption.MockAesGcmAdCrypto

import java.time.Instant

class EncryptedUserAnswersSpec extends SpecBase with MockAesGcmAdCrypto {

  "decrypt" - {
    "should decrypt user answers as per the decryption implementation" in{
      val model: EncryptedUserAnswers = EncryptedUserAnswers(
        id = "id",
        encryptedValue = EncryptedValue("encryptedString", "nonce"),
        lastUpdated = Instant.MIN
      )

      model.decrypt mustBe UserAnswers("id", Json.obj("foo" -> "bar"), Instant.MIN)
    }
  }

  val epochMilli: Long = 100L

  protected val json: JsValue = Json.parse(
    s"""
       |{
       | "_id": "anId",
       | "data": {
       |   "value": "encryptedValue",
       |   "nonce": "nonce"
       | },
       | "lastUpdated": {
       |   "$$date": {
       |     "$$numberLong": "$epochMilli"
       |   }
       | }
       |}
        """.stripMargin
  )

  "reads" - {
    "should read valid value from json" in {
      val result: JsResult[EncryptedUserAnswers] = json.validate[EncryptedUserAnswers]
      result mustBe a[JsSuccess[_]]
      result.get mustBe EncryptedUserAnswers(
        id = "anId",
        encryptedValue = EncryptedValue("encryptedValue", "nonce"),
        lastUpdated = Instant.ofEpochMilli(epochMilli)
      )
    }

    "should not read invalid or empty value from json" in {
      val json: JsValue = JsObject.empty
      json.validate[EncryptedUserAnswers] mustBe a[JsError]
    }
  }

  "writes" - {
    "should write correctly to JSON" in {
      val result: JsValue = Json.toJson(EncryptedUserAnswers(
        id = "anId",
        encryptedValue = EncryptedValue("encryptedValue", "nonce"),
        lastUpdated = Instant.ofEpochMilli(epochMilli)
      ))

      result mustBe json
    }
  }
}
