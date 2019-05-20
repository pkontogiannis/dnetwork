package com.pkon.service.dnetwork

import java.util.UUID

import akka.http.scaladsl.marshalling.ToEntityMarshaller
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Route
import com.pkon.service.dnetwork.DNetworkModel._
import com.pkon.service.dnetwork.EndpointMode.{CompactView, NormalView, TaskBatch, TaskSimple}
import com.pkon.service.errors.ServiceError.InsertModeIsNotDefined
import com.pkon.service.errors.{ErrorMapper, ErrorResponse, HttpError, ServiceError}
import com.pkon.service.{Routes, SecuredRoutes}
import io.circe.generic.auto._
import io.circe.syntax._

import scala.util.{Failure, Success}

class DNetworkRoutes(val dNetworkService: DNetworkService)
  extends Routes with SecuredRoutes {

  val authorizationList = List("admin", "developer")

  val dNetworkRoutes: Route =
    DNetwork.routes

  object DNetwork {

    def completeEither[E <: ServiceError, R: ToEntityMarshaller]
    (statusCode: StatusCode, either: => Either[E, R])(
      implicit mapper: ErrorMapper[E, HttpError]
    ): Route = {
      either match {
        case Left(value) =>
          complete(value.statusCode, ErrorResponse(code = value.code, message = value.message))
        case Right(value) => complete(statusCode, value)
      }
    }

    implicit val httpErrorMapper: ErrorMapper[ServiceError, HttpError] =
      Routes.buildErrorMapper(ServiceError.httpErrorMapper)

    def routes: Route = logRequestResult("DNetworkRoutes") {
      pathPrefix("api" / version)(
        dNetworksManagement
      )
    }

    def dNetworksManagement: Route = {
      pathPrefix("dependencynetworks") {
        dependencyNetworksActions ~
          getDependencyNetworks ~
          postUnderDependencies
      }
    }

    def getDependencyNetworks: Route = {
      pathEndOrSingleSlash {
        get {
          parameter('mode ? CompactView.mode) { mode =>
            EndpointMode.parse(mode) match {
              case Some(CompactView) =>
                onComplete(dNetworkService.getDependencyNetworksCompact) {
                  case Success(future) => completeEither(StatusCodes.OK, future)
                  case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
                }
              case Some(NormalView) =>
                onComplete(dNetworkService.getDependencyNetworksNormal) {
                  case Success(future) => completeEither(StatusCodes.OK, future)
                  case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
                }
              case _ =>
                val error = ServiceError.httpErrorMapper(InsertModeIsNotDefined(mode))
                complete(StatusCodes.BadRequest, ErrorResponse(code = error.code, message = error.message))
            }
          }
        }
      }
    }

    def postUnderDependencies: Route = {
      authorized(authorizationList) { _ =>
        parameter('mode ? TaskSimple.mode) {
          mode =>
            EndpointMode.parse(mode) match {
              case Some(TaskSimple) =>
                postDependencyNetwork
              case Some(TaskBatch) =>
                postDependencyNetworks
              case _ =>
                val error = ServiceError.httpErrorMapper(InsertModeIsNotDefined(mode))
                complete(StatusCodes.BadRequest, ErrorResponse(code = error.code, message = error.message))
            }
        }
      }
    }

    def postDependencyNetwork: Route = {
      pathEndOrSingleSlash {
        post {
          entity(as[RequestData[DependencyNetworkDto]]) { requestData =>
            onComplete(dNetworkService.addDependencyNetwork(requestData.data)) {
              case Success(future) => completeEither(StatusCodes.Created, future)
              case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
            }
          }
        }
      }
    }

    def postDependencyNetworks: Route = {
      pathEndOrSingleSlash {
        post {
          entity(as[RequestData[List[DependencyNetworkDto]]]) { requestData =>
            onComplete(dNetworkService.addDependencyNetworks(requestData.data)) {
              case Success(future) => completeEither(StatusCodes.Created, future)
              case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
            }
          }
        }
      }
    }

    def dependencyNetworksActions: Route = {
      pathPrefix(Segment) { id =>
        // TODO: invalid UUID send error message
        val dnId = UUID.fromString(id).toString
        dependencyNetworkTasks(dnId) ~
          getDependencyNetwork(dnId) ~
          putDependencyNetwork(dnId) ~
          deleteDependencyNetwork(dnId)
      }
    }

    def getDependencyNetwork(dnId: String): Route = {
      get(
        parameter('mode ? CompactView.mode) { mode =>
          EndpointMode.parse(mode) match {
            case Some(CompactView) =>
              onComplete(dNetworkService.getDependencyNetworkCompact(dnId)) {
                case Success(future) => completeEither(StatusCodes.OK, future)
                case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
              }
            case Some(NormalView) =>
              onComplete(dNetworkService.getDependencyNetworkNormal(dnId)) {
                case Success(future) => completeEither(StatusCodes.OK, future)
                case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
              }
            case _ =>
              val error = ServiceError.httpErrorMapper(InsertModeIsNotDefined(mode))
              complete(StatusCodes.BadRequest, ErrorResponse(code = error.code, message = error.message))
          }
        }
      )
    }

    // TODO: Should be PATCH in case of partial update
    def putDependencyNetwork(dnId: String): Route = {
      pathEndOrSingleSlash {
        authorized(authorizationList) { _ =>
          put {
            entity(as[RequestData[DependencyNetworkUpdate]]) { requestData =>
              onComplete(dNetworkService.updateDependencyNetwork(dnId, requestData.data)) {
                case Success(future) => completeEither(StatusCodes.OK, future)
                case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
              }
            }
          }
        }
      }
    }

    // TODO: Should add batch deletion
    def deleteDependencyNetwork(dnId: String): Route = {
      pathEndOrSingleSlash {
        authorized(authorizationList) { _ =>
          delete {
            onComplete(dNetworkService.deleteDependencyNetwork(dnId)) {
              case Success(future) => completeEither(StatusCodes.NoContent, future)
              case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
            }
          }
        }
      }
    }

    def dependencyNetworkTasks(dnId: String): Route = {
      pathPrefix("tasks") {
        dependencyNetworkTasksActions(dnId) ~
          getDependencyNetworkTasks(dnId) ~
          postUnderTasks(dnId)
      }
    }

    def postUnderTasks(dnId: String): Route = {
      pathEndOrSingleSlash {
        authorized(authorizationList) { _ =>
          parameter('mode ? TaskSimple.mode) {
            mode =>
              EndpointMode.parse(mode) match {
                case Some(TaskSimple) =>
                  postDependencyNetworkTask(dnId)
                case Some(TaskBatch) =>
                  postDependencyNetworkTasks(dnId)
                case _ =>
                  val error = ServiceError.httpErrorMapper(InsertModeIsNotDefined(mode))
                  complete(StatusCodes.BadRequest, ErrorResponse(code = error.code, message = error.message))
              }
          }
        }
      }
    }

    // TODO: A mode can be added here, if we need compact or normal view
    def getDependencyNetworkTasks(dnId: String): Route = {
      pathEndOrSingleSlash {
        get(
          onComplete(dNetworkService.getDependencyNetworkTasks(dnId)) {
            case Success(future) => completeEither(StatusCodes.OK, future)
            case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
          }
        )
      }
    }

    def postDependencyNetworkTask(dnId: String): Route = {
      post {
        entity(as[RequestData[TaskDto]]) { requestData =>
          onComplete(dNetworkService.addTask(dnId, requestData.data)) {
            case Success(future) => completeEither(StatusCodes.Created, future)
            case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
          }
        }
      }
    }

    def postDependencyNetworkTasks(dnId: String): Route = {
      post {
        entity(as[RequestData[List[TaskDto]]]) { requestData =>
          onComplete(dNetworkService.addTasks(dnId, requestData.data)) {
            case Success(future) => completeEither(StatusCodes.Created, future)
            case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
          }
        }
      }
    }

    def dependencyNetworkTasksActions(dnId: String): Route = {
      pathPrefix(Segment) { id =>
        val tId = UUID.fromString(id).toString
        actionsOnTaskRelations(dnId, tId) ~ getDependencyNetworkTask(dnId, tId) ~ putDependencyNetworkTask(dnId, tId) ~
          deleteDependencyNetworkTask(dnId, tId) ~
          getDependencyNetworkTaskPredecessors(dnId, tId) ~
          getDependencyNetworkTaskSuccessors(dnId, tId)
      }
    }

    // TODO: A mode can be added here, if we need compact or normal view
    def getDependencyNetworkTask(dnId: String, tId: String): Route = {
      pathEndOrSingleSlash {
        get(
          onComplete(dNetworkService.getDependencyNetworkTask(dnId, tId)) {
            case Success(future) => completeEither(StatusCodes.OK, future)
            case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
          }
        )
      }
    }

    def getDependencyNetworkTaskPredecessors(dnId: String, tId: String): Route = {
      pathPrefix("predecessors") {
        pathEndOrSingleSlash {
          get(
            onComplete(dNetworkService.getTaskPredecessors(dnId, tId)) {
              case Success(future) => completeEither(StatusCodes.OK, future)
              case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
            }
          )
        }
      }
    }

    def getDependencyNetworkTaskSuccessors(dnId: String, tId: String): Route = {
      pathPrefix("successors") {
        pathEndOrSingleSlash {
          get(
            onComplete(dNetworkService.getTaskSuccessors(dnId, tId)) {
              case Success(future) => completeEither(StatusCodes.OK, future)
              case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
            }
          )
        }
      }
    }

    def actionsOnTaskRelations(dnId: String, tId1: String): Route = {
      pathPrefix("relations") {
        postTaskToTaskRelation(dnId, tId1) ~ deleteTaskToTaskRelation(dnId, tId1)
      }
    }

    def postTaskToTaskRelation(dnId: String, tId1: String): Route = {
      pathEndOrSingleSlash {
        authorized(authorizationList) { _ =>
          post(
            entity(as[RequestData[RelationDto]]) { requestData =>
              onComplete(dNetworkService.addTaskToTaskRelation(dnId, tId1, requestData.data)) {
                case Success(future) => completeEither(StatusCodes.Created, future)
                case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
              }
            }
          )
        }
      }
    }

    def deleteTaskToTaskRelation(dnId: String, tId1: String): Route = {
      pathEndOrSingleSlash {
        authorized(authorizationList) { _ =>
          delete(
            entity(as[RequestData[RelationDto]]) { requestData =>
              onComplete(dNetworkService.deleteTaskToTaskRelation(dnId, tId1, requestData.data)) {
                case Success(future) => completeEither(StatusCodes.OK, future)
                case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
              }
            }
          )
        }
      }
    }

    // TODO: Should be PATCH in case of partial update
    def putDependencyNetworkTask(dnId: String, tId: String): Route = {
      pathEndOrSingleSlash {
        authorized(authorizationList) { _ =>
          put {
            entity(as[RequestData[TaskUpdate]]) { requestData =>
              onComplete(dNetworkService.updateTask(dnId, tId, requestData.data)) {
                case Success(future) => completeEither(StatusCodes.OK, future)
                case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
              }
            }
          }
        }
      }
    }

    // TODO: Should add batch deletion
    def deleteDependencyNetworkTask(dnId: String, tId: String): Route = {
      pathEndOrSingleSlash {
        authorized(authorizationList) { _ =>
          delete {
            onComplete(dNetworkService.deleteTask(dnId, tId)) {
              case Success(future) => completeEither(StatusCodes.NoContent, future)
              case Failure(ex) => complete((StatusCodes.InternalServerError, s"An error occurred: ${ex.getMessage}"))
            }
          }
        }
      }
    }

  }

}
