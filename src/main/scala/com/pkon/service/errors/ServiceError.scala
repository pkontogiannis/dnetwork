package com.pkon.service.errors

import com.pkon.service.errors.ServiceError.{GenericDatabaseError, RecordNotFound}

sealed trait ServiceError

trait DatabaseError extends ServiceError

object DatabaseError {
  implicit val ToHttpErrorMapper: ErrorMapper[DatabaseError, HttpError] = {
    case GenericDatabaseError =>
      InternalErrorHttp("Unexpected error")
    case RecordNotFound =>
      DefaultNotFoundErrorHttp
  }
}

object ServiceError {

  case object GenericDatabaseError extends DatabaseError

  case object RecordNotFound extends DatabaseError

  case object RecordAlreadyExists extends DatabaseError

  case object InvalidData extends DatabaseError

  case class NotAvailableData() extends ServiceError

  case class AuthenticationError() extends ServiceError

  case class InsertModeIsNotDefined(view: String) extends ServiceError

  case class ClientServiceError(message: String) extends ServiceError

  val httpErrorMapper: PartialFunction[ServiceError, HttpError] = {
    case NotAvailableData() =>
      new MappingNotFoundErrorHttp {
        override val code: String = "dataNotFound"
        override val message: String = s"There is no available data"
      }
    case ClientServiceError(msg) =>
      new ClientServiceErrorHttp {
        override val code: String = "mappingNotFound"
        override val message: String = s"$msg"
      }
    case RecordAlreadyExists => new RecordAlreadyExists()
    case RecordNotFound => DefaultNotFoundErrorHttp
    case AuthenticationError() => UnauthorizedErrorHttp()
    case GenericDatabaseError => InternalErrorHttp("Unexpected error")
    case InsertModeIsNotDefined(mode) => BadRequestErrorHttp(s"Unable to insert tasks with mode, $mode")
    case InvalidData =>  BadRequestErrorHttp(s"The inserted data are not valid")
  }
}
