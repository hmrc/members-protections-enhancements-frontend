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

package controllers

import com.google.inject.Inject
import controllers.ResultsController.tempStaticData
import controllers.actions.{DataRetrievalAction, IdentifierAction}
import models.response.RecordStatusMapped.{Active, Dormant, Withdrawn}
import models.response.RecordTypeMapped._
import models.response.{ProtectionRecord, ProtectionRecordDetails}
import pages.{MembersDobPage, MembersNinoPage, MembersPsaCheckRefPage, WhatIsTheMembersNamePage}
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import utils.DateTimeFormats
import views.html.ResultsView

import scala.concurrent.Future

class ResultsController @Inject()(override val messagesApi: MessagesApi,
                                  identify: IdentifierAction,
                                  getData: DataRetrievalAction,
                                  val controllerComponents: MessagesControllerComponents,
                                  view: ResultsView)
  extends MpeBaseController(identify, getData) {

  def onPageLoad(): Action[AnyContent] = handle {
    implicit request =>

      val result = for {
        memberDetails <- request.userAnswers.get(WhatIsTheMembersNamePage)
        dob <- request.userAnswers.get(MembersDobPage)
        nino <- request.userAnswers.get(MembersNinoPage)
        psaRefCheck <- request.userAnswers.get(MembersPsaCheckRefPage)
      } yield Future.successful(Ok(
        view(
          memberDetails = memberDetails,
          membersDob = dob,
          membersNino = nino,
          membersPsaCheckRef = psaRefCheck,
          backLinkUrl = Some(routes.CheckYourAnswersController.onPageLoad().url),
          formattedTimestamp = DateTimeFormats.getCurrentDateTimestamp(),
          protectionRecordDetails = tempStaticData
        )
      ))

      result.getOrElse(
        Future.successful(Redirect(routes.JourneyRecoveryController.onPageLoad()))
      )
  }
}

object ResultsController {
  @deprecated("This purely exists to test the display functionality and should be removed ASAP")
  val tempStaticData: ProtectionRecordDetails = ProtectionRecordDetails(
    Seq(
      ProtectionRecord(
        protectionReference = Some("IP141234567890A"),
        `type` = IndividualProtection2014,
        status = Active,
        protectedAmount = Some(1440321),
        lumpSumAmount = None,
        lumpSumPercentage = None,
        enhancementFactor = None
      ),
      ProtectionRecord(
        protectionReference = Some("FP1612345678901A"),
        `type` = FixedProtection2016,
        status = Dormant,
        protectedAmount = None,
        lumpSumAmount = None,
        lumpSumPercentage = None,
        enhancementFactor = None
      ),
      ProtectionRecord(
        protectionReference = None,
        `type` = PrimaryProtection,
        status = Withdrawn,
        protectedAmount = None,
        lumpSumAmount = Some(34876),
        lumpSumPercentage = Some(21),
        enhancementFactor = None
      ),
      ProtectionRecord(
        protectionReference = Some("IE211234567890A"),
        `type` = InternationalEnhancementTransfer,
        status = Active,
        protectedAmount = Some(1440321),
        lumpSumAmount = None,
        lumpSumPercentage = None,
        enhancementFactor = Some(0.12)
      )
    )
  )
}
