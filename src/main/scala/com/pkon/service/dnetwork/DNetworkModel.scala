package com.pkon.service.dnetwork

import java.util.UUID

import org.neo4j.driver.v1.Record

import scala.language.implicitConversions

object DNetworkModel {

  case class RequestData[A](data: A)

  case class Aircraft(id: Int, name: String)

  case class DependencyNetworkDtoRS(id: String, name: String, numberOfTasks: Int, tasks: List[Task])

  case class DependencyNetwork(id: String, name: String, tasks: List[Task] = List())

  case class DependencyNetworkDto(id: Option[String] = None, name: String, aircraft: Option[Aircraft] = None, tasks: List[TaskDto] = List())

  case class DependencyNetworkUpdate(name: String, aircraft: Option[Aircraft] = None)

  case class Task(
                   id: String, name: String, department: String,
                   earliestStart: Double, duration: Double, latestEnd: Double,
                   dependsOn: List[TaskInfo]
                 )

  case class TaskDto(id: Option[String] = None, name: String, department: String,
                     earliestStart: Double, duration: Double, latestEnd: Double,
                     dependsOn: List[TaskInfoDto])

  case class TaskUpdate(name: String, department: String,
                        earliestStart: Double, duration: Double, latestEnd: Double)

  def compareTasksWithoutUUID(task1: TaskDto, task2: TaskDto): Boolean = {
    task1.name == task2.name &&
      task1.department == task2.department &&
      task1.earliestStart == task2.earliestStart &&
      task1.duration == task2.duration &&
      task1.latestEnd == task2.latestEnd &&
      task1.dependsOn.size == task2.dependsOn.size &&
      task1.dependsOn.sortBy(_.name).zip(task2.dependsOn.sortBy(_.name))
        .forall(
          tuple => tuple._1.name == tuple._2.name
        )
  }

  case class TaskInfo(id: String, name: String)

  case class TaskInfoDto(id: Option[String] = None, name: String)

  case class RelationDto(dependsOn: List[String], dependentOn: List[String])

  def dependencyNetworkToDependencyNetworkDto(dependencyNetwork: DependencyNetwork): DependencyNetworkDto = {
    DependencyNetworkDto(Some(dependencyNetwork.id), dependencyNetwork.name, None, dependencyNetwork.tasks.map(taskToTaskDto))
  }

  def taskDtoToTask(taskDto: TaskDto): Task = Task(UUID.randomUUID().toString, taskDto.name, taskDto.department, taskDto.earliestStart, taskDto.duration, taskDto.latestEnd,
    taskDto.dependsOn.map(taskInfoDtoToTaskInfo))

  def dependencyNetworkDtoToDependencyNetwork(dependencyNetworkDto: DependencyNetworkDto, tasks: List[Task]): DependencyNetwork =
    DependencyNetwork(UUID.randomUUID().toString, dependencyNetworkDto.name, tasks)

  def taskInfoToTaskInfoDto(taskInfo: TaskInfo): TaskInfoDto = TaskInfoDto(Some(taskInfo.id), taskInfo.name)

  def taskInfoDtoToTaskInfo(taskInfoDto: TaskInfoDto): TaskInfo = TaskInfo(taskInfoDto.id.getOrElse(UUID.randomUUID().toString), taskInfoDto.name)

  def taskToTaskDto(task: Task): TaskDto = TaskDto(Some(task.id), task.name, task.department, task.earliestStart, task.duration, task.latestEnd, task.dependsOn.map(taskInfoToTaskInfoDto))

  def fromTaskDto(taskDto: TaskDto) = Task(taskDto.id.get, taskDto.name, taskDto.department, taskDto.earliestStart, taskDto.duration, taskDto.latestEnd, taskDto.dependsOn.map(taskInfoDtoToTaskInfo))

  case class DependencyNetworkCypherRS(dId: String, dnName: String, tId: Option[String], taskName: Option[String], taskDuration: Option[Double],
                                       taskLatestEnd: Option[Double], taskDepartment: Option[String], taskEarliestStart: Option[Double],
                                       depId: Option[String] = None, depName: Option[String] = None)

  case class TaskCypherRS(tId: String, taskName: String, taskDuration: Double,
                          taskLatestEnd: Double, taskDepartment: String, taskEarliestStart: Double,
                          depId: Option[String] = None, depName: Option[String] = None)

  def recordToCypherResponse(record: Record) = {

  }

}


