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

package controllers

import base.SpecBase
import forms.MembersNinoFormProvider
import models.{MemberDetails, MembersNino, MembersResult, NormalMode}
import pages.{MembersNinoPage, ResultsPage, WhatIsTheMembersNamePage}
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.formPage.FormPageViewModel
import views.html.MembersNinoView

class MembersNinoControllerSpec extends SpecBase {

  private lazy val onPageLoad = routes.MembersNinoController.onPageLoad(NormalMode).url
  private lazy val onSubmit = routes.MembersNinoController.onSubmit(NormalMode)
  private lazy val backLinkUrl = routes.MembersDobController.onSubmit(NormalMode).url

  private val formProvider = new MembersNinoFormProvider()
  private val form: Form[MembersNino] = formProvider()

  "Members Nino Controller" - {
    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
      val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersNinoView]
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
            "nino" -> "AA123456C"
          )

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MembersPsaCheckRefController.onPageLoad(NormalMode).url

      }
    }

    "must return OK and pre-fill the form when data is already present" in {
      val userAnswers = emptyUserAnswers
        .set(WhatIsTheMembersNamePage, MemberDetails("Pearl", "Harvey")).success.value
        .set(MembersNinoPage, MembersNino("AA123456C")).success.value

      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersNinoView]
        val viewModel: FormPageViewModel = getFormPageViewModel(onSubmit, backLinkUrl)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form.fill(
          MembersNino("AA123456C")), viewModel, "Pearl Harvey")(request, messages(application)).toString
      }
    }

    "must save the form data and redirect on valid submission when Nino is added with spaces" in {
      val userAnswers = emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(POST, onSubmit.url)
          .withFormUrlEncodedBody(
            "nino" -> "AA 12 34 56 C")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MembersPsaCheckRefController.onPageLoad(NormalMode).url

      }
    }

    "must redirect to start page for a GET if user journey is already successful" in {

      val userAnswers = emptyUserAnswers
        .set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
        .set(page = ResultsPage, value = MembersResult(true)).success.value

      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ClearCacheController.onPageLoad().url
      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(POST, onSubmit.url)
          .withFormUrlEncodedBody(
            "nino" -> "")

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersNinoView]
        val formWithErrors = form.bind(Map("nino" -> ""))
        val viewModel: FormPageViewModel = getFormPageViewModel(onSubmit, backLinkUrl)

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(formWithErrors, viewModel, "Pearl Harvey")(request, messages(application)).toString

      }
    }
  }

}
