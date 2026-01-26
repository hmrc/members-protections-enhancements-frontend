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

package viewmodels

import base.SpecBase
import models.response.ProtectionRecordDetails
import models.response.RecordTypeMapped.InternationalEnhancementTransfer
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist._
import viewmodels.ResultsViewUtils.{optValueToSummaryListRow, protectionRecordDetailsToSummaryLists, protectionRecordToSummaryList}

class ResultsViewUtilsSpec extends SpecBase {

  trait Test {
    val app: Application = applicationBuilder(emptyUserAnswers).build()
    implicit val msg: Messages = messages(app)

    val recordType: String = "protection"
    val recordName: String = "Individual protection 2014"
    val recordId: String = "IndividualProtection2014"

    lazy val expectedResult: SummaryList = SummaryList(
      rows = Seq(
        SummaryListRow(
          Key(HtmlContent("Status")),
          Value(HtmlContent(
            s"""
               |<strong class="govuk-tag govuk-tag--green">
               |Active
               |</strong>
               |- the $recordType is valid and can be used
               |""".stripMargin
          ))
        ),
        SummaryListRow(Key(HtmlContent("Protection reference number")), Value(HtmlContent("IP141234567890A"))),
        SummaryListRow(Key(HtmlContent("Protected amount")), Value(HtmlContent("£1,440,321")))
      ),
      card = Some(Card(
        title = Some(CardTitle(
          content = HtmlContent(recordName),
          headingLevel = Some(2)
        )),
        attributes = Map("id" -> recordId)
      ))
    )
  }

  "optValueToSummaryListRow" - {
    "should return an empty Seq when provided val is None" in new Test {
      val result: Seq[SummaryListRow] = optValueToSummaryListRow(None, "")
      result mustBe empty
    }

    "should return the expected summary list row when val exists" in new Test {
      val result: Seq[SummaryListRow] = optValueToSummaryListRow(Some("exists"), "results.lumpSumKey")
      result mustBe Seq(SummaryListRow(
        key = Key(HtmlContent("Lump sum")),
        Value(HtmlContent("exists"))
      ))
    }
  }

  "protectionRecordToSummaryList" -> {
    "should return the expected SummaryList model for a protection" in new Test {
      val result: SummaryList = protectionRecordToSummaryList(
        protectionRecord = dummyProtectionRecords.protectionRecords.head
      )

      result.card mustBe expectedResult.card
      result.attributes mustBe expectedResult.attributes
      result.rows mustBe expectedResult.rows
    }

    "should return the expected SummaryList model for an enhancement" in new Test {
      override val recordType: String = "enhancement"
      override val recordName: String = "Non-residence factor for a transfer from an overseas pension scheme"
      override val recordId: String = "InternationalEnhancementTransfer"

      val result: SummaryList = protectionRecordToSummaryList(
        protectionRecord = dummyProtectionRecords.protectionRecords.head.copy(
          `type` = InternationalEnhancementTransfer
        )
      )

      result.card mustBe expectedResult.card
      result.attributes mustBe expectedResult.attributes
      result.rows mustBe expectedResult.rows
    }
  }

  "protectionRecordDetailsToSummaryLists" -> {
    "must return the expected result" in new Test {
      val result: Seq[SummaryList] = protectionRecordDetailsToSummaryLists(
        protectionRecordDetails = ProtectionRecordDetails(
          protectionRecords = Seq(dummyProtectionRecords.protectionRecords.head)
        )
      )

      result mustBe Seq(expectedResult)
    }
  }
}
