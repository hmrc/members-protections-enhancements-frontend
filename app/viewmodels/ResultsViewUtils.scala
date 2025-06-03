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

import models.response.{ProtectionRecord, ProtectionRecordDetails}
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import utils.CurrencyFormats

object ResultsViewUtils {

  protected[viewmodels] def optValueToSummaryListRow(valueOpt: Option[String], keyString: String)
                                                    (implicit messages: Messages): Seq[SummaryListRow] =
    valueOpt.fold(Seq.empty[SummaryListRow])(value =>
      Seq(SummaryListRow(
        key = Key(HtmlContent(messages(keyString))),
        value = Value(HtmlContent(value))
      ))
    )

  def protectionRecordToSummaryList(protectionRecord: ProtectionRecord)
                                   (implicit messages: Messages): SummaryList = {
    import protectionRecord._

    val summaryListRows: Seq[SummaryListRow] = Seq(SummaryListRow(
      key = Key(HtmlContent(messages("results.statusKey"))),
      value = Value(HtmlContent(
        s"""
           |<strong class="govuk-tag govuk-tag--${status.colourString}">
           |${messages(status.toNameMessagesString)}
           |</strong>
           |${messages(status.toDescriptionMessagesString(`type`))}
           |""".stripMargin
      ))
    )) ++
      optValueToSummaryListRow(protectionReference, "results.protectionRefNumKey") ++
      optValueToSummaryListRow(CurrencyFormats.formatOptInt(protectedAmount), "results.protectedAmtKey") ++
      optValueToSummaryListRow(CurrencyFormats.formatOptInt(lumpSumAmount), "results.lumpSumKey") ++
      optValueToSummaryListRow(lumpSumPercentHtmlStringOpt, "results.lumpSumPercentKey") ++
      optValueToSummaryListRow(enhancementFactorHtmlStringOpt, "results.enhancementFactorKey")

    SummaryList(
      rows = summaryListRows,
      card = Some(Card(
        title = Some(CardTitle(
          content = HtmlContent(messages(`type`.toMessagesString)),
          headingLevel = Some(2)
        )),
        attributes = Map("id" -> `type`.getClass.getSimpleName.dropRight(1))
      ))
    )
  }

  def protectionRecordDetailsToSummaryLists(protectionRecordDetails: ProtectionRecordDetails)
                                           (implicit messages: Messages): Seq[SummaryList] = {
    protectionRecordDetails.protectionRecords.map(protectionRecordToSummaryList)
  }

}
