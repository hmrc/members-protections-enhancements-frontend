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
import controllers.actions.{CheckLockoutAction, DataRetrievalAction, IdentifierAction}
import forms.MembersPsaCheckRefFormProvider
import models.{CorrelationId, MembersPsaCheckRef, Mode}
import navigation.Navigator
import pages.MembersPsaCheckRefPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{Action, AnyContent, MessagesControllerComponents}
import services.SessionCacheService
import utils.Logging
import views.html.MembersPsaCheckRefView

import scala.concurrent.{ExecutionContext, Future}

class MembersPsaCheckRefController @Inject()(override val messagesApi: MessagesApi,
                                             identify: IdentifierAction,
                                             checkLockout: CheckLockoutAction,
                                             getData: DataRetrievalAction,
                                             navigator: Navigator,
                                             service: SessionCacheService,
                                             formProvider: MembersPsaCheckRefFormProvider,
                                             implicit val controllerComponents: MessagesControllerComponents,
                                             view: MembersPsaCheckRefView)(implicit ec: ExecutionContext)
  extends MpeBaseController(identify, checkLockout, getData) with Logging{

  private val form: Form[MembersPsaCheckRef] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = handleWithMemberNino("onPageLoad") { implicit request =>
    val methodLoggingContext: String = "onPageLoad"
    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(request.correlationId))

    infoLogger("Attempting to find existing user answers for 'member PSA check ref' page")

    membersDetails => _ => _ =>
      request.userAnswers.get(MembersPsaCheckRefPage) match {
        case None =>
          infoLogger("No user answers exist for 'member PSA check ref' page. Attempting to serve blank view")
          Future.successful(Ok(view(form, viewModel(mode, MembersPsaCheckRefPage), membersDetails.fullName)))
        case Some(value) =>
          infoLogger("Previous user answers exist for 'member PSA check ref' page. Attempting to serve pre-filled view")
          Future.successful(Ok(view(form.fill(value), viewModel(mode, MembersPsaCheckRefPage), membersDetails.fullName)))
      }
  }

  def onSubmit(mode: Mode): Action[AnyContent] = handleWithMemberDetails("onSubmit") { implicit request =>
    val methodLoggingContext: String = "onSubmit"
    implicit val correlationId: CorrelationId = request.correlationId

    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(correlationId))

    val warnLogger: (String, Option[Throwable]) => Unit = warnLog(
      secondaryContext = methodLoggingContext,
      dataLog = correlationIdLogString(correlationId)
    )

    infoLogger("Attempting to validate and save submitted user answers for 'member psa check ref' page")

    memberDetails =>
      form
        .bindFromRequest()
        .fold(
          formWithErrors => {
            warnLogger(s"Failed to validate user submission with errors: ${formWithErrors.errors}", None)
            Future.successful(BadRequest(view(
              form = formWithErrors,
              viewModel = viewModel(mode, MembersPsaCheckRefPage),
              name = memberDetails.fullName
            )))
          },
          answer => {
            infoLogger("Successfully validated user submission. Attempting to update session data cache")
            for {
              updatedAnswers <- Future.fromTry(request.userAnswers.set(MembersPsaCheckRefPage, answer))
              _ <- service.save(updatedAnswers)
            } yield {
              infoLogger("Successfully updated session data cache. Redirecting to next page")
              Redirect(navigator.nextPage(MembersPsaCheckRefPage, mode, updatedAnswers))
            }
          }
        )
  }
}
