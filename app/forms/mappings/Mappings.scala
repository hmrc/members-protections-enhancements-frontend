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
    val invalidError: String = "membersDob.error.invalid"
    val missingError: String = "membersDob.error.missing"

    val dayError: String = s".day"
    val monthError: String = s".month"
    val yearError: String = s".year"

    val invalidDayError: String = invalidError + dayError
    val invalidMonthError: String = invalidError + monthError
    val invalidYearError: String = invalidError + yearError

    mapping(
      "day" -> int(
        requiredKey = missingError + dayError,
        wholeNumberKey = invalidDayError,
        nonNumericKey = invalidDayError
      ).verifying(invalidDayError, d => d > 0 && d < 32),
      "month" -> month(
        requiredKey = missingError + monthError,
        wholeNumberKey = invalidMonthError,
        nonNumericKey = s"$invalidMonthError.nonNumeric"
      ).verifying(invalidMonthError, m => m > 0 && m < 13),
      "year" -> int(
        requiredKey = missingError + yearError,
        wholeNumberKey = invalidYearError,
        nonNumericKey = invalidYearError
      ).verifying(invalidYearError, y => y >= minYear && y <= dateTimeProvider.now().getYear)
    )(MembersDob.apply)(MembersDob.unapply)
  }

  protected def month(requiredKey: String,
                      wholeNumberKey: String,
                      nonNumericKey: String): FieldMapping[Int] =
    of(monthFormatter(requiredKey, wholeNumberKey, nonNumericKey))

  protected def int(requiredKey: String,
                    wholeNumberKey: String,
                    nonNumericKey: String): FieldMapping[Int] =
    of(intFormatter(requiredKey, wholeNumberKey, nonNumericKey))
}
