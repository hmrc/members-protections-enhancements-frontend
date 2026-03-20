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

class NavigationSpec extends AnyWordSpec with Matchers {

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
        Navigation.nextPage(fromPage, emptyUserAnswers, NormalMode) mustBe toPage
      }
    }
  }

  "previousPageIfNoDataEntered" must {
    Seq(
      MembersDobPage -> Some(WhatIsTheMembersNamePage.route),
      MembersNinoPage -> Some(MembersDobPage.route),
      MembersPsaCheckRefPage -> Some(MembersNinoPage.route)
    ).foreach { case (page, toRoute) =>
      s"return previous route for ${page.toString} when there is no previous data present in user answers" in {
        Navigation.previousPageIfNoDataEntered(page, NormalMode, emptyUserAnswers) mustBe toRoute.map(_(NormalMode))
      }
      s"return None for ${page.toString} when there is previous data present in user answers" in {
        Navigation.previousPageIfNoDataEntered(page, NormalMode, userAnswersWithValues) mustBe None
      }
    }
  }
}
