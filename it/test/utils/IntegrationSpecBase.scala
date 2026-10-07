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

package utils

import controllers.actions.*
import models.*
import models.userAnswers.UserAnswers
import org.scalatest.*
import org.scalatest.concurrent.{IntegrationPatience, ScalaFutures}
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import org.scalatestplus.play.guice.GuiceOneServerPerSuite
import pages.*
import play.api.inject.guice.GuiceApplicationBuilder
import play.api.mvc.{AnyContentAsEmpty, BodyParsers}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import play.api.{inject, Application}
import uk.gov.hmrc.http.{HeaderCarrier, SessionKeys}

import java.time.LocalDate
import java.util.UUID
import scala.concurrent.ExecutionContext
import scala.concurrent.duration.{Duration, FiniteDuration, SECONDS}

trait IntegrationSpecBase
    extends AnyWordSpec
    with TestSuite
    with ScalaFutures
    with IntegrationPatience
    with Matchers
    with WireMockHelper
    with GuiceOneServerPerSuite {

  implicit val ec: ExecutionContext = scala.concurrent.ExecutionContext.global
  implicit val hc: HeaderCarrier = HeaderCarrier()

  private val defaultSeconds = 5
  implicit val defaultDuration: FiniteDuration = Duration.apply(defaultSeconds, SECONDS)
  protected val authoriseUri: String = "/auth/authorise"
  protected val authResponseBody: String = """{
     |  "internalId": "Int-06b0ff6b-e2ec-4bbb-866a-7ebbaa90e109",
     |  "affinityGroup": "Organisation",
     |  "authorisedEnrolments": [
     |    {
     |      "key": "HMRC-PODS-ORG",
     |      "identifiers": [
     |        {
     |          "key": "PSAID",
     |          "value": "NINO"
     |        }
     |      ],
     |      "state": "Activated",
     |      "confidenceLevel": 50
     |    }
     |  ]
     |}""".stripMargin
  protected val authResponseNotAuthorisedBody: String = """{
     |  "internalId": "Int-06b0ff6b-e2ec-4bbb-866a-7ebbaa90e109",
     |  "affinityGroup": "Organisation",
     |  "authorisedEnrolments": [
     |  ]
     |}""".stripMargin

  protected val validMPEPOSTResponse: String =
    """
      |{
      | "protectionRecords": [
      |   {
      |     "protectionReference": "some-id",
      |     "type": "FIXED PROTECTION 2016",
      |     "status": "OPEN",
      |     "protectedAmount": 1,
      |     "lumpSumAmount": 1,
      |     "lumpSumPercentage": 1,
      |     "enhancementFactor": 0.5
      |   }
      | ]
      |}""".stripMargin

  val parsers: BodyParsers.Default = app.injector.instanceOf[BodyParsers.Default]
  protected val fakePsaIdentifierAction: FakePsaIdentifierAction = new FakePsaIdentifierAction(parsers)

  protected def applicationBuilder(userAnswers: UserAnswers): Application = new GuiceApplicationBuilder()
    .configure(
      "microservice.services.auth.port" -> server.port(),
      "microservice.services.mpe-backend.port" -> server.port()
    )
    .overrides(
      inject.bind[DataRetrievalAction].toInstance(new FakeDataRetrievalAction(userAnswers))
    )
    .build()

  protected val userAnswersId: String = "id"
  protected val memberDetails: MemberDetails = MemberDetails("Pearl", "Harvey")
  protected val membersDob: MembersDob = MembersDob(LocalDate.of(2022, 1, 1))
  protected val membersNino: MembersNino = MembersNino("AB123456A")
  protected val membersPsaCheckRef: MembersPsaCheckRef = MembersPsaCheckRef("PSA12345678A")
  protected def emptyUserAnswers: UserAnswers = UserAnswers(userAnswersId)
  protected val fullUserAnswers: UserAnswers = emptyUserAnswers
    .setOrException(page = WhatIsTheMembersNamePage, value = memberDetails)
    .setOrException(page = MembersDobPage, value = membersDob)
    .setOrException(page = MembersNinoPage, value = membersNino)
    .setOrException(page = MembersPsaCheckRefPage, value = membersPsaCheckRef)
    .setOrException(page = CheckYourAnswersPage, CheckMembersDetails(true))

  protected val fullUserAnswersJsonToPost: String = s"""{
               |  "firstName": "Pearl",
               |  "lastName": "Harvey",
               |  "dateOfBirth": "2022-01-01",
               |  "nino": "AB123456A",
               |  "psaCheckRef": "PSA12345678A"
               |}""".stripMargin

  def buildGet(url: String): FakeRequest[AnyContentAsEmpty.type] =
    FakeRequest(GET, url)
      .withSession(SessionKeys.sessionId -> UUID.randomUUID().toString, SessionKeys.authToken -> SessionKeys.authToken)
      .withHeaders("Csrf-Token" -> "nocheck")

  override def beforeAll(): Unit = {
    server.start()
    super.beforeAll()
  }

  override def beforeEach(): Unit = {
    server.resetAll()
    super.beforeEach()
  }

  override def afterAll(): Unit = {
    super.afterAll()
    server.stop()
  }
}
