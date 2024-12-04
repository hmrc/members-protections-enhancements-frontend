package models.cache

import play.api.libs.json.{JsError, JsString, JsSuccess, Json, Reads}
import utils.WithName

case class SessionData(administratorOrPractitioner: PensionSchemeUser)

object SessionData {
  implicit val reads: Reads[SessionData] = Json.reads[SessionData]
}

sealed trait PensionSchemeUser

object PensionSchemeUser {
  case object Adminstrator extends WithName("administrator") with PensionSchemeUser
  case object Practitioner extends WithName("practitioner") with PensionSchemeUser

  implicit val reads: Reads[PensionSchemeUser] = {
    case JsString(Adminstrator.name) => JsSuccess(Adminstrator)
    case JsString(Practitioner.name) => JsSuccess(Practitioner)
    case _ => JsError("Unknown value")
  }
}