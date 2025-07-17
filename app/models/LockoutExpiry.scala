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

import java.time.Instant
import scala.math.Integral.Implicits.infixIntegralOps

case class LockoutExpiry(minutes: Int, seconds: Int) {
  val isExpired: Boolean = if(minutes <= 0 && seconds <= 0) true else false
  val toTimeString: String = f"$minutes%02d" + ":" + f"$seconds%02d"
}

object LockoutExpiry {
  def apply(lockoutCreated: Instant,
            lockoutExpiry: Long,
            currentTime: Instant): LockoutExpiry = {
    val expiryTime = lockoutCreated.plusSeconds(lockoutExpiry)
    val secondsLeft = expiryTime.getEpochSecond - currentTime.getEpochSecond
    val (mins: Long, secs: Long) = secondsLeft /% 60

    val rawExpiry = LockoutExpiry(mins.toInt, secs.toInt)
    if (!rawExpiry.isExpired) rawExpiry else LockoutExpiry(minutes = 0, seconds = 0)
  }
}
