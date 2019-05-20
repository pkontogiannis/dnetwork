package com.pkon.service.dnetwork


import com.pkon.service.dnetwork.DNetworkModel.{DependencyNetworkDto, DependencyNetworkUpdate, RelationDto, TaskDto, TaskUpdate}
import com.pkon.service.dnetwork.persistence.DNetworkPersistence
import com.pkon.service.errors.DatabaseError
import com.pkon.service.errors.ServiceError.GenericDatabaseError
import com.typesafe.scalalogging.LazyLogging

import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future

class DNetworkServiceDefault(val dNetworkPersistence: DNetworkPersistence) extends DNetworkService with LazyLogging {

  /* Dependency Management */

  def getDependencyNetworksNormal: Future[Either[DatabaseError, List[DependencyNetworkDto]]] = {
    dNetworkPersistence.getDependencyNetworksNormal.map {
      case Right(value) =>
        Right(value.map(dnet => DNetworkModel.dependencyNetworkToDependencyNetworkDto(dnet)))
      case Left(_) => Left(GenericDatabaseError)
    }
  }

  def getDependencyNetworksCompact: Future[Either[DatabaseError, List[DependencyNetworkDto]]] = {
    dNetworkPersistence.getDependencyNetworksCompact.map {
      case Right(value) =>
        Right(value.map(dnet => DNetworkModel.dependencyNetworkToDependencyNetworkDto(dnet)))
      case Left(_) => Left(GenericDatabaseError)
    }
  }

  def addDependencyNetwork(dependencyNetworkDto: DependencyNetworkDto): Future[Either[DatabaseError, DependencyNetworkDto]] = {
    dNetworkPersistence.addDependencyNetwork(dependencyNetworkDto).map {
      case Right(value) =>
        Right(DNetworkModel.dependencyNetworkToDependencyNetworkDto(value))
      case Left(error) =>
        Left(error)
    }
  }

  def addDependencyNetworks(dependencyNetworks: List[DependencyNetworkDto]): Future[Either[DatabaseError, List[DependencyNetworkDto]]] = {
    dNetworkPersistence.addDependencyNetworks(dependencyNetworks).map {
      case Right(value) =>
        Right(value.map(dnet => DNetworkModel.dependencyNetworkToDependencyNetworkDto(dnet)))
      case Left(error) =>
        Left(error)
    }
  }

  //  def addDependencyNetworkTasks(dnId: String, tasks: List[TaskDto]): Future[Either[DatabaseError, List[TaskDto]]] = {
  //
  //    val res = for {
  //      _ <- EitherT(dNetworkPersistence.getDependencyNetworkCompact(dnId))
  //      tasks <- EitherT(dNetworkPersistence.addTasksToDependencyNetwork(dnId, tasks))
  //      taskDtos = tasks.map(ts => DNetworkModel.taskToTaskDto(ts))
  //    } yield taskDtos
  //
  //    res.value
  //
  //    //    dNetworkPersistence.addTasksToDependencyNetwork(dnId, tasks).map {
  //    //      case Right(value) =>
  //    //        Right(value.map(dnet => DNetworkModel.taskToTaskDto(dnet)))
  //    //      case Left(_) => Left(GenericDatabaseError)
  //    //    }
  //  }
  //
  //  def addDependencyNetworkTasks(dnId: String, tasks: List[TaskDto]): Future[Either[DatabaseError, List[TaskDto]]] = {
  //
  //    val res: Future[List[Either[DatabaseError, TaskDto]]] = Future.sequence(tasks.map {
  //      task => addDependencyNetworkTask(dnId, task)
  //    })
  //
  //    res.map {
  //      re =>
  //        re collectFirst { case Left(f) => f } toLeft {
  //          re collect { case Right(r) => r }
  //        }
  //    }
  //    //    dNetworkPersistence.addDependencyNetworkTask(dnId, taskDto).map {
  //    //      case Right(value) =>
  //    //        Right(DNetworkModel.taskToTaskDto(value))
  //    //      case Left(error) =>
  //    //        Left(error)
  //    //    }
  //  }

  def getDependencyNetworkCompact(dnId: String): Future[Either[DatabaseError, DependencyNetworkDto]] = {
    dNetworkPersistence.getDependencyNetworkCompact(dnId).map {
      case Right(value) =>
        Right(DNetworkModel.dependencyNetworkToDependencyNetworkDto(value))
      case Left(error) =>
        Left(error)
    }
  }

  def getDependencyNetworkNormal(dnId: String): Future[Either[DatabaseError, DependencyNetworkDto]] = {
    dNetworkPersistence.getDependencyNetworkNormal(dnId).map {
      case Right(value) =>
        Right(DNetworkModel.dependencyNetworkToDependencyNetworkDto(value))
      case Left(error) => Left(error)
    }
  }

