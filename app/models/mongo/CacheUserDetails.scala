package models.mongo

import models.requests.{UserDetails, UserType}
import play.api.libs.json.{Format, Json, OWrites}

import java.time.Instant

case class CacheUserDetails(psrUserType: UserType,
                            psrUserId: String,
                            internalId: Option[String],
                            createdAt: Option[Instant])

object CacheUserDetails {
  def apply(userDetails: UserDetails,
            withInternalId: Boolean,
            createdAt: Option[Instant] = None): CacheUserDetails = CacheUserDetails(
    psrUserType = userDetails.psrUserType,
    psrUserId = userDetails.psrUserId,
    internalId = if (withInternalId) Some(userDetails.userId) else None,
    createdAt = createdAt
  )

  implicit val mongoFormat: Format[CacheUserDetails] = Json.format[CacheUserDetails]
  implicit val objWrites: OWrites[CacheUserDetails] = Json.writes[CacheUserDetails]
}
