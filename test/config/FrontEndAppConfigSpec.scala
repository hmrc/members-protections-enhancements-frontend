/*
 * Copyright 2024 HM Revenue & Customs
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

import base.SpecBase

class FrontEndAppConfigSpec extends SpecBase {

  val appConfig: FrontendAppConfig = injected[FrontendAppConfig]

  "FrontEndAppConfig" - {
    "have appName" in {
      appConfig.appName must be("members-protections-enhancements-frontend")
    }
    "have host" in {
      appConfig.host must be("http://localhost:30029")
    }

    "have mps registration url" in {
      appConfig.mpsRegistrationUrl must be("http://localhost:8204/manage-pension-schemes/you-need-to-register")
    }

    "have checkAndRetrieveUrl and convert to string" in {
      Service.convertToString(Service("", "", "")) must be("")
      appConfig.checkAndRetrieveUrl must be("http://localhost:30030/members-protections-and-enhancements/check-and-retrieve")
    }
  }
}