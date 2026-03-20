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

import models.userAnswers.UserAnswers
import models.{Mode, NormalMode}
import pages.*

object Navigation {
  private val pages: Map[Page, Page] = Map(
    WhatIsTheMembersNamePage -> MembersDobPage,
    MembersDobPage -> MembersNinoPage,
    MembersNinoPage -> MembersPsaCheckRefPage,
    MembersPsaCheckRefPage -> CheckYourAnswersPage,
    ResultsPage -> CheckYourAnswersPage,
    WhatYouWillNeedPage -> WhatIsTheMembersNamePage
  )

  def nextPage(page: Page, userAnswers: UserAnswers, mode: Mode): Page = mode match {
    case NormalMode => pages.getOrElse(page, WhatYouWillNeedPage)
    case _ => CheckYourAnswersPage
  }

  def prevPage(page: Page, userAnswers: UserAnswers, mode: Mode): Page =
    mode match {
      case NormalMode => pages.find(_._2 == page).map(_._1).getOrElse(WhatIsTheMembersNamePage)
      case _ => CheckYourAnswersPage
    }
}
