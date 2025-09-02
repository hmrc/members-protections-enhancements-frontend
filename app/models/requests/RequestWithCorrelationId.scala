package models.requests

import models.CorrelationId
import play.api.mvc.{Request, WrappedRequest}

case class RequestWithCorrelationId[A](request: Request[A], correlationId: CorrelationId)
  extends WrappedRequest[A](request)