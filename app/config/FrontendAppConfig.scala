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

  val host: String    = loadConfig("host")
  val appName: String = loadConfig("appName")

  val loginUrl: String         = loadConfig("urls.login")
  val loginContinueUrl: String = loadConfig("urls.loginContinue")
  //val redirectUrl = s"$loginUrl?continue=http%3A%2F%2Flocalhost%3A6741$loginContinueUrl"
  val signOutUrl: String = loadConfig("urls.signOut")

  private val exitSurveyBaseUrl: String = configuration.get[Service]("microservice.services.feedback-frontend").baseUrl
  val exitSurveyUrl: String = s"$exitSurveyBaseUrl/feedback/members-protections-and-enhancements"

  private lazy val managePensionsFrontendUrl: String = configuration.get[Service]("microservice.services.manage-pensions-frontend")

  lazy val psaOverviewUrl: String = s"$managePensionsFrontendUrl${loadConfig("urls.psaOverview")}"
  lazy val pspDashboardUrl: String = s"$managePensionsFrontendUrl${loadConfig("urls.pspDashboard")}"

  val timeout: Int   = configuration.get[Int]("timeout-dialog.timeout")
  val countdown: Int = configuration.get[Int]("timeout-dialog.countdown")

  val cacheTtl: Long = configuration.get[Int]("mongodb.timeToLiveInSeconds")

}
