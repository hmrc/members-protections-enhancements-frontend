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

package forms.mappings

import org.scalatest.OptionValues
import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.data.FormError

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
}
