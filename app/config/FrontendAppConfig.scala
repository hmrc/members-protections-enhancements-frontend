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

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  private def loadConfig(key: String): String = configuration.get[String](key)
  def getServiceBaseUrl(service: String): String = configuration.get[Service](service)

  //Application config
  val host: String    = loadConfig("host")
  val appName: String = loadConfig("appName")

  //Timeout config
  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  //Lockout config
  val lockoutThreshold: Int = configuration.get[Int]("lockout.threshold")

  //MongoDB config
  val sessionDataTtl: Long = configuration.get[Int]("mongodb.sessionDataTtl")
  val failedAttemptTtl: Long = configuration.get[Int]("mongodb.failedAttemptTtl")
  val lockoutTtl: Long = configuration.get[Int]("mongodb.lockoutTtl")

  //URLs
  val loginUrl: String         = loadConfig("urls.login")
  val loginContinueUrl: String = loadConfig("urls.loginContinue")

  private val basGatewayFrontendBaseUrl: String = getServiceBaseUrl("microservice.services.bas-gateway-frontend")
  lazy val signOutUrl: String = basGatewayFrontendBaseUrl + "/bas-gateway/sign-out-without-state"

  // Feedback config
  val exitSurveyUrl: String = loadConfig("urls.signOutWithFeedback")


  private val backendUrl: String = getServiceBaseUrl("microservice.services.mpe-backend")
  val checkAndRetrieveUrl = s"$backendUrl/${loadConfig("urls.checkAndRetrieve")}"


  lazy val psaOverviewUrl: String = loadConfig("urls.psaOverview")
  lazy val pspDashboardUrl: String = loadConfig("urls.pspDashboard")
  lazy val mpsRegistrationUrl: String = loadConfig("urls.mpsRegistration")

  //Beta feedback config
//  private def redirectUrl(implicit request: RequestHeader) = SafeRedirectUrl(host + request.uri).encodedUrl
//  private val contactFormServiceIdentifier: String = appName
//  private val contactFrontendUrl: String = configuration.get[Service]("microservice.services.contact-frontend").baseUrl

//  def betaFeedbackUrl(implicit request: RequestHeader): String =
//    s"$contactFrontendUrl/contact/beta-feedback" +
//      s"?service=$contactFormServiceIdentifier&backUrl=$redirectUrl"

  val checkLtaGuidanceUrl: String = loadConfig("urls.guidance.checkLta")

  //Feature switches
  val betaBannerEnabled: Boolean = configuration.get[Boolean]("feature-switch.betaBannerEnabled")

  val lockoutEnabled: Boolean = configuration.get[Boolean]("feature-switch.lockoutEnabled")

  //Allow list
  val allowListEnabled: Boolean = configuration.get[Boolean]("feature-switch.allowListEnabled")
  val allowedPsrIds: Seq[String] = configuration.get[Seq[String]]("psrIdAllowList")
}
