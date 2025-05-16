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
import play.api.libs.ws.WSClient
import play.api.mvc.{BodyParsers, Call}
import play.api.test.FakeRequest
import play.api.test.Helpers.running
import viewmodels.models.FormPageViewModel

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

  val parsers = app.injector.instanceOf[BodyParsers.Default]

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
    "microservice.services.mpe-backend.port"           -> mockPort
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
}
