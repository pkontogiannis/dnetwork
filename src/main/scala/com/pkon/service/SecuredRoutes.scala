package com.pkon.service


import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directive1
import akka.http.scaladsl.server.Directives.{complete, optionalHeaderValueByName, _}
import com.pkon.service.errors.ServiceError.AuthenticationError
import com.pkon.service.errors.{ErrorMapper, UnauthorizedErrorHttp}
import com.pkon.utils.jwt.JWTUtils
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import io.circe.syntax._

trait SecuredRoutes extends FailFastCirceSupport {

  def authorized(roles: List[String]): Directive1[Map[String, Any]] = {
    optionalHeaderValueByName("Authorization").flatMap {
      case Some(jwt) =>
        JWTUtils.validateToken(jwt) match {
          case Left(error) =>
            complete(StatusCodes.Unauthorized, ErrorMapper.toHttpError(error))
          case Right(_) =>
            JWTUtils.decodeToken(jwt) match {
              case Left(error) =>
                complete(StatusCodes.Unauthorized, ErrorMapper.toHttpError(error))
              case Right(claims) =>
                if (roles.contains(claims.issuer))
                  provide(Map("userId" -> claims.subject, "role" -> claims.issuer))
                else
                  complete(StatusCodes.Unauthorized, ErrorMapper.toHttpError(AuthenticationError()))
            }
        }
      case None => complete(StatusCodes.Unauthorized, ErrorMapper.toHttpError(AuthenticationError()))
    }
  }
}


