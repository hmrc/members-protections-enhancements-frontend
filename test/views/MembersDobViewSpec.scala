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
import controllers.MembersDobController
import forms.MembersDobFormProvider
import models.{MembersDob, NormalMode}
import org.jsoup.Jsoup
import org.jsoup.nodes.Document
import play.api.Application
import play.api.data.Form
import play.api.i18n.Messages
import play.api.mvc.AnyContentAsEmpty
import play.api.test.FakeRequest
import viewmodels.models.FormPageViewModel
import views.html.MembersDobView

class MembersDobViewSpec extends SpecBase {

  "view" - {
    "display correct guidance and text" in new Setup {

      view.getElementsByTag("h1").text() mustBe "What is the Pearl Harvey's date of birth?"
      view.html.contains(messages(app)("membersDob.title"))
      view.text.contains(messages(app)("date.day"))
      view.text.contains(messages(app)("date.month"))
      view.text.contains(messages(app)("date.year"))

    }
  }


  trait Setup {

    val app: Application = applicationBuilder(emptyUserAnswers).build()
    implicit val msg: Messages = messages(app)
    implicit val request: FakeRequest[AnyContentAsEmpty.type] = FakeRequest("GET", "/some/resource/path")
    private val formProvider = new MembersDobFormProvider()
    private val form: Form[MembersDob] = formProvider()
    val viewModel: FormPageViewModel[MembersDob] = MembersDobController.viewModel(NormalMode)

    val view: Document =
      Jsoup.parse(app.injector.instanceOf[MembersDobView].apply(form, viewModel, "Pearl Harvey").body
      )
  }

}
