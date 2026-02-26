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
import forms.MembersDobFormProvider
import models.{MemberDetails, MembersDob, MembersResult, NormalMode}
import pages.{MembersDobPage, ResultsPage, WhatIsTheMembersNamePage}
import play.api.data.{Form, FormError}
import play.api.test.FakeRequest
import play.api.test.Helpers.*
import viewmodels.formPage.FormPageViewModel
import views.html.MembersDobView

import java.time.LocalDate

class MembersDobControllerSpec extends SpecBase {

  private lazy val onPageLoad = routes.MembersDobController.onPageLoad(NormalMode).url
  private lazy val onSubmit = routes.MembersDobController.onSubmit(NormalMode)
  private lazy val backLinkUrl = routes.WhatIsTheMembersNameController.onSubmit(NormalMode).url
  private val formProvider = new MembersDobFormProvider(mockDateTimeProvider)
  private val form: Form[MembersDob] = formProvider()

  "Member Dob Controller" - {
    "must return OK and the correct view for a GET" in {

      val userAnswers =
        emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
      val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersDobView]
        val viewModel: FormPageViewModel = getFormPageViewModel(onSubmit, backLinkUrl)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, viewModel, "Pearl Harvey")(request, messages(application)).toString
      }
    }

    "must return OK and pre-fill the form when data is already present" in {
      val userAnswers = emptyUserAnswers
        .set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey"))
        .success
        .value
        .set(MembersDobPage, MembersDob(LocalDate.of(2014, 3, 10)))
        .success
        .value

      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersDobView]
        val viewModel: FormPageViewModel = getFormPageViewModel(onSubmit, backLinkUrl)
        val expectedForm: Form[MembersDob] = form.fill(MembersDob(LocalDate.of(2014, 3, 10)))
        val expectedViewString: String =
          view(expectedForm, viewModel, "Pearl Harvey")(request, messages(application)).toString

        status(result) mustEqual OK
        contentAsString(result) mustEqual expectedViewString
      }
    }

    "must save the form data and redirect on valid submission" in {
      val userAnswers =
        emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(POST, onSubmit.url)
          .withFormUrlEncodedBody("dateOfBirth.day" -> "10", "dateOfBirth.month" -> "10", "dateOfBirth.year" -> "2024")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MembersNinoController.onPageLoad(NormalMode).url

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers =
        emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(POST, onSubmit.url)
          .withFormUrlEncodedBody(
            "dateOfBirth.day" -> "",
            "dateOfBirth.month" -> "",
            "dateOfBirth.year" -> ""
          )

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersDobView]
        val viewModel: FormPageViewModel = getFormPageViewModel(onSubmit, backLinkUrl)

        val formWithErrors: Form[MembersDob] = form
          .bind(Map("dateOfBirth.day" -> "", "dateOfBirth.month" -> "", "dateOfBirth.year" -> ""))
          .copy(errors = Seq(FormError("dateOfBirth", "membersDob.error.missing.all")))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(formWithErrors, viewModel, "Pearl Harvey")(
          request,
          messages(application)
        ).toString

      }
    }

    "must redirect to WhatIsTheMembersNamePage when no members details exists" in {
      val userAnswers = emptyUserAnswers
      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.WhatIsTheMembersNameController.onPageLoad(NormalMode).url
      }
    }

    "must redirect to start page for a GET if user journey is already successful" in {

      val application = applicationBuilder(userAnswers =
        emptyUserAnswers
          .set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey"))
          .success
          .value
          .set(page = ResultsPage, value = MembersResult(true))
          .success
          .value
      ).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.ClearCacheController.onPageLoad().url
      }
    }
  }
}
