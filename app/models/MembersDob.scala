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

package models

import play.api.libs.json.{Format, Json}
import utils.DateTimeFormats

import java.time.LocalDate
import java.time.format.DateTimeFormatter

case class MembersDob(day: Int, month: Int, year: Int) {
  lazy val date: LocalDate = LocalDate.of(year, month, day)
  lazy val dob: String = date.format(DateTimeFormatter.ofPattern("dd MMMM yyyy"))
  lazy val dateOfBirth: String = date.format(DateTimeFormats.apiDateTimeFormat)
}

object MembersDob {
  implicit val format: Format[MembersDob] = Json.format[MembersDob]
}
