package com.pkon.service.dnetwork.persistence

import com.pkon.service.dnetwork.DNetworkModel
import com.pkon.service.dnetwork.DNetworkModel._
import com.pkon.service.errors.DatabaseError
import com.pkon.service.errors.ServiceError.InvalidData

import scala.util.{Failure, Success, Try}

object Helpers {

  def groupRowsIntoDependencyNetwork(dependencyNetwork: List[DependencyNetworkCypherRS]): DependencyNetwork = {
    val tasks: List[Task] = dependencyNetwork
      .filter(task => task.tId.nonEmpty && task.taskName.nonEmpty && task.taskDepartment.nonEmpty &&
        task.taskEarliestStart.nonEmpty && task.taskDuration.nonEmpty && task.taskLatestEnd.nonEmpty)
      .groupBy(_.tId)
      .map(dNet => {
        val task = dNet._2.head
        val taskInfoList: List[TaskInfo] = dNet._2.filter(ti => ti.depId.nonEmpty && ti.depName.nonEmpty).map { ti =>
          TaskInfo(ti.depId.get, ti.depName.get)
        }.sortBy(_.id)
        Task(task.tId.get, task.taskName.get, task.taskDepartment.get, task.taskEarliestStart.get,
          task.taskDuration.get, task.taskLatestEnd.get, taskInfoList)
      }).toList.sortBy(_.id)
    DependencyNetwork(dependencyNetwork.head.dId, dependencyNetwork.head.dnName, tasks)
  }

  def groupRowsIntoTaskList(taskRows: List[TaskCypherRS]): List[Task] = {
    val tasks: List[Task] = taskRows
      .groupBy(_.tId)
      .map(dNet => {
        val task = dNet._2.head
        val taskInfoList: List[TaskInfo] = dNet._2.filter(ti => ti.depId.nonEmpty && ti.depName.nonEmpty).map { ti =>
          TaskInfo(ti.depId.get, ti.depName.get)
        }.sortBy(_.id)
        Task(task.tId, task.taskName, task.taskDepartment, task.taskEarliestStart,
          task.taskDuration, task.taskLatestEnd, taskInfoList)
      }).toList.sortBy(_.id)
    tasks
  }

  def groupRowsIntoDependencyNetworks(dependencyNetworks: List[DependencyNetworkCypherRS]): List[DependencyNetwork] = {
    dependencyNetworks
      .groupBy(_.dId).map { dn =>
      val dNetwork = dn._2.head
      val dNet = dn._2.filter(task => task.tId.nonEmpty && task.taskName.nonEmpty && task.taskDepartment.nonEmpty &&
        task.taskEarliestStart.nonEmpty && task.taskDuration.nonEmpty && task.taskLatestEnd.nonEmpty)
        .groupBy(_.tId.get)
      val tasks: List[Task] = dNet.map(subtes => {
        val task = subtes._2.head
        val taskInfoList: List[TaskInfo] = subtes._2
          .filter(ti => ti.depId.nonEmpty && ti.depName.nonEmpty)
          .map {
            ti =>
              TaskInfo(ti.depId.get, ti.depName.get)
          }.sortBy(_.id)
        Task(task.tId.get, task.taskName.get, task.taskDepartment.get, task.taskEarliestStart.get,
          task.taskDuration.get, task.taskLatestEnd.get, taskInfoList)
      }).toList.sortBy(_.id)
      DependencyNetwork(dNetwork.dId, dNetwork.dnName, tasks)
    }.toList.sortBy(_.id)
  }

  def groupRowsInfoTaskToTask(taskRows: List[TaskCypherRS]): Task = {
    val task: TaskCypherRS = taskRows.head
    val taskInfoList: List[TaskInfo] =
      taskRows.filter(ti => ti.depId.nonEmpty && ti.depName.nonEmpty)
        .map(ti => {
          TaskInfo(ti.depId.get, ti.depName.get)
        }).sortBy(_.id)
    Task(task.tId, task.taskName, task.taskDepartment, task.taskEarliestStart, task.taskDuration, task.taskLatestEnd, taskInfoList)
  }

  def cleanAndFillIdsForDependencyNetwork(dependencyNetworks: List[DependencyNetworkDto]): Either[DatabaseError, List[DependencyNetwork]] = {
    Try {
      dependencyNetworks.map { dependencyNetworkDto =>
        val updatedTasks: Map[String, Task] = dependencyNetworkDto.tasks.map(task => (task.name, DNetworkModel.taskDtoToTask(task))).toMap
        val mapOfTasksWithId: List[Task] = updatedTasks.map { task =>
          val dependenciesWithId: List[TaskInfo] = task._2.dependsOn.map(dep => dep.copy(id = updatedTasks.find(_._1 == dep.name).get._2.id))
          task._2.copy(dependsOn = dependenciesWithId)
        }.toList
        DNetworkModel.dependencyNetworkDtoToDependencyNetwork(dependencyNetworkDto, mapOfTasksWithId)
      }
    } match {
      case Failure(_) => Left(InvalidData)
      case Success(value) => Right(value)
    }
  }


}
