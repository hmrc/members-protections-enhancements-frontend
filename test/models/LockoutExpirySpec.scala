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

package models

import base.SpecBase

import java.time.Instant

class LockoutExpirySpec extends SpecBase {
  "apply" -> {
    val timeoutTtl: Long = 120L
    "should convert a timestamp and expiry amount into a LockoutExpiry object" in {
      LockoutExpiry(
        lockoutCreated = Instant.parse("2025-11-11T10:15:30.00Z"),
        lockoutExpiry = timeoutTtl,
        currentTime = Instant.parse("2025-11-11T10:15:32.00Z")
      ) mustBe LockoutExpiry(minutes = 1, seconds = 58)
    }

    "should convert correctly when time left is an exact amount of minutes only" in {
      LockoutExpiry(
        lockoutCreated = Instant.parse("2025-11-11T10:15:30.00Z"),
        lockoutExpiry = timeoutTtl,
        currentTime = Instant.parse("2025-11-11T10:15:30.00Z")
      ) mustBe LockoutExpiry(minutes = 2, seconds = 0)
    }

    "should convert correctly when time left is an exact amount of seconds only" in {
      LockoutExpiry(
        lockoutCreated = Instant.parse("2025-11-11T10:15:30.00Z"),
        lockoutExpiry = timeoutTtl,
        currentTime = Instant.parse("2025-11-11T10:16:31.00Z")
      ) mustBe LockoutExpiry(minutes = 0, seconds = 59)
    }

    "should convert correctly when time left is zero" in {
      LockoutExpiry(
        lockoutCreated = Instant.parse("2025-11-11T10:15:30.00Z"),
        lockoutExpiry = timeoutTtl,
        currentTime = Instant.parse("2025-11-11T10:17:30.00Z")
      ) mustBe LockoutExpiry(minutes = 0, seconds = 0)
    }

    "should convert correctly when lockout expiry is in the past" in {
      LockoutExpiry(
        lockoutCreated = Instant.parse("2025-11-11T10:15:30.00Z"),
        lockoutExpiry = timeoutTtl,
        currentTime = Instant.parse("2025-11-11T10:18:30.00Z")
      ) mustBe LockoutExpiry(minutes = 0, seconds = 0)
    }
  }

  "isExpired" - {
    "should return false when more than 1 second remaining" in {
      LockoutExpiry(minutes = 0, seconds = 1).isExpired mustBe false
    }

    "should return true when 0 seconds remaining" in {
      LockoutExpiry(minutes = 0, seconds = 0).isExpired mustBe true
    }

    "should return true when negative time remaining" in {
      LockoutExpiry(minutes = -1, seconds = -20).isExpired mustBe true
    }
  }

  "toTimeString" - {
    "must return the expected string for a one digit amount of seconds" in {
      LockoutExpiry(minutes = 12, seconds = 1).toTimeString mustBe "12:01"
    }

    "must return the expected string for a one digit amount of minutes" in {
      LockoutExpiry(minutes = 2, seconds = 1).toTimeString mustBe "02:01"
    }

    "must return the expected string for a two digit amount of minutes and seconds" in {
      LockoutExpiry(minutes = 12, seconds = 12).toTimeString mustBe "12:12"
    }
  }
}
