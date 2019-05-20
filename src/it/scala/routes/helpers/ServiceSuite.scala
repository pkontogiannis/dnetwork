package routes.helpers

import java.util.concurrent.TimeUnit

import akka.actor.ActorSystem
import akka.http.scaladsl.testkit.ScalatestRouteTest
import com.pkon.service.Routes
import com.pkon.service.dnetwork.persistence.DNetworkPersistenceGraph
import com.typesafe.config.{Config, ConfigFactory}
import org.neo4j.driver.v1.{AuthTokens, Driver, GraphDatabase, Config => Neo4JConfig}
import org.scalatest.{BeforeAndAfterAll, BeforeAndAfterEach, Matchers, WordSpec}

import scala.concurrent.Await
import scala.concurrent.duration._
import scala.util.matching.Regex

abstract class ServiceSuite extends WordSpec with Matchers with ScalatestRouteTest
  with Routes with BeforeAndAfterAll with BeforeAndAfterEach {

  implicit val actorSystem: ActorSystem =
    ActorSystem("test-actor-system")

  val config: Config = ConfigFactory.load()

  val neo4jConfig: Neo4JConfig = Neo4JConfig.build()
    .withEncryption()
    .withConnectionTimeout(10, TimeUnit.SECONDS)
    .withMaxConnectionLifetime(30, TimeUnit.MINUTES)
    .withMaxConnectionPoolSize(10)
    .withConnectionAcquisitionTimeout(20, TimeUnit.SECONDS)
    .toConfig

  val driver: Driver = {
    GraphDatabase.driver(
      config.getString("neo4j-test.url"),
      AuthTokens.basic(config.getString("neo4j-test.username"), config.getString("neo4j-test.password")),
      neo4jConfig)
  }

  val dNetworkPersistence = new DNetworkPersistenceGraph(driver)

  override def beforeAll(): Unit = {
    Await.result(dNetworkPersistence.deleteDatabaseContent, 10.seconds)
  }

  override def afterAll(): Unit = {
    driver.close()
    Await.result(actorSystem.terminate(), 10.seconds)
  }

  private def unsafeExtractHostPort(string: String): (String, Int) = {
    val HostnameRegEx: Regex = "(.+):(\\d{4,5})".r
    string match {
      case HostnameRegEx(host, port) => (host, port.toInt)
      case _ => throw new Exception(s"Failed to parse hostname: $string")
    }
  }

}
