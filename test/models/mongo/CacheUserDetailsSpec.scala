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

package models.mongo

import base.SpecBase
import models.mongo.CacheUserDetails.mongoFormat
import models.requests.UserDetails
import models.requests.UserType.PSA
import play.api.libs.json._
import uk.gov.hmrc.auth.core.AffinityGroup

import java.time.Instant

class CacheUserDetailsSpec extends SpecBase {
  val instantLong: Long = 10000

  val mongoJson: JsValue = Json.parse(
    s"""
      |{
      | "psrUserType": "PSA",
      | "psrUserId": "anId",
      | "createdAt": {
      |   "$$date": {
      |     "$$numberLong": "$instantLong"
      |   }
      | }
      |}
    """.stripMargin
  )

  val testModel: CacheUserDetails = CacheUserDetails(
    psrUserType = PSA,
    psrUserId = Some("anId"),
    createdAt = Some(Instant.ofEpochMilli(instantLong))
  )

  "mongoReads" -> {
    "return a JsError when reading from invalid JSON" in {
      JsObject.empty.validate[CacheUserDetails](mongoFormat) mustBe a[JsError]
    }

    "return a JsSuccess when reading from valid JSON" in {
      val result = mongoJson.validate[CacheUserDetails](mongoFormat)
      result mustBe a[JsSuccess[_]]
      result.get mustBe testModel
    }
  }

  "mongoWrites" -> {
    "return the expected JSON" in {
      val result = Json.toJson(testModel)(mongoFormat)
      result mustBe mongoJson
    }
  }

  "apply" -> {
    "should not include PSR ID when flag is false" in {
      CacheUserDetails.apply(
        userDetails = UserDetails(PSA, "anId", "anotherId", AffinityGroup.Individual),
        withPsrUserId = false,
        createdAt = None
      ) mustBe CacheUserDetails(PSA, None, None)
    }

    "should include PSR ID when flag is true" in {
      CacheUserDetails.apply(
        userDetails = UserDetails(PSA, "anId", "anotherId", AffinityGroup.Individual),
        withPsrUserId = true,
        createdAt = Some(Instant.ofEpochMilli(instantLong))
      ) mustBe CacheUserDetails(PSA, Some("anId"), Some(Instant.ofEpochMilli(instantLong)))
    }
  }
}
