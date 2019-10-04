/**
* Copyright: Copyright (C) 2016, ATS Advanced Telematic Systems GmbH
* License: MPL-2.0
*/
package com.advancedtelematic.libats.test

import java.util.TimeZone

import com.typesafe.config.{Config, ConfigFactory}
import org.flywaydb.core.Flyway
import org.scalatest.{BeforeAndAfterAll, Suite}
import slick.jdbc.MySQLProfile.api._

import scala.collection.JavaConverters._

trait DatabaseSpec extends BeforeAndAfterAll {
  self: Suite =>

  TimeZone.setDefault(TimeZone.getTimeZone("UTC"))

  implicit lazy val db = Database.forConfig("", slickDbConfig)

  protected lazy val schemaName = {
    val catalog = testDbConfig.getString("catalog")
    val className = this.getClass.getSimpleName
    val cleanSchemaName = catalog.split(",").head
    cleanSchemaName + "_" + className
  }

  private lazy val config = ConfigFactory.load()

  private lazy val testDbConfig: Config = config.getConfig("database")

  private [libats] lazy val slickDbConfig: Config = {
    val withSchemaName =
      ConfigFactory.parseMap(Map("catalog" -> schemaName.toLowerCase).asJava)
    withSchemaName.withFallback(testDbConfig)
  }

  protected [libats] def cleanDatabase(): Unit = {
    flyway.clean()
  }

  private lazy val flyway = {
    val url = slickDbConfig.getString("url")
    val user = slickDbConfig.getConfig("properties").getString("user")
    val password = slickDbConfig.getConfig("properties").getString("password")

    val schemaName = slickDbConfig.getString("catalog")

    Flyway.configure()
      .dataSource(url, user, password)
      .schemas(schemaName)
      .locations("classpath:db.migration")
      .load()
  }

  private def resetDatabase() = {
    flyway.clean()
    flyway.migrate()
  }

  override def beforeAll() {
    resetDatabase()
    super.beforeAll()
  }

  override def afterAll() {
    db.close()
    super.afterAll()
  }
}
