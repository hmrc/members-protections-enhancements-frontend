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

package viewmodels

import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{BreadcrumbsItem, Text}

object MpeBreadcrumbs {
  private def managingPensionSchemesBreadcrumb(implicit messages: Messages) = BreadcrumbsItem(
    content = Text(messages("results.breadcrumbs.mps")),
    href = Some(controllers.routes.MpsDashboardController.redirectToMps().url)
  )

  private def checkMpeBreadcrumb(implicit messages: Messages) = BreadcrumbsItem(
    content = Text(messages("results.breadcrumbs.mpe")),
    href = Some(controllers.routes.ClearCacheController.onPageLoad().url)
  )

  def mpePageBreadcrumbs(implicit messages: Messages): Seq[BreadcrumbsItem] = Seq(
    managingPensionSchemesBreadcrumb,
    checkMpeBreadcrumb
  )
}
