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

import base.SpecBase
import controllers.actions.IdentifierAction
import models.requests.IdentifierRequest
import org.mockito.ArgumentMatchers
import org.mockito.Mockito.when
import play.api.Application
import play.api.inject.bind
import play.api.mvc.Results.Redirect
import play.api.mvc.{AnyContent, Result}
import play.api.test.FakeRequest
import play.api.test.Helpers._
import providers.DateTimeProvider
import services.FailedAttemptService
import views.html.LockedOutView

import java.time.{Instant, ZoneId, ZonedDateTime}
import scala.concurrent.Future

class LockedOutControllerSpec extends SpecBase {

  trait Test {
    val mockDateTimeProvider: DateTimeProvider = mock[DateTimeProvider]
    val mockService: FailedAttemptService = mock[FailedAttemptService]

    val application: Application = applicationBuilder(
      userAnswers = emptyUserAnswers,
    )
      .overrides(
        bind(classOf[DateTimeProvider]).toInstance(mockDateTimeProvider),
        bind[FailedAttemptService].toInstance(mockService)
      )
      .build()
  }

  "LockedOut Controller" - {
    "must redirect appropriately when user is not authenticated" in new Test {
      val mockIdentifierAction: IdentifierAction = mock[IdentifierAction]

      override val application: Application = applicationBuilder(
        userAnswers = emptyUserAnswers,
        identifierAction = mockIdentifierAction
      ).build()

      when(
        mockIdentifierAction.async(
          ArgumentMatchers.any[IdentifierRequest[AnyContent] => Future[Result]]()
        )
      ).thenReturn(
        stubMessagesControllerComponents().messagesActionBuilder.async(
          _ => Future.successful(Redirect(routes.UnauthorisedController.onPageLoad()))
        )
      )

      running(application) {
        val request = FakeRequest(GET, routes.LockedOutController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.UnauthorisedController.onPageLoad().url)
      }
    }

    "must return OK and the correct view for a GET when user is locked out" in new Test {
      val timestampSeconds: Long = 100000L
      val instantTime: Instant = Instant.ofEpochSecond(timestampSeconds)
      val getExpiryResult: Future[Option[Instant]] = Future.successful(Some(instantTime))

      when(
        mockService.getLockoutExpiry()(ArgumentMatchers.any())
      ).thenReturn(
        getExpiryResult
      )

      when(
        mockDateTimeProvider.now()
      ).thenReturn(
        ZonedDateTime.ofInstant(instantTime, ZoneId.of("Europe/London"))
      )

      running(application) {
        val request = FakeRequest(GET, routes.LockedOutController.onPageLoad().url)
        val result = route(application, request).value

        val view = application.injector.instanceOf[LockedOutView]

        status(result) mustEqual OK
        contentAsString(result) mustEqual view(15)(request, messages(application)).toString
      }
    }

    "must redirect to start page when user is not locked out" in new Test{
      when(
        mockService.getLockoutExpiry()(ArgumentMatchers.any())
      ).thenReturn(
        Future.successful(None)
      )

      running(application) {
        val request = FakeRequest(GET, routes.LockedOutController.onPageLoad().url)
        val result = route(application, request).value

        status(result) mustEqual SEE_OTHER
        redirectLocation(result) mustBe Some(routes.ClearCacheController.onPageLoad().url)
      }
    }
  }
}
