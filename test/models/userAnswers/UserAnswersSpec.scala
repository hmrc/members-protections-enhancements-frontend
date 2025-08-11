package models.userAnswers

import base.SpecBase
import pages.QuestionPage
import play.api.libs.json.{JsObject, JsPath, JsString, JsValue, Json, Writes}
import uk.gov.hmrc.crypto.EncryptedValue
import utils.encryption.MockAesGcmAdCrypto

import java.time.Instant
import scala.util.{Failure, Success, Try}

class UserAnswersSpec extends SpecBase with MockAesGcmAdCrypto{

  "encrypt" - {
    "should encrypt user answers as per the encryption implementation" in {
      val model = UserAnswers(
        id = "id",
        data = Json.parse(
          """{
            | "foo": "bar"
            |}""".stripMargin
        ).as[JsObject],
        lastUpdated = Instant.MIN
      )

      model.encrypt mustBe EncryptedUserAnswers("id", EncryptedValue("some-value", "some-nonce"), Instant.MIN)
    }
  }

  val model: UserAnswers = UserAnswers(
    id = "id",
    data = JsObject.empty,
    lastUpdated = Instant.MIN
  )

  class DummyPage extends QuestionPage[String] {
    override def path: JsPath = JsPath \ "field"
  }

  val dummyPage: DummyPage = new DummyPage

  "get" - {
    "should return None when a value doesn't exist" in {
      model.get(dummyPage) mustBe None
    }

    "should return a value when it exists" in {
      model.copy(data = Json.obj("field" -> JsString("value"))).get(dummyPage) mustBe Some("value")
    }
  }

  "set" - {
    "should update data when setting succeeds" in {
      val result: Try[UserAnswers] = model.set(dummyPage, "some-value")
      result mustBe a[Success[_]]
      result.get.data mustBe Json.obj("field" -> "some-value")
    }

    "should return a failure when setting fails" in {
      class BadPage extends QuestionPage[String] {
        override def path: JsPath = JsPath
      }

      val badPage: BadPage = new BadPage

      val result: Try[UserAnswers] = model.set(badPage, "some-value")
      result mustBe a[Failure[_]]
    }
  }
}
