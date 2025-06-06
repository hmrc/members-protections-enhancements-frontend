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

package base

import com.github.tomakehurst.wiremock.WireMockServer
import com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig
import controllers.actions._
import models.UserAnswers
import models.response.RecordStatusMapped.{Active, Dormant, Withdrawn}
import models.response.RecordTypeMapped.{FixedProtection2016, IndividualProtection2014, InternationalEnhancementTransfer, PrimaryProtection}
import models.response.{ProtectionRecord, ProtectionRecordDetails}
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, OptionValues, TryValues}
import org.scalatestplus.mockito.MockitoSugar
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import play.api.Application
import play.api.i18n.{Messages, MessagesApi}
import play.api.inject.bind
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.libs.json.{JsResult, JsString, JsSuccess, Reads}
import play.api.libs.ws.WSClient
import play.api.mvc.{BodyParsers, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import viewmodels.formPage.FormPageViewModel

import java.net.URLEncoder
import scala.reflect.ClassTag

trait SpecBase
  extends AnyFreeSpec
    with Matchers
    with TryValues
    with OptionValues
    with ScalaFutures
    with IntegrationPatience
    with MockitoSugar
    with BeforeAndAfterEach
    with WireMockHelper
    with GuiceOneServerPerSuite
    with BeforeAndAfterAll {

  val server: WireMockServer = new WireMockServer(wireMockConfig().dynamicPort())

  val userAnswersId: String = "id"

  def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)

  def messages(app: Application): Messages = app.injector.instanceOf[MessagesApi].preferred(FakeRequest())

  val parsers: BodyParsers.Default = app.injector.instanceOf[BodyParsers.Default]

  val fakePsaIdentifierAction: FakePsaIdentifierAction = new FakePsaIdentifierAction(parsers)

  protected def applicationBuilder(userAnswers: UserAnswers, identifierAction: IdentifierAction = fakePsaIdentifierAction): GuiceApplicationBuilder =
    new GuiceApplicationBuilder()
      .configure(servicesConfig)
      .overrides(
        bind[IdentifierAction].toInstance(identifierAction),
        bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
      )

  def runningApplication(block: Application => Unit): Unit =
    running(_ => applicationBuilder(emptyUserAnswers))(block)

  protected def injected[A: ClassTag](implicit app: Application): A = app.injector.instanceOf[A]

  def urlEncode(input: String): String = URLEncoder.encode(input, "utf-8")

  def getFormPageViewModel(onSubmit: Call, backLinkUrl: String): FormPageViewModel =
    FormPageViewModel(onSubmit = onSubmit, backLinkUrl = Some(backLinkUrl))

  val mockHost: String = WireMockHelper.host
  val mockPort: String = WireMockHelper.wireMockPort.toString

  lazy val client: WSClient = app.injector.instanceOf[WSClient]

  def servicesConfig: Map[String, String] = Map(
    "microservice.services.mpe-backend.host"           -> mockHost,
    "microservice.services.mpe-backend.port"           -> mockPort,
    "microservice.services.bas-gateway-frontend.host"  -> mockHost,
    "microservice.services.bas-gateway-frontend.port"  -> mockPort
  )

  override def beforeAll(): Unit = {
    super.beforeAll()
    startWireMock()
  }

  override def afterAll(): Unit = {
    stopWireMock()
    super.afterAll()
  }

  override def beforeEach(): Unit = {
    resetWireMock()
    super.beforeEach()
  }

  def enumRoundTest[ModelType: Reads](stringValue: String, expectedModel: ModelType): Unit =
    s"when provided with valid string '$stringValue' should read and map to the correct model" in {
      val result: JsResult[ModelType] = JsString(stringValue).validate[ModelType]
      result mustBe a[JsSuccess[_]]
      result.get mustBe expectedModel
    }

  val dummyProtectionRecords: ProtectionRecordDetails = ProtectionRecordDetails(
    Seq(
      ProtectionRecord(
        protectionReference = Some("IP141234567890A"),
        `type` = IndividualProtection2014,
        status = Active,
        protectedAmount = Some(1440321),
        lumpSumAmount = None,
        lumpSumPercentage = None,
        enhancementFactor = None
      ),
      ProtectionRecord(
        protectionReference = Some("FP1612345678901A"),
        `type` = FixedProtection2016,
        status = Dormant,
        protectedAmount = None,
        lumpSumAmount = None,
        lumpSumPercentage = None,
        enhancementFactor = None
      ),
      ProtectionRecord(
        protectionReference = None,
        `type` = PrimaryProtection,
        status = Withdrawn,
        protectedAmount = None,
        lumpSumAmount = Some(34876),
        lumpSumPercentage = Some(21),
        enhancementFactor = None
      ),
      ProtectionRecord(
        protectionReference = Some("IE211234567890A"),
        `type` = InternationalEnhancementTransfer,
        status = Active,
        protectedAmount = Some(1440321),
        lumpSumAmount = None,
        lumpSumPercentage = None,
        enhancementFactor = Some(0.12)
      )
    )
  )
}
