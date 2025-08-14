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

package forms.mappings

import play.api.data.FieldMapping
import play.api.data.Forms.of

import java.time.LocalDate

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(stringFormatter(errorKey, args))

  protected def localDate(
                           dayInvalidKey: String,
                           monthInvalidKey: String,
                           yearInvalidKey: String,
                           monthTextInvalidKey: String,
                           allRequiredKey: String,
                           twoRequiredKey: String,
                           oneRequiredKey: String,
                           realDateKey: String
                         ): FieldMapping[LocalDate] = of(
    new LocalDateFormatter(
      dayInvalidKey,
      monthInvalidKey,
      yearInvalidKey,
      monthTextInvalidKey,
      oneRequiredKey,
      twoRequiredKey,
      allRequiredKey,
      realDateKey
    )
  )

}
