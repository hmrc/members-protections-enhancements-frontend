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

package navigation

import models.*
import models.userAnswers.UserAnswers
import org.scalatest.matchers.must.Matchers
import org.scalatest.wordspec.AnyWordSpec
import pages.*

import java.time.LocalDate

class NavigatorSpec extends AnyWordSpec with Matchers {

  private val emptyUserAnswers = UserAnswers("id")
  val userAnswersWithValues: UserAnswers = emptyUserAnswers
    .setOrException(page = WhatIsTheMembersNamePage, value = MemberDetails("Pearl", "Harvey"))
    .setOrException(page = MembersDobPage, value = MembersDob(LocalDate.of(2000, 1, 1)))
    .setOrException(page = MembersNinoPage, value = MembersNino("AB123456A"))
    .setOrException(page = MembersPsaCheckRefPage, value = MembersPsaCheckRef("PSA12345678A"))

  "nextPage" must {
    Seq(
      WhatIsTheMembersNamePage -> MembersDobPage,
      MembersDobPage -> MembersNinoPage,
      MembersNinoPage -> MembersPsaCheckRefPage,
      MembersPsaCheckRefPage -> CheckYourAnswersPage,
      ResultsPage -> CheckYourAnswersPage,
      WhatYouWillNeedPage -> WhatIsTheMembersNamePage
    ).foreach { case (fromPage, toPage) =>
      s"return correct next page for ${fromPage.toString}" in {
        Navigator.nextPage(fromPage, NormalMode, emptyUserAnswers) mustBe toPage
      }
    }
  }

  val allDataEntryPages: Seq[QuestionPage[?]] = Seq(
    MembersDobPage,
    MembersNinoPage,
    MembersPsaCheckRefPage
  )
  "firstPageWithNoData" must {
    allDataEntryPages.foreach { page =>
      s"return first route for ${page.toString} when there is no previous data present in user answers" in {
        Navigator.firstPreviousPageWithNoData(page, NormalMode, emptyUserAnswers) mustBe Some(
          WhatIsTheMembersNamePage
            .route(NormalMode)
        )
      }
      s"return None for ${page.toString} when there is full previous data present in user answers" in {
        Navigator.firstPreviousPageWithNoData(page, NormalMode, userAnswersWithValues) mustBe None
      }

      // Test that nav goes back to the first page (in order) which doesn't have any value entered
      val priorDataEntryPages = allDataEntryPages.takeWhile(_ != page)
      priorDataEntryPages.foreach { pageToRemove =>
        s"return ${pageToRemove.toString} route for ${page.toString} when there is no previous data present in user answers for ${pageToRemove.toString}" in {
          val userAnswers = userAnswersWithValues.removeWithPath(pageToRemove.path)
          Navigator.firstPreviousPageWithNoData(page, NormalMode, userAnswers) mustBe Some(
            pageToRemove.route(NormalMode)
          )
        }
      }

      // TODO: Add test that doesn't return page that comes AFTER page passed into firstPreviousPageWithNoData
      // TODO: Add tests for submitUrl and backLinkUrl
    }

  }
}
