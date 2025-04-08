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

package viewmodels.checkYourAnswers

import models.{MemberDetails, MembersDob, MembersNino, MembersPsaCheckRef}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Text
import uk.gov.hmrc.govukfrontend.views.viewmodels.table.TableRow

object ResultsSummary {

  def membersNameRow(memberDetails: MemberDetails)(implicit messages: Messages): Seq[TableRow] = {
    List(
    TableRow(
      content = Text(messages("membersName.name")),
      classes = "govuk-table__header"
    ),
    TableRow(
      content = Text(memberDetails.fullName),
      classes = "govuk-table__cell"
    )
    )
  }

  def membersLastNameRow(memberDetails: MemberDetails)(implicit messages: Messages): Seq[TableRow] = {
    List(
      TableRow(
        content = Text(messages("membersName.lastName")),
        classes = "govuk-table__header"
      ),
      TableRow(
        content = Text(memberDetails.lastName),
        classes = "govuk-table__cell"
      )
    )
  }
  

  def membersDobRow(membersDob: MembersDob)(implicit messages: Messages): Seq[TableRow] = {
    List(
      TableRow(
        content = Text(messages("membersDob.dob")),
        classes = "govuk-table__header"
      ),
      TableRow(
        content = Text(membersDob.dob),
        classes = "govuk-table__cell"
      )
    )
  }

  def membersNinoRow(membersNino: MembersNino)(implicit messages: Messages): Seq[TableRow] = {
    List(
      TableRow(
        content = Text(messages("membersNino.nino")),
        classes = "govuk-table__header"
      ),
      TableRow(
        content = Text(membersNino.nino),
        classes = "govuk-table__cell"
      )
    )
  }

  def membersPsaCheckRefRow(membersPsaCheckRef: MembersPsaCheckRef)(implicit messages: Messages): Seq[TableRow] = {
    List(
      TableRow(
        content = Text(messages("membersPsaCheckRef.pensionSchemeAdminCheckRef")),
        classes = "govuk-table__header"
      ),
      TableRow(
        content = Text(membersPsaCheckRef.psaCheckRef),
        classes = "govuk-table__cell"
      )
    )
  }

}
