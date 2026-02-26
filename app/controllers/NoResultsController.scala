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

package controllers

import controllers.actions.{CheckLockoutAction, DataRetrievalAction, IdentifierAction}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import providers.DateTimeProvider
import utils.DateTimeFormats
import views.html.NoResultsView

import javax.inject.Inject
import scala.concurrent.Future

class NoResultsController @Inject() (
  override val messagesApi: MessagesApi,
  identify: IdentifierAction,
  checkLockout: CheckLockoutAction,
  getData: DataRetrievalAction,
  val controllerComponents: MessagesControllerComponents,
  view: NoResultsView,
  dateTimeProvider: DateTimeProvider
) extends MpeBaseController(identify, checkLockout, getData) {

  def onPageLoad(): Action[AnyContent] = handleWithCheckedAnswers {
    implicit request => memberDetails => membersDob => membersNino => membersPsaCheckRef => _ =>
      Future.successful(
        Ok(
          view(
            memberDetails = memberDetails,
            membersDob = membersDob,
            membersNino = membersNino,
            membersPsaCheckRef = membersPsaCheckRef,
            formattedTimestamp = DateTimeFormats.getCurrentDateTimestamp(dateTimeProvider.now())
          )
        )
      )
  }
}
