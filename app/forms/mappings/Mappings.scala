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

import models.MembersDob
import play.api.data.Forms.{mapping, of}
import play.api.data.{FieldMapping, Mapping}
import providers.DateTimeProvider

trait Mappings extends Formatters with Constraints {

  protected def text(errorKey: String = "error.required", args: Seq[String] = Seq.empty): FieldMapping[String] =
    of(stringFormatter(errorKey, args))

  protected def dateOfBirth(dateTimeProvider: DateTimeProvider): Mapping[MembersDob] = {
    val baseError = "membersDob.error"
    val baseFormatError = s"$baseError.format"
    val baseMissingError = s"$baseError.missing"

    val dayString: String = "day"
    val monthString: String = "month"
    val yearString: String = "year"

    val dayError = s"$baseFormatError.$dayString"
    val monthError = s"$baseFormatError.$monthString"
    val yearError = s"$baseFormatError.$yearString"

    mapping(
      dayString -> int(
        requiredKey = s"$baseMissingError.$dayString",
        wholeNumberKey = dayError,
        nonNumericKey = dayError
      ).verifying(dayError, day => day > 0 && day < 32),
      monthString -> month(
        requiredKey = s"$baseMissingError.$monthString",
        wholeNumberKey = monthError,
        nonNumericKey = monthError,
        invalidMonthKey = s"$monthError.nonNumeric"
      ).verifying(monthError, month => month >= 1 && month <= 12),
      yearString -> int(
        requiredKey = s"$baseMissingError.$yearString",
        wholeNumberKey = yearError,
        nonNumericKey = yearError
      ).verifying(yearError, year => year >= minYear && year <= dateTimeProvider.now().getYear)
    )(MembersDob.apply)(MembersDob.unapply)
  }

  protected def int(requiredKey: String,
                    wholeNumberKey: String,
                    nonNumericKey: String): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey))

  protected def month(requiredKey: String,
                      wholeNumberKey: String,
                      nonNumericKey: String,
                      invalidMonthKey: String): FieldMapping[Int] =
    of(monthFormatter(requiredKey, wholeNumberKey, nonNumericKey, invalidMonthKey))
}
