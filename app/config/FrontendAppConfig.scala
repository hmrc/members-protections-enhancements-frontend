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
import uk.gov.hmrc.play.bootstrap.config.ServicesConfig

@Singleton
class FrontendAppConfig @Inject() (servicesConfig: ServicesConfig) {

  private def loadConfig(key: String): String = servicesConfig.getString(key)

  // Application config
  val host: String = loadConfig("host")
  val appName: String = loadConfig("appName")

  // Timeout config
  val timeout: Int = servicesConfig.getInt("timeout-dialog.timeout")
  val countdown: Int = servicesConfig.getInt("timeout-dialog.countdown")

  // Lockout config
  val lockoutThreshold: Int = servicesConfig.getInt("lockout.threshold")

  // MongoDB config
  val sessionDataTtl: Long = servicesConfig.getInt("mongodb.sessionDataTtl")
  val failedAttemptTtl: Long = servicesConfig.getInt("mongodb.failedAttemptTtl")
  val lockoutTtl: Long = servicesConfig.getInt("mongodb.lockoutTtl")
  val useEncryption: Boolean = servicesConfig.getBoolean("mongodb.encryption.enabled")
  val encryptionKey: String = loadConfig("mongodb.encryption.key")

  // URLs
  val loginUrl: String = loadConfig("urls.login")
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

}
