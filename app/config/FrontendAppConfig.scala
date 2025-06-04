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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import play.api.mvc.RequestHeader
import uk.gov.hmrc.play.bootstrap.binders.SafeRedirectUrl

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  private def loadConfig(key: String): String = configuration.get[String](key)

  //Application config
  val host: String    = loadConfig("host")
  val appName: String = loadConfig("appName")

  //Timeout config
  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  //MongoDB config
  val cacheTtl: Long = configuration.get[Int]("mongodb.timeToLiveInSeconds")

  //URLs
  val loginUrl: String         = loadConfig("urls.login")
  val loginContinueUrl: String = loadConfig("urls.loginContinue")
  //val redirectUrl = s"$loginUrl?continue=http%3A%2F%2Flocalhost%3A6741$loginContinueUrl"

  private val basGatewayFrontendBaseUrl: String = configuration.get[Service]("microservice.services.bas-gateway-frontend").baseUrl
  lazy val signOutUrl: String = basGatewayFrontendBaseUrl + "/bas-gateway/sign-out-without-state"

  private val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val backendUrl: String = configuration.get[Service]("microservice.services.mpe-backend").baseUrl
  val checkAndRetrieveUrl = s"$backendUrl/${loadConfig("urls.checkAndRetrieve")}"
  val exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/members-protections-and-enhancements"

  lazy val psaOverviewUrl: String = loadConfig("urls.psaOverview")
  lazy val pspDashboardUrl: String = loadConfig("urls.pspDashboard")

  //Beta feedback config
  private def redirectUrl(implicit request: RequestHeader) = SafeRedirectUrl(host + request.uri).encodedUrl
  private val contactFormServiceIdentifier: String = appName
  private val contactFrontendUrl: String = configuration.get[Service]("microservice.services.contact-frontend").baseUrl

  def betaFeedbackUrl(implicit request: RequestHeader): String =
    s"$contactFrontendUrl/contact/beta-feedback" +
      s"?service=$contactFormServiceIdentifier&backUrl=$redirectUrl"

  val checkLtaGuidanceUrl: String = loadConfig("urls.guidance.checkLta")

  //Feature switches
  val betaBannerEnabled: Boolean = configuration.get[Boolean]("feature-switch.betaBannerEnabled")
}
