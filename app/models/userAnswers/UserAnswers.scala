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

package models.userAnswers

import models.CheckMembersDetails
import pages.{CheckYourAnswersPage, QuestionPage}
import play.api.libs.json.*
import queries.{Gettable, Settable}
import utils.encryption.AesGcmAdCrypto
import utils.encryption.Cypher.jsObjectCypher
import utils.encryption.CypherSyntax.*

import java.time.Instant
import scala.util.{Failure, Success, Try}

final case class UserAnswers(id: String, data: JsObject = Json.obj(), lastUpdated: Instant = Instant.now) {
  def get[A](page: Gettable[A])(implicit rds: Reads[A]): Option[A] =
    Reads.optionNoError(Reads.at(page.path)).reads(data).getOrElse(None)

  def set[A](page: Settable[A], value: A)(implicit writes: Writes[A]): Try[UserAnswers] = {
    def updateData(userAnswers: UserAnswers = this): Try[UserAnswers] =
      userAnswers.data.setObject(page.path, Json.toJson(value)) match {
        case JsSuccess(jsValue, _) => Success(userAnswers.copy(data = jsValue))
        case JsError(errors) => Failure(JsResultException(errors))
      }

    page match {
      case _: CheckYourAnswersPage.type => updateData()
      case _ =>
        get(CheckYourAnswersPage).fold(
          updateData()
        )(cyaAnswers =>
          if (cyaAnswers.isChecked) {
            set(CheckYourAnswersPage, CheckMembersDetails(isChecked = false)).flatMap(updateData)
          } else {
            updateData()
          }
        )
    }
  }

  def getOrException[A](page: QuestionPage[A])(implicit rds: Reads[A]): A =
    get(page).getOrElse(throw new RuntimeException("Expected a value but none found for " + page))

  def setOrException[A](page: Settable[A], value: A)(implicit writes: Writes[A]): UserAnswers =
    set(page, value) match {
      case Success(ua) => ua
      case Failure(ex) => throw ex
    }

  def removeWithPath(path: JsPath): UserAnswers =
    data.remove(path) match {
      case JsSuccess(jsValue, _) =>
        UserAnswers(this.id, jsValue.as[JsObject])
      case JsError(_) =>
        throw new RuntimeException("Unable to remove with path: " + path)
    }

  def encrypt(implicit aesGcmAdCrypto: AesGcmAdCrypto, associatedText: String): EncryptedUserAnswers =
    EncryptedUserAnswers(
      id = id,
      encryptedValue = data.encrypted,
      lastUpdated = lastUpdated
    )

}
