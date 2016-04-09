package org.flywaydb.play

import javax.inject.{ Inject, Singleton }

import org.flywaydb.core.Flyway
import org.flywaydb.core.internal.util.jdbc.DriverDataSource
import play.api.{ Configuration, Environment, Logger }
import scala.collection.JavaConverters._

@Singleton
class Flyways @Inject() (configuration: Configuration, environment: Environment) {

  private val logger = Logger(classOf[Flyways])

  private val flywayConfigurations = {
    val configReader = new ConfigReader(configuration, environment)
    configReader.getFlywayConfigurations
  }

  def get(name: String): Option[Flyway] = all.get(name)

  def keys: Iterable[String] = all.keys

  val all: Map[String, Flyway] = {
    for {
      (dbName, configuration) <- flywayConfigurations
      migrationFilesLocation = s"db/migration/${dbName}"
      if migrationFileDirectoryExists(migrationFilesLocation)
    } yield {
      val flyway = new Flyway
      val database = configuration.database
      flyway.setDataSource(new DriverDataSource(getClass.getClassLoader, database.driver, database.url, database.user, database.password))
      if (!configuration.locations.isEmpty) {
        val locations = configuration.locations.map(location => s"${migrationFilesLocation}/${location}")
        flyway.setLocations(locations: _*)
      } else {
        flyway.setLocations(migrationFilesLocation)
      }
      flyway.setValidateOnMigrate(configuration.validateOnMigrate)
      flyway.setEncoding(configuration.encoding)
      flyway.setOutOfOrder(configuration.outOfOrder)
      if (configuration.initOnMigrate) {
        flyway.setBaselineOnMigrate(true)
      }
      for (prefix <- configuration.placeholderPrefix) {
        flyway.setPlaceholderPrefix(prefix)
      }
      for (suffix <- configuration.placeholderSuffix) {
        flyway.setPlaceholderSuffix(suffix)
      }
      flyway.setSchemas(configuration.schemas: _*)
      flyway.setPlaceholders(configuration.placeholders.asJava)
      configuration.sqlMigrationPrefix.foreach { sqlMigrationPrefix =>
        flyway.setSqlMigrationPrefix(sqlMigrationPrefix)
      }

      dbName -> flyway
    }
  }

  private def migrationFileDirectoryExists(path: String): Boolean = {
    environment.resource(path) match {
      case Some(r) => {
        logger.debug(s"Directory for migration files found. ${path}")
        true
      }
      case None => {
        logger.warn(s"Directory for migration files not found. ${path}")
        false
      }
    }
  }

}
