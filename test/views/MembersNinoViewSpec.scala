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

package views

import base.SpecBase
import controllers.routes
import forms.MembersNinoFormProvider
import models.{MembersNino, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import viewmodels.formPage.FormPageViewModel
import views.html.MembersNinoView

class MembersNinoViewSpec extends SpecBase {

  "view" - {
    "display correct guidance and text" in new Setup {

      view.getElementsByTag("h1").text() mustBe messages(app)("What is Pearl Harvey's National Insurance number?")

      view.html.contains(messages(app)("membersNino.title"))
      view.text.contains(messages(app)("membersNino.hint"))
    }
  }


  trait Setup {

    val app: Application = applicationBuilder(emptyUserAnswers).build()
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")

    private val formProvider = new MembersNinoFormProvider()
    private val form: Form[MembersNino] = formProvider()
    private val onSubmit = routes.MembersNinoController.onSubmit(NormalMode)
    private val backLinkUrl = routes.MembersDobController.onSubmit(NormalMode).url
    val viewModel: FormPageViewModel = getFormPageViewModel(onSubmit, backLinkUrl)

    val view: Document =
      Jsoup.parse(app.injector.instanceOf[MembersNinoView].apply(form, viewModel, "Pearl Harvey").body
      )
  }

}
