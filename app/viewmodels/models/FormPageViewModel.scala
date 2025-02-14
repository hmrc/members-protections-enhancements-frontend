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

package viewmodels.models

import models.{Mode, NormalMode}
import play.api.mvc.Call
import viewmodels.DisplayMessage
import viewmodels.DisplayMessage.{InlineMessage, Message}

case class FormPageViewModel[+A](
                                  title: Message,
                                  heading: InlineMessage,
                                  description: Option[DisplayMessage],
                                  page: A,
                                  refresh: Option[Int],
                                  buttonText: Message,
                                  details: Option[FurtherDetailsViewModel] = None,
                                  onSubmit: Call,
                                  mode: Mode = NormalMode,
                                  optViewOnlyDetails: Option[ViewOnlyDetailsViewModel] = None,
                                  optNotificationBanner: Option[(String, String, String)] = None,
                                  showBackLink: Boolean = true,
                                  breadcrumbs: Option[List[(String, String)]] = None
                                ) {
  def withButtonText(message: Message): FormPageViewModel[A] =
    copy(buttonText = message)
}

object FormPageViewModel {
  def apply[A](
                title: Message,
                heading: InlineMessage,
                page: A,
                onSubmit: Call
              ): FormPageViewModel[A] = FormPageViewModel(
    title,
    heading,
    None,
    page,
    None,
    Message("site.continue"),
    None,
    onSubmit
  )

  def apply[A](
                title: Message,
                heading: InlineMessage,
                page: A,
                details: Option[FurtherDetailsViewModel],
                onSubmit: Call
              ): FormPageViewModel[A] =
    FormPageViewModel(
      title,
      heading,
      None,
      page,
      refresh = None,
      Message("site.continue"),
      details,
      onSubmit
    )

}