  def updateDependencyNetwork(dnId: String, dependencyNetworkUpdate: DependencyNetworkUpdate): Future[Either[DatabaseError, DependencyNetworkDto]] = {
    dNetworkPersistence.updateDependencyNetwork(dnId, dependencyNetworkUpdate).map {
      case Right(value) =>
        Right(DNetworkModel.dependencyNetworkToDependencyNetworkDto(value))
      case Left(error) => Left(error)
    }
  }

  def deleteDependencyNetwork(dnId: String): Future[Either[DatabaseError, Boolean]] = {
    dNetworkPersistence.deleteDependencyNetwork(dnId).map {
      case Right(value) =>
        Right(value)
      case Left(error) => Left(error)
    }
  }

  /* Task Management */

  def addTask(dnId: String, taskDto: TaskDto): Future[Either[DatabaseError, TaskDto]] = {
    dNetworkPersistence.addTask(dnId, taskDto).map {
      case Right(value) =>
        Right(DNetworkModel.taskToTaskDto(value))
      case Left(error) =>
        Left(error)
    }
  }

  def addTasks(dnId: String, tasks: List[TaskDto]): Future[Either[DatabaseError, List[TaskDto]]] = {
    dNetworkPersistence.addTasks(dnId, tasks).map {
      case Right(value) =>
        Right(value.map(task => DNetworkModel.taskToTaskDto(task)))
      case Left(error) =>
        Left(error)
    }
  }

  def getDependencyNetworkTasks(dnId: String): Future[Either[DatabaseError, List[TaskDto]]] = {
    dNetworkPersistence.getDependencyNetworkTasks(dnId).map {
      case Right(value) =>
        Right(value.map(task => DNetworkModel.taskToTaskDto(task)))
      case Left(_) => Left(GenericDatabaseError)
    }
  }

  def getDependencyNetworkTask(dnId: String, tId: String): Future[Either[DatabaseError, TaskDto]] = {
    dNetworkPersistence.getTask(dnId, tId).map {
      case Right(value) =>
        Right(DNetworkModel.taskToTaskDto(value))
      case Left(error) =>
        Left(error)
    }
  }

  def updateTask(dnId: String, tId: String, taskUpdate: TaskUpdate): Future[Either[DatabaseError, TaskDto]] = {
    dNetworkPersistence.updateTask(dnId, tId, taskUpdate).map {
      case Right(value) =>
        Right(DNetworkModel.taskToTaskDto(value))
      case Left(error) =>
        Left(error)
    }
  }

  def deleteTask(dnId: String, tId: String): Future[Either[DatabaseError, Boolean]] = {
    dNetworkPersistence.deleteTask(dnId, tId).map {
      case Right(value) =>
        Right(value)
      case Left(error) => Left(error)
    }
  }

  def getTaskPredecessors(dnId: String, tId: String): Future[Either[DatabaseError, List[TaskDto]]] = {
    dNetworkPersistence.getTaskPredecessors(dnId, tId).map {
      case Right(value) =>
        Right(value.map(task => DNetworkModel.taskToTaskDto(task)))
      case Left(_) => Left(GenericDatabaseError)
    }
  }

  def getTaskSuccessors(dnId: String, tId: String): Future[Either[DatabaseError, List[TaskDto]]] = {
    dNetworkPersistence.getTaskSuccessors(dnId, tId).map {
      case Right(value) =>
        Right(value.map(task => DNetworkModel.taskToTaskDto(task)))
      case Left(_) => Left(GenericDatabaseError)
    }
  }

  def addTaskToTaskRelation(dnId: String, tId1: String, relationDto: RelationDto): Future[Either[DatabaseError, TaskDto]] = {
    dNetworkPersistence.addTaskToTaskRelation(dnId, tId1, relationDto.copy(dependsOn = relationDto.dependsOn.distinct, dependentOn = relationDto.dependentOn.distinct)).
      map {
        case Right(value) =>
          Right(DNetworkModel.taskToTaskDto(value))
        case Left(error) =>
          Left(error)
      }
  }

  def deleteTaskToTaskRelation(dnId: String, tId1: String, relationDto: RelationDto): Future[Either[DatabaseError, TaskDto]] = {
    dNetworkPersistence.deleteTaskToTaskRelation(dnId, tId1, relationDto.copy(dependsOn = relationDto.dependsOn.distinct, dependentOn = relationDto.dependentOn.distinct)).
      map {
        case Right(value) =>
          Right(DNetworkModel.taskToTaskDto(value))
        case Left(error) =>
          Left(error)
      }
  }
}
