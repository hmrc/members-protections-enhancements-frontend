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

package config

import com.google.inject.{Inject, Singleton}
import play.api.Configuration
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (configuration: Configuration) {

  private def loadConfig(key: String): String = configuration.get[String](key)
  private val servicesConfig = ServicesConfig(configuration)
  
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
  val useEncryption: Boolean = configuration.get[Boolean]("mongodb.encryption.enabled")
  val encryptionKey: String = configuration.get[String]("mongodb.encryption.key")

  //URLs
  val loginUrl: String         = loadConfig("urls.login")
  val loginContinueUrl: String = loadConfig("urls.loginContinue")

  private val basGatewayFrontendBaseUrl: String = servicesConfig.baseUrl("bas-gateway-frontend")
  lazy val signOutUrl: String = basGatewayFrontendBaseUrl + "/bas-gateway/sign-out-without-state"

  // Feedback config
  val exitSurveyUrl: String = loadConfig("urls.signOutWithFeedback")


  private val backendUrl: String = servicesConfig.baseUrl("mpe-backend")
  val checkAndRetrieveUrl = s"$backendUrl/${loadConfig("urls.checkAndRetrieve")}"

  lazy val mpsDashboardUrl: String = loadConfig("urls.mpsDashboard")
  lazy val mpsRegistrationUrl: String = loadConfig("urls.mpsRegistration")

  val checkLtaGuidanceUrl: String = loadConfig("urls.guidance.checkLta")

  //Feature switches
  val betaBannerEnabled: Boolean = configuration.get[Boolean]("feature-switch.betaBannerEnabled")

  // User allow list
  val userAllowListServiceUrl: String = servicesConfig.baseUrl("user-allow-list")
  val internalAuthToken: String = configuration.get[String]("internal-auth.token")

  //Beta feedback config
  val contactFrontendUrl: String = s"${loadConfig("urls.betaFeedbackUrl")}/?service=members-protections-and-enhancements"

}
