package com.pkon.service.dnetwork

import com.pkon.service.dnetwork.DNetworkModel.{DependencyNetworkDto, DependencyNetworkUpdate, RelationDto, TaskDto, TaskUpdate}
import com.pkon.service.errors.DatabaseError

import scala.concurrent.Future

trait DNetworkService {

  /* Dependency Management */

  def getDependencyNetworksNormal: Future[Either[DatabaseError, List[DependencyNetworkDto]]]

  def getDependencyNetworksCompact: Future[Either[DatabaseError, List[DependencyNetworkDto]]]

  def addDependencyNetwork(dependencyNetworkDto: DependencyNetworkDto): Future[Either[DatabaseError, DependencyNetworkDto]]

  def addDependencyNetworks(dependencyNetworks: List[DependencyNetworkDto]): Future[Either[DatabaseError, List[DependencyNetworkDto]]]

  def getDependencyNetworkCompact(dnId: String): Future[Either[DatabaseError, DependencyNetworkDto]]

  def getDependencyNetworkNormal(dnId: String): Future[Either[DatabaseError, DependencyNetworkDto]]

  def updateDependencyNetwork(dnId: String, dependencyNetworkUpdate: DependencyNetworkUpdate): Future[Either[DatabaseError, DependencyNetworkDto]]

  def deleteDependencyNetwork(dnId: String): Future[Either[DatabaseError, Boolean]]

  /* Task Management */

  def addTask(dnId: String, taskDto: TaskDto): Future[Either[DatabaseError, TaskDto]]

  def addTasks(dnId: String, tasks: List[TaskDto]): Future[Either[DatabaseError, List[TaskDto]]]

  def getDependencyNetworkTasks(dnId: String): Future[Either[DatabaseError, List[TaskDto]]]

  def getDependencyNetworkTask(dnId: String, tId: String): Future[Either[DatabaseError, TaskDto]]

  def updateTask(dnId: String, tId: String, taskUpdate: TaskUpdate): Future[Either[DatabaseError, TaskDto]]

  def deleteTask(dnId: String, tId: String): Future[Either[DatabaseError, Boolean]]

  def getTaskPredecessors(dnId: String, tId: String): Future[Either[DatabaseError, List[TaskDto]]]

  def getTaskSuccessors(dnId: String, tId: String): Future[Either[DatabaseError, List[TaskDto]]]

  def addTaskToTaskRelation(dnId: String, tId1: String, relationDto: RelationDto): Future[Either[DatabaseError, TaskDto]]

  def deleteTaskToTaskRelation(dnId: String, tId1: String, relationDto: RelationDto): Future[Either[DatabaseError, TaskDto]]
}
