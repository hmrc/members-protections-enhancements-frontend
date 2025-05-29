package viewmodels

import base.SpecBase
import models.response.ProtectionRecordDetails
import play.api.Application
import play.api.i18n.Messages
import uk.gov.hmrc.govukfrontend.views.Aliases.Value
import uk.gov.hmrc.govukfrontend.views.viewmodels.content.HtmlContent
import uk.gov.hmrc.govukfrontend.views.viewmodels.summarylist.{Card, CardTitle, Key, SummaryList, SummaryListRow}
import viewmodels.ResultsViewUtils.{optValueToSummaryListRow, protectionRecordDetailsToSummaryLists, protectionRecordToSummaryList}

class ResultsViewUtilsSpec extends SpecBase {

  trait Test {
    val app: Application = applicationBuilder(emptyUserAnswers).build()
    implicit val msg: Messages = messages(app)

    val expectedResult: SummaryList = SummaryList(
      rows = Seq(
        SummaryListRow(
          Key(HtmlContent("Status")),
          Value(HtmlContent(
            s"""
               |<strong class="govuk-tag govuk-tag--green">
               |  Active
               |</strong>
               |- The protection is valid and can be used
               |""".stripMargin
          ))
        ),
        SummaryListRow(Key(HtmlContent("Protection reference number")), Value(HtmlContent("IP141234567890A"))),
        SummaryListRow(Key(HtmlContent("Protected amount")), Value(HtmlContent("£1,440,321")))
      ),
      card = Some(Card(
        title = Some(CardTitle(
          content = HtmlContent("Individual Protection 2014"),
          headingLevel = Some(2)
        )),
        attributes = Map("id" -> "IndividualProtection2014")
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
    "should return the expected SummaryList model for a given set of protection details" in new Test {
      val result: SummaryList = protectionRecordToSummaryList(
        protectionRecord = dummyProtectionRecords.protectionRecords.head
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
