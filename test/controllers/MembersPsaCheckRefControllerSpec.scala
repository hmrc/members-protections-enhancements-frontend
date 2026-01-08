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
import forms.MembersPsaCheckRefFormProvider
import models.{MemberDetails, MembersDob, MembersNino, MembersPsaCheckRef, MembersResult, NormalMode}
import pages.{MembersDobPage, MembersNinoPage, MembersPsaCheckRefPage, ResultsPage, WhatIsTheMembersNamePage}
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.formPage.FormPageViewModel
import views.html.MembersPsaCheckRefView

import java.time.LocalDate

class MembersPsaCheckRefControllerSpec extends SpecBase {

  private lazy val onPageLoad = routes.MembersPsaCheckRefController.onPageLoad(NormalMode).url
  private lazy val onSubmit = routes.MembersPsaCheckRefController.onSubmit(NormalMode)
  private lazy val backLinkUrl = routes.MembersNinoController.onSubmit(NormalMode).url

  private val formProvider = new MembersPsaCheckRefFormProvider()
  private val form: Form[MembersPsaCheckRef] = formProvider()


  "Members Psa Check Reference Controller" - {
    "must return OK and the correct view for a GET" in {
      val userAnswers = emptyUserAnswers
        .set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
        .set(MembersDobPage, MembersDob(LocalDate.of(2010, 1, 1))).success.value
        .set(MembersNinoPage, MembersNino("AA123456C")).success.value

      val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersPsaCheckRefView]
        val viewModel: FormPageViewModel = getFormPageViewModel(onSubmit, backLinkUrl)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, viewModel, "Pearl Harvey")(request, messages(application)).toString
      }
    }

    "must save the form data and redirect on valid submission" in {
      val userAnswers = emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(POST, onSubmit.url)
          .withFormUrlEncodedBody(
            "psaCheckRef" -> "PSA12345678A")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url

      }
    }

    "must return OK and pre-fill the form when data is already present" in {
      val userAnswers = emptyUserAnswers
        .set(WhatIsTheMembersNamePage, MemberDetails("Pearl", "Harvey")).success.value
        .set(MembersDobPage, MembersDob(LocalDate.of(2010, 1, 1))).success.value
        .set(MembersNinoPage, MembersNino("AA123456C")).success.value
        .set(MembersPsaCheckRefPage, MembersPsaCheckRef("PSA12345678A")).success.value

      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersPsaCheckRefView]
        val viewModel: FormPageViewModel = getFormPageViewModel(onSubmit, backLinkUrl)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(
          MembersPsaCheckRef("PSA12345678A")), viewModel, "Pearl Harvey")(request, messages(application)).toString
      }
    }

    "must redirect to MembersNino page when user haven't submitted NINO details" in {
      val userAnswers = emptyUserAnswers
        .set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
        .set(MembersDobPage, MembersDob(LocalDate.of(2010, 1, 1))).success.value

      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MembersNinoController.onPageLoad(NormalMode).url

      }
    }

    "must redirect to MembersDob page when user haven't submitted DOB details" in {
      val userAnswers = emptyUserAnswers
        .set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value

      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MembersDobController.onPageLoad(NormalMode).url

      }
    }

    "must save the form data and redirect on valid submission when PsaCheckRef is added with spaces" in {
      val userAnswers = emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(POST, onSubmit.url)
          .withFormUrlEncodedBody(
            "psaCheckRef" -> "PS A1 234 567 8 A")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.CheckYourAnswersController.onPageLoad().url

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(POST, onSubmit.url)
          .withFormUrlEncodedBody(
            "psaCheckRef" -> "")

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersPsaCheckRefView]
        val formWithErrors = form.bind(Map("psaCheckRef" -> ""))
        val viewModel: FormPageViewModel = getFormPageViewModel(onSubmit, backLinkUrl)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(formWithErrors, viewModel, "Pearl Harvey")(request, messages(application)).toString

      }
    }

    "must redirect to start page for a GET if user journey is already successful" in {

      val userAnswers = emptyUserAnswers
        .set(WhatIsTheMembersNamePage, MemberDetails("Pearl", "Harvey")).success.value
        .set(page = ResultsPage, value = MembersResult(true)).success.value

      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ClearCacheController.onPageLoad().url
      }
    }
  }

}
