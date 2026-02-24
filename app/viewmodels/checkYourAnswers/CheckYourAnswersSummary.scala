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

package viewmodels.checkYourAnswers

import controllers.routes
import models.*
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.{Text, Value}
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{ActionItem, Actions, Key, SummaryListRow}

object CheckYourAnswersSummary {

  def membersFirstNameRow(memberDetails: MemberDetails)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(content = Text(messages("membersName.firstName"))),
      value = Value(content = Text(memberDetails.firstName)),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href = routes.WhatIsTheMembersNameController.onPageLoad(CheckMode).url + "#firstName",
              content = Text(messages("site.change")),
              visuallyHiddenText = Some("first name"),
              attributes = Map("id" -> "change-first-name")
            )
          )
        )
      )
    )

  def membersLastNameRow(memberDetails: MemberDetails)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(content = Text(messages("membersName.lastName"))),
      value = Value(content = Text(memberDetails.lastName)),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href = routes.WhatIsTheMembersNameController.onPageLoad(CheckMode).url + "#lastName",
              content = Text(messages("site.change")),
              visuallyHiddenText = Some("last name"),
              attributes = Map("id" -> "change-last-name")
            )
          )
        )
      )
    )

  def membersDobRow(membersDob: MembersDob)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(content = Text(messages("membersDob.dob"))),
      value = Value(content = Text(membersDob.dob)),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href = routes.MembersDobController.onPageLoad(CheckMode).url + "#dateOfBirth",
              content = Text(messages("site.change")),
              visuallyHiddenText = Some("date of birth"),
              attributes = Map("id" -> "change-dob")
            )
          )
        )
      )
    )

  def membersNinoRow(membersNino: MembersNino)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(content = Text(messages("membersNino.nino"))),
      value = Value(content = Text(membersNino.nino)),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href = routes.MembersNinoController.onPageLoad(CheckMode).url + "#nino",
              content = Text(messages("site.change")),
              visuallyHiddenText = Some("National Insurance number"),
              attributes = Map("id" -> "change-nino")
            )
          )
        )
      )
    )

  def membersPsaCheckRefRow(membersPsaCheckRef: MembersPsaCheckRef)(implicit messages: Messages): SummaryListRow =
    SummaryListRow(
      key = Key(content = Text(messages("membersPsaCheckRef.pensionSchemeAdminCheckRef"))),
      value = Value(content = Text(membersPsaCheckRef.psaCheckRef)),
      actions = Some(
        Actions(
          items = Seq(
            ActionItem(
              href = routes.MembersPsaCheckRefController.onPageLoad(CheckMode).url + "#psaCheckRef",
              content = Text(messages("site.change")),
              visuallyHiddenText = Some("pension scheme administrator check reference"),
              attributes = Map("id" -> "change-pensionSchemeAdminCheckRef")
            )
          )
        )
      )
    )

}
