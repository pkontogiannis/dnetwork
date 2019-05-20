package com.pkon.service


import akka.http.scaladsl.server.{Directives, Route}
import com.pkon.config.Version
import com.pkon.service.auth.AuthRoutes
import com.pkon.service.dnetwork.DNetworkRoutes
import com.pkon.service.errors.{ErrorMapper, HttpError, InternalErrorHttp, ServiceError}
import com.pkon.service.user.UserRoutes
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport

trait Routes extends Version
  with Directives with FailFastCirceSupport

object Routes extends Directives {

  def buildRoutes(dependencies: Dependencies): Route =
    new DNetworkRoutes(dependencies.dNetworkService).dNetworkRoutes ~
      new UserRoutes(dependencies.userService).userRoutes ~
      new AuthRoutes(dependencies.authService).authRoutes

  def buildErrorMapper(serviceErrorMapper: PartialFunction[ServiceError, HttpError]): ErrorMapper[ServiceError, HttpError] =
    (e: ServiceError) =>
      serviceErrorMapper
        .applyOrElse(e, (_: ServiceError) => InternalErrorHttp("Unexpected error"))

}
