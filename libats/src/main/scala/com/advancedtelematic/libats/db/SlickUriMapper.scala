package com.advancedtelematic.libats.db

import akka.http.scaladsl.model.Uri
import slick.driver.MySQLDriver.api._

object SlickUriMapper {
  implicit val uriMapper = MappedColumnType.base[Uri, String](
    _.toString,
    Uri.apply
  )
}


