/*
 * Copyright 2021 HM Revenue & Customs
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

package controllers

import base.SpecBase
import models.{MemberDetails, MembersDob, MembersNino, MembersPsaCheckRef, UserAnswers}
import org.mockito.Mockito.{times, verify}
import org.scalatestplus.mockito.MockitoSugar
import pages.{MembersDobPage, MembersNinoPage, MembersPsaCheckRefPage, WhatIsTheMembersNamePage}
import play.api.inject
import play.api.test.FakeRequest
import play.api.test.Helpers._
import utils.IdGenerator

class ClearCacheControllerSpec extends SpecBase with MockitoSugar {

  val userAnswers: UserAnswers = emptyUserAnswers
    .set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
    .set(page = MembersDobPage, value = MembersDob(1, 1, 2000)).success.value
    .set(page = MembersNinoPage, value = MembersNino("AB123456A")).success.value
    .set(page = MembersPsaCheckRefPage, value = MembersPsaCheckRef("PSA12345678A")).success.value

  "onPageLoad" - {
    "when the user clicked service name on banner" - {
      "must redirect to start page with new correlation id" in {

        val mockIdGenerator = mock[IdGenerator]
        val application = applicationBuilder(userAnswers = userAnswers)
          .overrides(
            inject.bind(classOf[IdGenerator]).to(mockIdGenerator)
          ).build()

        running(application) {

          val request = FakeRequest(GET, routes.ClearCacheController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.WhatYouWillNeedController.onPageLoad().url
          verify(mockIdGenerator, times(1)).getCorrelationId
        }
      }
      "must redirect to start page with existing request correlation id, no need to generate new" in {

        val mockIdGenerator = mock[IdGenerator]
        val application = applicationBuilder(userAnswers = userAnswers, correlationId = Some("X-123"))
          .overrides(
            inject.bind(classOf[IdGenerator]).to(mockIdGenerator)
          ).build()

        running(application) {

          val request = FakeRequest(GET, routes.ClearCacheController.onPageLoad().url)

          val result = route(application, request).value

          status(result) mustEqual SEE_OTHER
          redirectLocation(result).value mustEqual routes.WhatYouWillNeedController.onPageLoad().url
          verify(mockIdGenerator, times(0)).getCorrelationId
        }
      }
    }
  }
}
