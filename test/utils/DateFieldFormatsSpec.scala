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

import base.SpecBase

class DateFieldFormatsSpec extends SpecBase{
  "numericRegexp" - {
    "should match any string containing only digits and periods" in {
      DateFieldFormats.numericRegexp.r.matches("1.123.234.234.23") mustBe true
    }

    "should not match any string containing any non-digit or period characters" in {
      DateFieldFormats.numericRegexp.r.matches("1.123.234.234.23a") mustBe false
    }

    "should not match an empty string" in {
      DateFieldFormats.numericRegexp.r.matches("") mustBe false
    }
  }

  "decimalRegexp" - {
    "should match any string containing a valid decimal number" in {
      DateFieldFormats.decimalRegexp.r.matches("1.12") mustBe true
    }

    "should not match any string containing any non-digit or period characters" in {
      DateFieldFormats.decimalRegexp.r.matches("1.123a") mustBe false
    }

    "should not match any string containing an integer number" in {
      DateFieldFormats.decimalRegexp.r.matches("11234") mustBe false
    }

    "should not match an empty string" in {
      DateFieldFormats.decimalRegexp.r.matches("") mustBe false
    }
  }

  "integerRegexp" - {
    "should match any string containing a valid integer number" in {
      DateFieldFormats.integerRegexp.r.matches("12312312312") mustBe true
    }

    "should not match any string containing any non-digit characters" in {
      DateFieldFormats.integerRegexp.r.matches("1324234a") mustBe false
    }

    "should not match any string containing a valid decimal number" in {
      DateFieldFormats.integerRegexp.r.matches("123.3") mustBe false
    }

    "should not match an empty string" in {
      DateFieldFormats.integerRegexp.r.matches("") mustBe false
    }
  }
}
