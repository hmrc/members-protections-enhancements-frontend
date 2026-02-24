/*
 * Copyright 2023 HM Revenue & Customs
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

package models.audit

import models.requests.{PensionSchemeMemberRequest, UserDetails}
import play.api.libs.json.{Json, OWrites}

case class AuditDetail(
  journey: String,
  firstName: String,
  lastName: String,
  dateOfBirth: String,
  nino: String,
  pensionSchemeMemberCheckReference: String,
  searchAPIMatchResult: Option[String],
  retrieveAPIFailureReason: Option[String],
  searchAPIFailureReason: Option[String],
  numberOfProtectionsAndEnhancementsTotal: Option[Int],
  numberOfProtectionsAndEnhancementsActive: Option[Int],
  numberOfProtectionsAndEnhancementsDormant: Option[Int],
  numberOfProtectionsAndEnhancementsWithdrawn: Option[Int],
  roleLoggedInAs: String,
  affinityGroup: String,
  requesterIdentifier: String,
  correlationId: String
)

object AuditDetail {
  implicit val writes: OWrites[AuditDetail] = Json.writes[AuditDetail]

  def apply(
    journey: String,
    request: PensionSchemeMemberRequest,
    searchAPIMatchResult: Option[String] = None,
    retrieveAPIFailureReason: Option[String] = None,
    searchAPIFailureReason: Option[String] = None,
    userDetails: UserDetails
  )(implicit correlationId: String): AuditDetail =
    AuditDetail(
      journey = journey,
      firstName = request.firstName,
      lastName = request.lastName,
      dateOfBirth = request.dateOfBirth,
      nino = request.nino,
      pensionSchemeMemberCheckReference = request.psaCheckRef,
      searchAPIMatchResult = searchAPIMatchResult,
      retrieveAPIFailureReason = retrieveAPIFailureReason,
      searchAPIFailureReason = searchAPIFailureReason,
      roleLoggedInAs = userDetails.psrUserType.toString,
      affinityGroup = userDetails.affinityGroup.toString,
      requesterIdentifier = userDetails.psrUserId,
      numberOfProtectionsAndEnhancementsActive = None,
      numberOfProtectionsAndEnhancementsTotal = None,
      numberOfProtectionsAndEnhancementsDormant = None,
      numberOfProtectionsAndEnhancementsWithdrawn = None,
      correlationId = correlationId
    )

}
