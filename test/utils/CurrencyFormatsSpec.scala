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

import org.scalatest.freespec.AnyFreeSpec
import org.scalatest.matchers.must.Matchers
import play.api.i18n.Lang
import utils.DateTimeFormats.dateTimeFormat

import java.time.LocalDate

class CurrencyFormatsSpec extends AnyFreeSpec with Matchers {
  "format" -> {
    "should convert a number to the correct currency string format" in {
      CurrencyFormats.format(1) mustBe "£1"
    }

    "should correctly add commas for large numbers" in {
      CurrencyFormats.format(1000) mustBe "£1,000"
    }

    "should not show empty decimals" in {
      CurrencyFormats.format(1.0) mustBe "£1"
    }
  }

  "formatOptInt" -> {
    "should return nothing for an empty val" in {
      CurrencyFormats.formatOptInt(None) mustBe None
    }

    "should format an existing int val" in {
      CurrencyFormats.formatOptInt(Some(1234567)) mustBe Some("£1,234,567")
    }
  }
}
