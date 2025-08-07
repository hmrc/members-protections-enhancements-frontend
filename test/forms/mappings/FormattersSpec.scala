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

import forms.mappings.Formatters.{validateInt, validateMonth}
import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.FormError
import play.api.data.format.Formatter

class FormattersSpec extends AnyFreeSpec with Matchers with OptionValues with Mappings {
  "stringFormatter" - {
    val testStringFormatter = stringFormatter("error")

    "must bind a valid string" in {
      val result: Either[Seq[FormError], String] = testStringFormatter.bind("value", Map("value" -> "foobar"))
      result.toOption mustBe Some("foobar")
    }

    "must not bind an empty string" in {
      val result: Either[Seq[FormError], String] = testStringFormatter.bind("value", Map("value" -> ""))
      result.swap.getOrElse(Nil) must contain(FormError("value", "error"))
    }

    "must not bind a string of whitespace only" in {
      val result: Either[Seq[FormError], String] = testStringFormatter.bind("value", Map("value" -> " \t"))
      result.swap.getOrElse(Nil) must contain(FormError("value", "error"))
    }

    "must not bind an empty map" in {
      val result: Either[Seq[FormError], String] = testStringFormatter.bind("value", Map.empty[String, String])
      result.swap.getOrElse(Nil) must contain(FormError("value", "error"))
    }

    "must unbind a valid value" in {
      val result = testStringFormatter.unbind("value", "foo")
      result.apply("value") mustBe "foo"
    }
  }

  "intFormatter" - {
    val testIntFormatter: Formatter[Int] = intFormatter("required", "wholeNumber", "nonNumeric")

    "must bind a valid string" in {
      val intVal: Int = 39
      val result = testIntFormatter.bind("value", Map("value" -> intVal.toString))
      result mustBe a[Right[_, _]]
      result mustBe Right(intVal)
    }

    "must not bind a valid decimal string" in {
      val decimalVal: Double = 39.3
      val result = testIntFormatter.bind("value", Map("value" -> decimalVal.toString))
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(Nil) must contain(FormError("value", "wholeNumber"))
    }

    "must ignore whitespace" in {
      val intVal: Int = 39
      val result = testIntFormatter.bind("value", Map("value" -> s"     $intVal"))
      result mustBe a[Right[_, _]]
      result mustBe Right(intVal)
    }

    "must not bind an empty string" in {
      val result = testIntFormatter.bind("value", Map("value" -> ""))
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(Nil) must contain(FormError("value", "required"))
    }

    "must not bind an empty map" in {
      val result = testIntFormatter.bind("value", Map.empty[String, String])
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(Nil) must contain(FormError("value", "required"))
    }

    "must not bind any other invalid string" in {
      val result = testIntFormatter.bind("value", Map("value" -> "beep"))
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(Nil) must contain(FormError("value", "nonNumeric"))
    }
  }

  "monthFormatter" - {
    val testMonthFormatter: Formatter[Int] = monthFormatter("required", "wholeNumber", "nonNumeric", "invalidMonth")

    "must bind a valid month int" in {
      val intVal: Int = 11
      val result = testMonthFormatter.bind("value", Map("value" -> intVal.toString))
      result mustBe a[Right[_, _]]
      result mustBe Right(intVal)
    }

    "must bind a valid month short code" in {
      val shortVal: String = "Jan"
      val intVal: Int = 1
      val result = testMonthFormatter.bind("value", Map("value" -> shortVal))
      result mustBe a[Right[_, _]]
      result mustBe Right(intVal)
    }

    "must bind a valid month long code" in {
      val shortVal: String = "January"
      val intVal: Int = 1
      val result = testMonthFormatter.bind("value", Map("value" -> shortVal))
      result mustBe a[Right[_, _]]
      result mustBe Right(intVal)
    }

    "must not bind an empty string" in {
      val result = testMonthFormatter.bind("value", Map("value" -> ""))
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(Nil) must contain(FormError("value", "required"))
    }

    "must not bind an empty map" in {
      val result = testMonthFormatter.bind("value", Map.empty[String, String])
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(Nil) must contain(FormError("value", "required"))
    }

    "must not bind an invalid numeric string" in {
      val result = testMonthFormatter.bind("value", Map("value" -> "123..123.2323.2323"))
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(Nil) must contain(FormError("value", "nonNumeric"))
    }

    "must not bind any other invalid string" in {
      val result = testMonthFormatter.bind("value", Map("value" -> "beep"))
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(Nil) must contain(FormError("value", "invalidMonth"))
    }
  }

  "validateInt" - {
    "should complete successfully for a valid submission" in {
      validateInt("1", "value", "err", "err") mustBe Right(1)
    }

    "should return an error for a decimal string" in {
      val result: ValidationResult[Int] = validateInt("1.1", "value", "wholeNumber", "err")
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(Nil) must contain(FormError("value", "wholeNumber"))
    }

    "should return an error for a non numeric string" in {
      val result: ValidationResult[Int] = validateInt("N/A", "value", "err", "nonNumeric")
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(Nil) must contain(FormError("value", "nonNumeric"))
    }

    "should return an error for an empty string" in {
      val result: ValidationResult[Int] = validateInt("", "value", "err", "nonNumeric")
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(Nil) must contain(FormError("value", "nonNumeric"))
    }
  }

  "validateMonth" - {
    "should complete successfully for a valid short submission" in {
      validateMonth("Jan", "value", "err") mustBe Right(1)
    }

    "should complete successfully for a valid lower case short submission" in {
      validateMonth("jan", "value", "err") mustBe Right(1)
    }

    "should complete successfully for a valid upper case short submission" in {
      validateMonth("JAN", "value", "err") mustBe Right(1)
    }

    "should complete successfully for a valid long submission" in {
      validateMonth("March", "value", "err") mustBe Right(3)
    }

    "should complete successfully for a valid lower case long submission" in {
      validateMonth("march", "value", "err") mustBe Right(3)
    }

    "should complete successfully for a valid upper case long submission" in {
      validateMonth("MARCH", "value", "err") mustBe Right(3)
    }

    "should return an error for an invalid string" in {
      val result: ValidationResult[Int] = validateMonth("N/A", "value", "err")
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(Nil) must contain(FormError("value", "err"))
    }

    "should return an error for an empty string" in {
      val result: ValidationResult[Int] = validateMonth("", "value", "err")
      result mustBe a[Left[_, _]]
      result.swap.getOrElse(Nil) must contain(FormError("value", "err"))
    }
  }
}
