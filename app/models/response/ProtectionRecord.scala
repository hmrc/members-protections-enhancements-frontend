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

package models.response

import play.api.libs.functional.syntax._
import play.api.libs.json.{JsPath, Reads}

case class ProtectionRecord(protectionReference: Option[String],
                            `type`: RecordTypeMapped,
                            status: RecordStatusMapped,
                            protectedAmount: Option[Int],
                            lumpSumAmount: Option[Int],
                            lumpSumPercentage: Option[Int],
                            enhancementFactor: Option[Double]) {

  lazy val lumpSumPercentHtmlStringOpt: Option[String] = lumpSumPercentage.map(_.toString + "%")
  lazy val enhancementFactorHtmlStringOpt: Option[String] = enhancementFactor.map(_.toString)
}

object ProtectionRecord {
  implicit val reads: Reads[ProtectionRecord] = (
    (JsPath \ "protectionReference").readNullable[String] and
      (JsPath \ "type").read[RecordType].map(_.toMapped) and
      (JsPath \ "status").read[RecordStatus].map(_.toMapped) and
      (JsPath \ "protectedAmount").readNullable[Int] and
      (JsPath \ "lumpSumAmount").readNullable[Int] and
      (JsPath \ "lumpSumPercentage").readNullable[Int] and
      (JsPath \ "enhancementFactor").readNullable[Double]
    )(ProtectionRecord.apply _)
}
