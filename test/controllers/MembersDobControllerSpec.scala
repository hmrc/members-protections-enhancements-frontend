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
import forms.MembersDobFormProvider
import models.{MemberDetails, MembersDob, NormalMode}
import pages.WhatIsTheMembersNamePage
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.models.FormPageViewModel
import views.html.MembersDobView

class MembersDobControllerSpec extends SpecBase {

  private lazy val onPageLoad = routes.MembersDobController.onPageLoad(NormalMode).url
  private lazy val onSubmit = routes.MembersDobController.onSubmit(NormalMode).url

  private val formProvider = new MembersDobFormProvider()
  private val form: Form[MembersDob] = formProvider()

  "Member Dob Controller" - {
    "must return OK and the correct view for a GET" in {

      val userAnswers = emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
      val application = applicationBuilder(userAnswers = userAnswers).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersDobView]
        val viewModel: FormPageViewModel[MembersDob] = MembersDobController.viewModel(NormalMode)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, viewModel, "Pearl Harvey")(request, messages(application)).toString
      }
    }

    "must save the form data and redirect on valid submission" in {
      val userAnswers = emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(POST, onSubmit)
          .withFormUrlEncodedBody(
            "date.day" -> "10",
            "date.month" -> "10",
            "date.year" -> "2024")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MembersNinoController.onPageLoad().url

      }
    }


    "must return a Bad Request and errors when invalid data is submitted" in {
      val userAnswers = emptyUserAnswers.set(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey")).success.value
      val application = applicationBuilder(userAnswers).build()

      running(application) {
        val request = FakeRequest(POST, onSubmit)
          .withFormUrlEncodedBody(
            "date.day" -> "",
            "date.month" -> "",
            "date.year" -> "")

        val result = route(application, request).value

        val view = application.injector.instanceOf[MembersDobView]
        val viewModel: FormPageViewModel[MembersDob] = MembersDobController.viewModel(NormalMode)
        val formWithErrors = form.bind(Map("date.day" -> "", "date.month" -> "", "date.year" -> ""))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(formWithErrors, viewModel, "Pearl Harvey")(request, messages(application)).toString

      }
    }
  }

}
