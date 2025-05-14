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

package utils

import play.api.i18n.Lang

import java.time.format.DateTimeFormatter
import java.time.{ZoneId, ZonedDateTime}
import java.util.Locale

object DateTimeFormats {

  private val dateFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy")
  val dateTimeFormatter = DateTimeFormatter.ofPattern("dd MMMM yyyy 'at' HH:mm")
  val dateTimeHintFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("d M yyyy")
  val apiDateTimeFormat: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")

  private val localisedDateFormatters = Map(
    "en" -> dateFormatter,
    "cy" -> dateFormatter.withLocale(new Locale("cy"))
  )

  def dateTimeFormat()(implicit lang: Lang): DateTimeFormatter = {
    localisedDateFormatters.getOrElse(lang.code, dateFormatter)
  }

  def getCurrentDateTimestamp(formatter: DateTimeFormatter = dateTimeFormatter): String = {
    val dateTimeWithZone = ZonedDateTime.now(ZoneId.of("Europe/London"))
    formatter.format(dateTimeWithZone)
  }

}
