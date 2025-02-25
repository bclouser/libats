/**
 * Copyright: Copyright (C) 2016, ATS Advanced Telematic Systems GmbH
 * License: MPL-2.0
 */
package com.advancedtelematic.libats.http

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives.complete
import akka.http.scaladsl.server._
import com.advancedtelematic.libats.codecs.CirceCodecs._
import com.advancedtelematic.libats.codecs.{DeserializationException, RefinementError}
import com.advancedtelematic.libats.data.{ErrorCodes, ErrorRepresentation}
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.DecodingFailure
import io.circe.generic.auto._

/**
  * When validation, JSON deserialisation fail or a duplicate entry
  * occurs in the database, we complete the request by returning the
  * correct status code and JSON error message (see Errors.scala).
  */

object DefaultRejectionHandler {
  def rejectionHandler : RejectionHandler = RejectionHandler.newBuilder().handle {
    case ValidationRejection(msg, _) =>
      complete( StatusCodes.BadRequest -> ErrorRepresentation(ErrorCodes.InvalidEntity, msg) )
  }.handle{
    case MalformedRequestContentRejection(_, RefinementError(_, msg)) =>
      complete(StatusCodes.BadRequest -> ErrorRepresentation(ErrorCodes.InvalidEntity, msg))
  }.handle{
    case MalformedRequestContentRejection(_, DeserializationException(RefinementError(_, msg))) =>
      complete(StatusCodes.BadRequest -> ErrorRepresentation(ErrorCodes.InvalidEntity, msg))
  }.handle {
    case MalformedRequestContentRejection(_, df@DecodingFailure(_, _)) =>
      complete(StatusCodes.BadRequest -> ErrorRepresentation(ErrorCodes.InvalidEntity, df.getMessage))
  }.handle {
    case MalformedQueryParamRejection(name, _, _) ⇒
      complete(StatusCodes.BadRequest -> ErrorRepresentation(ErrorCodes.InvalidEntity, "The query parameter '" + name + "' was malformed"))
  }.result().withFallback(RejectionHandler.default)
}
