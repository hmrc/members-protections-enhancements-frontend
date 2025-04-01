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

import models.{Enumerable, MembersDob}
import play.api.data.Forms.{mapping, of}
import play.api.data.{FieldMapping, Mapping}
import play.api.i18n.Messages

import java.time.LocalDate

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(stringFormatter(errorKey, args))

  protected def int(requiredKey: String = "error.required",
                    wholeNumberKey: String = "error.wholeNumber",
                    nonNumericKey: String = "error.nonNumeric",
                    args: Seq[String] = Seq.empty): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey, args))

  protected def boolean(requiredKey: String = "error.required",
                        invalidKey: String = "error.boolean",
                        args: Seq[String] = Seq.empty): FieldMapping[Boolean] =
    of(booleanFormatter(requiredKey, invalidKey, args))


  protected def enumerable[A](requiredKey: String = "error.required",
                              invalidKey: String = "error.invalid",
                              args: Seq[String] = Seq.empty)(implicit ev: Enumerable[A]): FieldMapping[A] =
    of(enumerableFormatter[A](requiredKey, invalidKey, args))

  protected def localDate(
                           invalidKey: String,
                           allRequiredKey: String,
                           twoRequiredKey: String,
                           requiredKey: String,
                           args: Seq[String] = Seq.empty)(implicit messages: Messages): FieldMapping[LocalDate] =
    of(new LocalDateFormatter(invalidKey, allRequiredKey, twoRequiredKey, requiredKey, args))

  protected def dateMapping: Mapping[MembersDob] = {

    mapping(
      "day" -> int(requiredKey = "membersDob.error.required.day", wholeNumberKey = "membersDob.error.invalid.day", nonNumericKey =
        "membersDob.error.invalid.day").verifying("membersDob.error.invalid.day", d => d > 0 && d < 32),
      "month" -> int(requiredKey = "membersDob.error.required.month", wholeNumberKey = "membersDob.error.invalid.month",
        nonNumericKey = "membersDob.error.invalid.month").verifying("membersDob.error.invalid.month", m => m > 0 && m < 13),
      "year" -> int(requiredKey = "membersDob.error.required.year", wholeNumberKey = "membersDob.error.invalid.year", nonNumericKey
      = "membersDob.error.invalid.year").verifying("membersDob.error.invalid.year", y => y >= minYear && y <= maxDate.getYear)
    )(MembersDob.apply)(MembersDob.unapply)
  }

  protected def int(requiredKey: String,
                    wholeNumberKey: String,
                    nonNumericKey: String): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey))
}
