package com.pkon.service.dnetwork.persistence

import com.pkon.service.dnetwork.DNetworkModel._
import com.pkon.service.errors.DatabaseError

import scala.concurrent.Future

trait DNetworkPersistence {

  def getDependencyNetworksNormal: Future[Either[DatabaseError, List[DependencyNetwork]]]

  def getDependencyNetworksCompact: Future[Either[DatabaseError, List[DependencyNetwork]]]

  def addDependencyNetwork(dependencyNetworkDto: DependencyNetworkDto): Future[Either[DatabaseError, DependencyNetwork]]

  def addDependencyNetworks(dependencyNetworks: List[DependencyNetworkDto]): Future[Either[DatabaseError, List[DependencyNetwork]]]

  def getDependencyNetworkCompact(dnId: String): Future[Either[DatabaseError, DependencyNetwork]]

  def getDependencyNetworkNormal(dnId: String): Future[Either[DatabaseError, DependencyNetwork]]

  def updateDependencyNetwork(dnId: String, dependencyNetworkUpdate: DependencyNetworkUpdate): Future[Either[DatabaseError, DependencyNetwork]]

  def deleteDependencyNetwork(dnId: String): Future[Either[DatabaseError, Boolean]]

  def getDependencyNetworkTasks(dnId: String): Future[Either[DatabaseError, List[Task]]]

  def addTask(dnId: String, taskDto: TaskDto): Future[Either[DatabaseError, Task]]

  def addTasks(dnId: String, tasks: List[TaskDto]): Future[Either[DatabaseError, List[Task]]]

  def getTask(dnId: String, tId: String): Future[Either[DatabaseError, Task]]

  def updateTask(dnId: String, tId: String, taskUpdate: TaskUpdate): Future[Either[DatabaseError, Task]]

  def deleteTask(dnId: String, tId: String): Future[Either[DatabaseError, Boolean]]

  def getTaskPredecessors(dnId: String, tId: String): Future[Either[DatabaseError, List[Task]]]

  def getTaskSuccessors(dnId: String, tId: String): Future[Either[DatabaseError, List[Task]]]

  def addTaskToTaskRelation(dnId: String, tId: String, relationDto: RelationDto): Future[Either[DatabaseError, Task]]

  def deleteTaskToTaskRelation(dnId: String, tId: String, relationDto: RelationDto): Future[Either[DatabaseError, Task]]

}
