package com.pkon

import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Route
import com.pkon.config.{Configuration, Server}
import com.pkon.service.{Dependencies, Routes}
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.Future
import scala.util.{Failure, Success}

object Main extends App with Server with LazyLogging {

  def startApplication(): Unit = {
    val configuration: Configuration = Configuration.default

    val dependencies: Dependencies = Dependencies.fromConfig(configuration)

    val routes: Route = Routes.buildRoutes(dependencies)

    val serverBinding: Future[Http.ServerBinding] = Http().bindAndHandle(routes, configuration.serverConfig.host, configuration.serverConfig.port)

    serverBinding.onComplete {
      case Success(bound) =>
        logger.info(s"com.klm.config.Server online at http://${bound.localAddress.getHostString}:${bound.localAddress.getPort}/")
      case Failure(e) =>
        logger.error(s"com.klm.config.Server could not start!")
        e.printStackTrace()
        system.terminate()
    }
  }

  startApplication()

}
