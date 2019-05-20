package com.pkon.service

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.stream.Materializer
import com.pkon.config.Configuration
import com.pkon.service.auth.{AuthService, AuthServiceDefault}
import com.pkon.service.dnetwork.persistence.DNetworkPersistenceGraph
import com.pkon.service.dnetwork.{DNetworkService, DNetworkServiceDefault}
import com.pkon.service.user.persistence.UserPersistenceSQL
import com.pkon.service.user.{UserService, UserServiceDefault}
import com.pkon.utils.database.DBAccess
import com.typesafe.config.{Config, ConfigFactory}
import org.neo4j.driver.v1.{AuthTokens, Driver, GraphDatabase, Config => Neo4JConfig}

case class Dependencies(dNetworkService: DNetworkService,
                        userService: UserService,
                        authService: AuthService)

object Dependencies {

  private val config: Config = ConfigFactory.load()

  def fromConfig(configuration: Configuration)(implicit system: ActorSystem, mat: Materializer): Dependencies = {
    val dbAccess = DBAccess(system)

    val neo4jConfig: Neo4JConfig = Neo4JConfig.build()
      .withEncryption()
      .withConnectionTimeout(10, TimeUnit.SECONDS)
      .withMaxConnectionLifetime(30, TimeUnit.MINUTES)
      .withMaxConnectionPoolSize(10)
      .withConnectionAcquisitionTimeout(20, TimeUnit.SECONDS)
      .toConfig

    val driver: Driver = {
      GraphDatabase.driver(
        config.getString("neo4j.url"),
        AuthTokens.basic(config.getString("neo4j.username"), config.getString("neo4j.password")),
        neo4jConfig)
    }

    val userPersistence = new UserPersistenceSQL(dbAccess)

    val dNetworkPersistence = new DNetworkPersistenceGraph(driver)

    val userService = new UserServiceDefault(userPersistence)
    val authService = new AuthServiceDefault(userPersistence)
    val dNetworkService: DNetworkService = new DNetworkServiceDefault(dNetworkPersistence)

    Dependencies(dNetworkService, userService, authService)
  }
}
