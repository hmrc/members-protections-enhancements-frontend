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

package models.errors

import play.api.libs.json._

sealed trait ErrorSource {
  def value: String
}

object ErrorSource {

  private val MATCH_PERSON: String = "MatchPerson"
  private val RETRIEVE_MPE: String = "RetrieveMpe"
  private val INTERNAL: String = "Internal"

  case object MatchPerson extends ErrorSource {
    override val value: String = MATCH_PERSON
  }
  case object RetrieveMpe extends ErrorSource {
    override val value: String = RETRIEVE_MPE
  }
  case object Internal extends ErrorSource {
    override val value: String = INTERNAL
  }

  implicit val reads: Reads[ErrorSource] = Reads[ErrorSource] {
    case JsString(MATCH_PERSON) => JsSuccess(MatchPerson)
    case JsString(RETRIEVE_MPE) => JsSuccess(RetrieveMpe)
    case JsString(INTERNAL) => JsSuccess(Internal)
    case _ => JsError("error.usertype.invalid")
  }

  implicit val writes: Writes[ErrorSource] = Writes[ErrorSource] { errorSource =>
    JsString(errorSource.value)
  }
}
