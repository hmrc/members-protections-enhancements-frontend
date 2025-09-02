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
import forms.MembersDobFormProvider
import models.{CorrelationId, MembersDob, Mode}
import navigation.Navigator
import pages.MembersDobPage
import play.api.data.Form
import play.api.i18n.MessagesApi
import play.api.mvc.{request, _}
import services.SessionCacheService
import utils.NewLogging
import views.html.MembersDobView

import scala.concurrent.{ExecutionContext, Future}

class MembersDobController @Inject()(override val messagesApi: MessagesApi,
                                     identify: IdentifierAction,
                                     checkLockout: CheckLockoutAction,
                                     getData: DataRetrievalAction,
                                     navigator: Navigator,
                                     service: SessionCacheService,
                                     formProvider: MembersDobFormProvider,
                                     implicit val controllerComponents: MessagesControllerComponents,
                                     view: MembersDobView)(implicit ec: ExecutionContext)
  extends MpeBaseController(identify, checkLockout, getData) with NewLogging {

  private val form: Form[MembersDob] = formProvider()

  def onPageLoad(mode: Mode): Action[AnyContent] = handleWithMemberDetails("onPageLoad") { implicit request =>
    val methodLoggingContext: String = "onPageLoad"
    val infoLogger: String => Unit = infoLog(methodLoggingContext, correlationIdLogString(request.correlationId))

    infoLogger("Attempting to find existing user answers for 'member dob' page")

    memberDetails =>
      request.userAnswers.get(MembersDobPage) match {
        case None =>
          infoLogger("No user answers exist for 'member DOB' page. Attempting to serve blank view")
          Future.successful(Ok(view(form, viewModel(mode, MembersDobPage), memberDetails.fullName)))
        case Some(value) =>
          infoLogger("Previous user answers exist for 'member DOB' page. Attempting to serve pre-filled view")
          Future.successful(Ok(view(form.fill(value), viewModel(mode, MembersDobPage), memberDetails.fullName)))
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

    infoLogger("Attempting to validate and save submitted user answers for 'member DOB' page")

    memberDetails =>
      form.bindFromRequest().fold(
        formWithErrors => {
          warnLogger(s"Failed to validate user submission with errors: ${formWithErrors.errors}", None)
          Future.successful(BadRequest(view(
            form = formWithErrors,
            viewModel = viewModel(mode, MembersDobPage),
            name = memberDetails.fullName
          )))
        },
        answer => {
          infoLogger("Successfully validated user submission. Attempting to update session data cache")
          for {
            updatedAnswers <- Future.fromTry(request.userAnswers.set(MembersDobPage, answer))
            _ <- service.save(updatedAnswers)
          } yield {
            infoLogger("Successfully updated session data cache. Redirecting to next page")
            Redirect(navigator.nextPage(MembersDobPage, mode, updatedAnswers))
          }
        }
      )
  }
}
