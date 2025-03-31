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

import java.time.LocalDate
import play.api.data.validation.{Constraint, Invalid, Valid}

import scala.util.Try

trait Constraints {

  val minYear: Int = 1900
  val maxDate: LocalDate = LocalDate.now()

  protected def firstError[A](constraints: Constraint[A]*): Constraint[A] =
    Constraint {
      input =>
        constraints
          .map(_.apply(input))
          .find(_ != Valid)
          .getOrElse(Valid)
    }

  protected def minimumValue[A](minimum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum) {
          Valid
        } else {
          Invalid(errorKey, minimum)
        }
    }

  protected def maximumValue[A](maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, maximum)
        }
    }

  protected def inRange[A](minimum: A, maximum: A, errorKey: String)(implicit ev: Ordering[A]): Constraint[A] =
    Constraint {
      input =>

        import ev._

        if (input >= minimum && input <= maximum) {
          Valid
        } else {
          Invalid(errorKey, minimum, maximum)
        }
    }

  protected def regexp(regex: String, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.matches(regex) =>
        Valid
      case _ =>
        Invalid(errorKey, regex)
    }

  protected def maxLength(maximum: Int, errorKey: String): Constraint[String] =
    Constraint {
      case str if str.length <= maximum =>
        Valid
      case _ =>
        Invalid(errorKey, maximum)
    }

  protected def maxDate(errorKey: String, args: Any*): Constraint[MembersDob] =
    Constraint {
      case membersDob if isFutureDate(membersDob) =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def validDate(errorKey: String, args: Any*): Constraint[MembersDob] =
    Constraint {
      case membersDob if isValidDate(membersDob) => Valid
      case _ => Invalid(errorKey, args: _*)
    }

  protected def minDate(minimum: Int, errorKey: String, args: Any*): Constraint[MembersDob] =
    Constraint {
      case membersDob if toLocalDate(membersDob).getYear < minimum =>
        Invalid(errorKey, args: _*)
      case _ =>
        Valid
    }

  protected def nonEmptySet(errorKey: String): Constraint[Set[_]] =
    Constraint {
      case set if set.nonEmpty =>
        Valid
      case _ =>
        Invalid(errorKey)
    }

  private def toLocalDate(input: MembersDob): LocalDate =
    LocalDate.of(input.year, input.month, input.day)

  private def isValidDate(input: MembersDob): Boolean = Try(LocalDate.of(input.year, input.month, input.day)).isSuccess

  private def isFutureDate(input: MembersDob): Boolean =
    if(isValidDate(input) && toLocalDate(input).isAfter(maxDate)){ true }
    else { false }

  protected def toMembersDob(input: LocalDate): MembersDob = {
    MembersDob(input.getDayOfMonth, input.getMonthValue, input.getYear)
  }

}
