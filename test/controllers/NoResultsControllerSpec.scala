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

package controllers

import base.SpecBase
import models._
import models.userAnswers.UserAnswers
import pages._
import play.api.Application
import play.api.mvc.Results.Redirect
import play.api.test.FakeRequest
import play.api.test.Helpers._
import uk.gov.hmrc.http.HeaderCarrier
import views.html.NoResultsView

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId, ZonedDateTime}
import java.util.Locale

class NoResultsControllerSpec extends SpecBase {

  trait Test {
    implicit val hc: HeaderCarrier = HeaderCarrier()
    val userAnswers: UserAnswers = emptyUserAnswers
      .set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
      .set(page = MembersDobPage, value = MembersDob(LocalDate.of(2022, 1, 1))).success.value
      .set(page = MembersNinoPage, value = MembersNino("AB123456A")).success.value
      .set(page = MembersPsaCheckRefPage, value = MembersPsaCheckRef("PSA12345678A")).success.value

    val memberDetails: MemberDetails = MemberDetails("Pearl", "Harvey")
    val membersDob: MembersDob = MembersDob(LocalDate.of(2022, 1, 1))
    val membersNino: MembersNino = MembersNino("AB123456A")
    val membersPsaCheckRef: MembersPsaCheckRef = MembersPsaCheckRef("PSA12345678A")

    val dateTimeWithZone: ZonedDateTime = ZonedDateTime.now(ZoneId.of("Europe/London"))
    val formatter: DateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' h:mma")
    val localDateTime: String = dateTimeWithZone.format(formatter.withLocale(Locale.UK))
  }

  "No Results Controller" - {
    "must redirect to Lockout page if the user is locked out" in new Test {

      val application: Application = applicationBuilder(
        userAnswers = userAnswers,
        checkLockoutResult = Some(Redirect(controllers.routes.LockedOutController.onPageLoad()))
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.NoResultsController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(controllers.routes.LockedOutController.onPageLoad().url)
      }
    }

    "must return OK and the correct view for a GET" in new Test {
      private val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, routes.NoResultsController.onPageLoad().url)
        val result = route(application, request).value
        val view = application.injector.instanceOf[NoResultsView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(
          memberDetails,
          membersDob,
          membersNino,
          membersPsaCheckRef,
          localDateTime,
        )(request, messages(application)).toString
      }
    }

    "must redirect to start page for a GET if no existing data is found" in {
      val application = applicationBuilder(userAnswers = emptyUserAnswers).build()

      running(application) {
        val request = FakeRequest(GET, routes.NoResultsController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.WhatIsTheMembersNameController.onPageLoad(NormalMode).url
      }
    }


    "must redirect to start page for a GET if user journey is already successful" in new Test {
      val application: Application = applicationBuilder(
        userAnswers.set(
          page = ResultsPage,
          value = MembersResult(true)
        ).success.value
      ).build()

      running(application) {
        val request = FakeRequest(GET, routes.NoResultsController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ClearCacheController.onPageLoad().url
      }
    }
  }

}
