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

package controllers

import base.SpecBase
import forms.WhatIsTheMembersNameFormProvider
import models.{MemberDetails, NormalMode}
import play.api.data.Form
import play.api.test.FakeRequest
import play.api.test.Helpers._
import viewmodels.models.FormPageViewModel
import views.html.WhatIsTheMembersNameView

class WhatIsTheMembersNameControllerSpec extends SpecBase {

  private lazy val onPageLoad = routes.WhatIsTheMembersNameController.onPageLoad(NormalMode).url
  private lazy val onSubmit = routes.WhatIsTheMembersNameController.onSubmit(NormalMode).url

  private val formProvider = new WhatIsTheMembersNameFormProvider()
  private val form: Form[MemberDetails] = formProvider()

  "Member Name Controller" - {
    "must return OK and the correct view for a GET" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(GET, onPageLoad)

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatIsTheMembersNameView]
        val viewModel: FormPageViewModel[MemberDetails] = WhatIsTheMembersNameController.viewModel(NormalMode)

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(form, viewModel)(request, messages(application)).toString
      }
    }

    "must save the form data and redirect on valid submission" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, onSubmit)
          .withFormUrlEncodedBody(
            "firstName" -> "John",
            "lastName" -> "Doe")

        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result).value mustEqual routes.MembersDobController.onPageLoad().url

      }
    }

    "must return a Bad Request and errors when invalid data is submitted" in {
      val application = applicationBuilder(userAnswers = Some(emptyUserAnswers)).build()

      running(application) {
        val request = FakeRequest(POST, onSubmit)
          .withFormUrlEncodedBody(
            "firstName" -> "",
            "lastName" -> "Doe")

        val result = route(application, request).value

        val view = application.injector.instanceOf[WhatIsTheMembersNameView]
        val viewModel: FormPageViewModel[MemberDetails] = WhatIsTheMembersNameController.viewModel(NormalMode)
        val formWithErrors = form.bind(Map("firstName" -> "", "lastName" -> "Doe"))

        status(result) mustEqual BAD_REQUEST
        contentAsString(result) mustEqual view(formWithErrors, viewModel)(request, messages(application)).toString

      }
    }
  }

}
