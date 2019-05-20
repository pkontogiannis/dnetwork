package com.pkon.service.dnetwork.persistence

import com.pkon.service.dnetwork.DNetworkModel._
import com.pkon.service.dnetwork.persistence.Helpers._
import com.pkon.service.errors.DatabaseError
import com.pkon.service.errors.ServiceError.{GenericDatabaseError, InvalidData, RecordNotFound}
import com.pkon.utils.database.Neo4jHelpers._
import org.neo4j.driver.v1._

import scala.collection.JavaConverters._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class DNetworkPersistenceGraph(val driver: Driver) extends DNetworkPersistence {

  def getDependencyNetworksNormal: Future[Either[DatabaseError, List[DependencyNetwork]]] = {
    Try {
      val result: StatementResult = driver.session.
        run(
          "MATCH (dnp :DependencyNetwork)  " +
            "    WITH dnp as dns" +
            "    OPTIONAL MATCH (dns:DependencyNetwork)<-[r:BELONGS_TO]-(task:Task)" +
            "     WITH task as task, dns as dn" +
            "     OPTIONAL  MATCH (task:Task)<-[r:PRECEDES]-(dep:Task)" +
            "     WITH dn as dn, task as task, dep as dep" +
            "     RETURN dn.dId as dId, dn.name as dnName, dn.aircraft as dnAircraft, " +
            "     task.tId as tId, task.name as taskName, task.duration as taskDuration, task.latestEnd as taskLatestEnd," +
            "     task.department as taskDepartment, task.earliestStart as taskEarliestStart," +
            "     dep.tId as depId, dep.name as depName"
        )

      val dependencyNetworks = result.list().asScala.toList.map(
        record => {
          DependencyNetworkCypherRS(record.get("dId").asString(), record.get("dnName").asString(), getOptString(record.get("tId")), getOptString(record.get("taskName")),
            getOptDouble(record.get("taskDuration")), getOptDouble(record.get("taskLatestEnd")), getOptString(record.get("taskDepartment")),
            getOptDouble(record.get("taskEarliestStart")),
            getOptString(record.get("depId")),
            getOptString(record.get("depName")))
        }
      )
      groupRowsIntoDependencyNetworks(dependencyNetworks)
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(value) =>
        driver.session.close()
        Future(Right(value))
    }
  }

  def getDependencyNetworksCompact: Future[Either[DatabaseError, List[DependencyNetwork]]] = {
    Try {
      val result: StatementResult = driver.session.run(s"MATCH (dep:DependencyNetwork) RETURN dep.dId as id, dep.name as name")
      result.list().asScala.toList.map(
        record =>
          DependencyNetwork(record.get("id").asString(),
            record.get("name").asString(),
            List()
          )
      )
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(value) =>
        driver.session.close()
        Future(Right(value))
    }
  }

  def addDependencyNetwork(dependencyNetworkDto: DependencyNetworkDto): Future[Either[DatabaseError, DependencyNetwork]] = {
    addDependencyNetworksTransactionally(List(dependencyNetworkDto)).map {
      case Left(value) => Left(value)
      case Right(value) => Right(value.head)
    }
  }

  def addDependencyNetworks(dependencyNetworks: List[DependencyNetworkDto]): Future[Either[DatabaseError, List[DependencyNetwork]]] = {
    addDependencyNetworksTransactionally(dependencyNetworks).map {
      case Left(value) => Left(value)
      case Right(value) => Right(value)
    }
  }

  def getDependencyNetworkCompact(dnId: String): Future[Either[DatabaseError, DependencyNetwork]] = {
    Try {
      val result: StatementResult = driver.session.run(s"MATCH (dep {dId: {dId}}) RETURN dep.dId as id, dep.name as name", Values.parameters("dId", dnId))
      val record = result.single
      DependencyNetwork(record.get("id").asString(),
        record.get("name").asString(),
        List()
      )
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(RecordNotFound))
      case Success(value) =>
        driver.session.close()
        Future(Right(value))
    }
  }

  def getDependencyNetworkNormal(dnId: String): Future[Either[DatabaseError, DependencyNetwork]] = {
    Try {
      val result: StatementResult = driver.session.
        run(
          "MATCH (task:Task)-[r:BELONGS_TO]->(dn:DependencyNetwork {dId: {dId}})" +
            " WITH task as task, dn as dn" +
            " OPTIONAL  MATCH (task:Task)<-[r:PRECEDES]-(dep:Task)" +
            " WITH dn as dn, task as task, dep as dep" +
            " RETURN dn.dId as dId, dn.name as dnName, dn.aircraft as dnAircraft, " +
            " task.tId as tId, task.name as taskName, task.duration as taskDuration, task.latestEnd as taskLatestEnd," +
            " task.department as taskDepartment, task.earliestStart as taskEarliestStart," +
            " dep.tId as depId, dep.name as depName",
          Values.parameters(
            "dId", dnId
          )
        )

      val dependencyNetwork = result.list().asScala.toList.map(
        record => {
          DependencyNetworkCypherRS(record.get("dId").asString(), record.get("dnName").asString(), getOptString(record.get("tId")), getOptString(record.get("taskName")),
            getOptDouble(record.get("taskDuration")), getOptDouble(record.get("taskLatestEnd")), getOptString(record.get("taskDepartment")),
            getOptDouble(record.get("taskEarliestStart")),
            getOptString(record.get("depId")),
            getOptString(record.get("depName")))
        }
      )
      groupRowsIntoDependencyNetwork(dependencyNetwork)
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(RecordNotFound))
      case Success(value) =>
        driver.session.close()
        Future(Right(value))
    }
  }

  def updateDependencyNetwork(dnId: String, dependencyNetworkUpdate: DependencyNetworkUpdate): Future[Either[DatabaseError, DependencyNetwork]] = {
    // TODO: a partially update can be done here, it can also contains the list tasks
    Try {
      val result = driver.session
        .run(
          "MATCH (dn: DependencyNetwork { dId: {dnId} }) " +
            "SET dn.name = {name}" +
            "RETURN dn.name",
          Values.parameters(
            "dnId", dnId,
            "name", dependencyNetworkUpdate.name
          ))
      result
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(_) =>
        driver.session.close()
        Future(Right(DependencyNetwork(dnId, dependencyNetworkUpdate.name, List())))
    }
  }

  def deleteDependencyNetwork(dnId: String): Future[Either[DatabaseError, Boolean]] = {
    Try {
      val result = driver.session
        .run(
          "MATCH (task:Task)-[r:BELONGS_TO]->(dn:DependencyNetwork {dId: {dId}})" +
            "DETACH DELETE task, dn ",
          Values.parameters(
            "dId", dnId
          ))
      result
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(RecordNotFound))
      case Success(_) =>
        driver.session.close()
        Future(Right(true))
    }
  }

  def getDependencyNetworkTasks(dnId: String): Future[Either[DatabaseError, List[Task]]] = {
    Try {
      val result: StatementResult = driver.session.
        run(
          "MATCH (task:Task)-[r:BELONGS_TO]->(dn:DependencyNetwork {dId: {dId}})" +
            " WITH task as task, dn as dn" +
            " OPTIONAL  MATCH (task:Task)<-[r:PRECEDES]-(dep:Task)" +
            " WITH dn as dn, task as task, dep as dep" +
            " RETURN " +
            " task.tId as tId, task.name as taskName, task.duration as taskDuration, task.latestEnd as taskLatestEnd," +
            " task.department as taskDepartment, task.earliestStart as taskEarliestStart," +
            " dep.tId as depId, dep.name as depName",
          Values.parameters(
            "dId", dnId
          )
        )
      val taskRows = result.list().asScala.toList.map(
        record => {
          TaskCypherRS(record.get("tId").asString(), record.get("taskName").asString(),
            record.get("taskDuration").asString().toDouble, record.get("taskLatestEnd").asString().toDouble, record.get("taskDepartment").asString(),
            record.get("taskEarliestStart").asString().toDouble,
            getOptString(record.get("depId")),
            getOptString(record.get("depName")))
        }
      )
      groupRowsIntoTaskList(taskRows)
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(value) =>
        driver.session.close()
        Future(Right(value))
    }
  }

  // TODO: what if the dependency node does not exist
  def addTask(dnId: String, taskDto: TaskDto): Future[Either[DatabaseError, Task]] = {
    val transaction: Transaction = driver.session().beginTransaction()
    val task: Task = taskDtoToTask(taskDto)
    Try {
      addTaskTransactionally(transaction, task.id, task)
      addTaskToDependencyNetworkTransactionally(transaction, dnId, task.id)
      task.dependsOn.map { dep =>
        addTaskRelationTransactionally(transaction, task.id, dep.id)
      }
      transaction.success()
      transaction.close()
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(_) =>
        driver.session.close()
        Future(Right(task))
    }
  }

  def addTasks(dnId: String, tasks: List[TaskDto]): Future[Either[DatabaseError, List[Task]]] = {

    // TODO: what if a task has a dependency to a task which is not in this list
    val transaction: Transaction = driver.session().beginTransaction()
    val updatedTasks: List[Task] = tasks.map(taskDto => taskDtoToTask(taskDto)) //cleanAndFillIdsForTasks(tasks)
    Try {
      addDependencyNetworkTasksTransactionally(transaction, dnId, updatedTasks)
      addDependencyNetworkTasksRelationsTransactionally(transaction, updatedTasks)
      addDependencyNetworkTasksToDependencyNetworkTransactionally(transaction, dnId, updatedTasks)
      transaction.success()
      transaction.close()
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(_) =>
        driver.session.close()
        Future(Right(updatedTasks))
    }
  }

  def getTask(dnId: String, tId: String): Future[Either[DatabaseError, Task]] = {
    Try {
      val result: StatementResult = driver.session.
        run(
          "MATCH (dep:Task)-[:PRECEDES]->(task:Task {tId: {tId}})" +
            "    RETURN task.tId as tId, task.name as taskName, task.duration as taskDuration, task.latestEnd as taskLatestEnd," +
            "    task.department as taskDepartment, task.earliestStart as taskEarliestStart," +
            "    dep.tId as depId, dep.name as depName",
          Values.parameters(
            "tId", tId
          )
        )
      val taskRows = result.list().asScala.toList.map(
        record => {
          TaskCypherRS(record.get("tId").asString(), record.get("taskName").asString(),
            record.get("taskDuration").asString().toDouble, record.get("taskLatestEnd").asString().toDouble, record.get("taskDepartment").asString(),
            record.get("taskEarliestStart").asString().toDouble,
            getOptString(record.get("depId")),
            getOptString(record.get("depName")))
        }
      )
      groupRowsInfoTaskToTask(taskRows)
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(value) =>
        driver.session.close()
        Future(Right(value))
    }
  }

  def updateTask(dnId: String, tId: String, taskUpdate: TaskUpdate): Future[Either[DatabaseError, Task]] = {
    // TODO: a partially update can be done here, it can also contains the list of updated dependencies
    Try {
      val result = driver.session
        .run(
          "MATCH (t:Task { tId: {tId} }) " +
            "SET t.name = {name}, t.department = {department}, t.earliestStart = {earliestStart}, " +
            "t.duration = {duration}, t.latestEnd = {latestEnd} " +
            "RETURN t",
          Values.parameters(
            "tId", tId,
            "name", taskUpdate.name,
            "department", taskUpdate.department,
            "earliestStart", taskUpdate.earliestStart.toString,
            "duration", taskUpdate.duration.toString,
            "latestEnd", taskUpdate.latestEnd.toString
          ))
      result
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(_) =>
        driver.session.close()
        Future(Right(Task(tId, taskUpdate.name, taskUpdate.department, taskUpdate.earliestStart,
          taskUpdate.duration, taskUpdate.latestEnd, List())))
    }
  }

  def deleteTask(dnId: String, tId: String): Future[Either[DatabaseError, Boolean]] = {
    Try {
      val result = driver.session
        .run(
          "MATCH (n:Task { tId: {tId}})" +
            "DETACH DELETE n ",
          Values.parameters(
            "tId", tId
          ))
      result
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(_) =>
        driver.session.close()
        Future(Right(true))
    }
  }

  def getTaskPredecessors(dnId: String, tId: String): Future[Either[DatabaseError, List[Task]]] = {
    Try {
      val result: StatementResult =
        driver.session.
          run(
            s"MATCH p = (task:Task)-[:PRECEDES*]->(:Task {tId: {tId}}) " +
              s"RETURN task.tId as tId, task.name as taskName, task.duration as taskDuration, " +
              s"task.latestEnd as taskLatestEnd, task.department as taskDepartment, task.earliestStart as taskEarliestStart ",
            Values.parameters("tId", tId))

      val taskRows = result.list().asScala.toList.map(
        record => {
          TaskCypherRS(record.get("tId").asString(), record.get("taskName").asString(),
            record.get("taskDuration").asString().toDouble, record.get("taskLatestEnd").asString().toDouble, record.get("taskDepartment").asString(),
            record.get("taskEarliestStart").asString().toDouble)
        }
      )
      groupRowsIntoTaskList(taskRows)
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(value) =>
        driver.session.close()
        Future(Right(value))
    }
  }

  def getTaskSuccessors(dnId: String, tId: String): Future[Either[DatabaseError, List[Task]]] = {
    Try {
      val result: StatementResult =
        driver.session.
          run(
            s"MATCH p = (task:Task)<-[:PRECEDES*]-(:Task {tId: {tId}}) " +
              s"RETURN task.tId as tId, task.name as taskName, task.duration as taskDuration, " +
              s"task.latestEnd as taskLatestEnd, task.department as taskDepartment, task.earliestStart as taskEarliestStart ",
            Values.parameters("tId", tId))

      val taskRows = result.list().asScala.toList.map(
        record => {
          TaskCypherRS(record.get("tId").asString(), record.get("taskName").asString(),
            record.get("taskDuration").asString().toDouble, record.get("taskLatestEnd").asString().toDouble, record.get("taskDepartment").asString(),
            record.get("taskEarliestStart").asString().toDouble)
        }
      )
      groupRowsIntoTaskList(taskRows)
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(value) =>
        driver.session.close()
        Future(Right(value))
    }
  }

  def addTaskToTaskRelation(dnId: String, tId: String, relationDto: RelationDto): Future[Either[DatabaseError, Task]] = {
    val transaction: Transaction = driver.session().beginTransaction()
    Try {
      relationDto.dependsOn.map(depId => addTaskRelationTransactionally(transaction, depId, tId))
      relationDto.dependentOn.map(depId => addTaskRelationTransactionally(transaction, tId, depId))
      transaction.success()
      transaction.close()
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(_) =>
        driver.session.close()
        getTask(dnId, tId)
    }
  }

  def deleteTaskToTaskRelation(dnId: String, tId: String, relationDto: RelationDto): Future[Either[DatabaseError, Task]] = {
    val transaction: Transaction = driver.session().beginTransaction()
    Try {
      relationDto.dependsOn.map(depId => deleteTaskRelationTransactionally(transaction, depId, tId))
      relationDto.dependentOn.map(depId => deleteTaskRelationTransactionally(transaction, tId, depId))
      transaction.success()
      transaction.close()
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(_) =>
        driver.session.close()
        getTask(dnId, tId)
    }
  }

  def deleteDatabaseContent: Future[Either[DatabaseError, Boolean]] = {
    Try {
      val result = driver.session
        .run(
          "MATCH (n)" +
            "DETACH DELETE n")
      result
    } match {
      case Failure(_) =>
        driver.session.close()
        Future(Left(GenericDatabaseError))
      case Success(_) =>
        driver.session.close()
        Future(Right(true))
    }
  }

  /* Transactions */

  private def addDependencyNetworksTransactionally(dependencyNetworks: List[DependencyNetworkDto]): Future[Either[DatabaseError, List[DependencyNetwork]]] = {
    val transaction: Transaction = driver.session().beginTransaction()
    val updatedDependencyNetworks: Either[DatabaseError, List[DependencyNetwork]] = cleanAndFillIdsForDependencyNetwork(dependencyNetworks)
    updatedDependencyNetworks match {
      case Left(_) =>
        Future(Left(InvalidData))
      case Right(value) =>
        Try {
          value.foreach { dependencyNetwork =>
            addDependencyNetworkTransactionally(transaction, dependencyNetwork)
            if (dependencyNetwork.tasks.nonEmpty) {
              addDependencyNetworkTasksTransactionally(transaction, dependencyNetwork.id, dependencyNetwork.tasks)
              addDependencyNetworkTasksToDependencyNetworkTransactionally(transaction, dependencyNetwork.id, dependencyNetwork.tasks)
              addDependencyNetworkTasksRelationsTransactionally(transaction, dependencyNetwork.tasks)
            }
          }
          transaction.success()
          transaction.close()
        } match {
          case Failure(_) =>
            driver.session.close()
            Future(Left(GenericDatabaseError))
          case Success(_) =>
            driver.session.close()
            Future(Right(value))
        }
    }
  }

  private def addDependencyNetworkTransactionally(transaction: Transaction, dependencyNetwork: DependencyNetwork): StatementResult = {
    transaction.run(
      "CREATE (dependencyNetwork: DependencyNetwork { dId: {dId}, name: {name}})",
      Values.parameters(
        "dId", dependencyNetwork.id,
        "name", dependencyNetwork.name
      ))
  }

  private def addDependencyNetworkTasksTransactionally(transaction: Transaction, dnId: String, tasks: List[Task]): List[StatementResult] = {
    tasks.map(task =>
      addTaskTransactionally(transaction, task.id, task)
    )
  }

  private def addDependencyNetworkTasksToDependencyNetworkTransactionally(transaction: Transaction, dnId: String, tasks: List[Task]): List[StatementResult] = {
    tasks.map(task =>
      addTaskToDependencyNetworkTransactionally(transaction, dnId, task.id)
    )
  }

  private def addDependencyNetworkTasksRelationsTransactionally(transaction: Transaction, tasks: List[Task]): List[StatementResult] = {
    tasks.flatMap(task =>
      task.dependsOn.map { dep =>
        addTaskRelationTransactionally(transaction, task.id, dep.id)
      }
    )
  }

  private def addTaskRelationTransactionally(transaction: Transaction, task: String, dep: String): StatementResult = {
    transaction.run(
      "MATCH (a:Task)" +
        "WITH a" +
        "  MATCH (b:Task)" +
        "WHERE a.tId = {dep} AND b.tId = {task}" +
        "MERGE (a)-[r:PRECEDES]->(b)" +
        "RETURN type(r)",
      Values.parameters(
        "dep", dep,
        "task", task
      ))
  }

  private def deleteTaskRelationTransactionally(transaction: Transaction, task: String, dep: String): StatementResult = {
    transaction.run(
      "MATCH p = (t1:Task {tId: {task}})-[r:PRECEDES]->(t2:Task {tId: {dep}})" +
        "DELETE r",
      Values.parameters(
        "dep", dep,
        "task", task
      ))
  }

  private def addTaskToDependencyNetworkTransactionally(transaction: Transaction, dnId: String, tId: String): StatementResult = {
    transaction.run(
      "MATCH (a:DependencyNetwork)" +
        "WITH a" +
        "  MATCH (b:Task)" +
        "WHERE a.dId = {dId} AND b.tId = {tId}" +
        "MERGE (b)-[r:BELONGS_TO]->(a)" +
        "RETURN type(r)",
      Values.parameters(
        "dId", dnId,
        "tId", tId
      ))
  }

  private def addTaskTransactionally(transaction: Transaction, tId: String, task: Task): StatementResult = {
    transaction.run(
      "CREATE (task: Task {tId: {tId}, name: {name}, " +
        "department: {department}, earliestStart: {earliestStart}, duration: {duration}, latestEnd: {latestEnd}})",
      Values.parameters(
        "tId", tId,
        "name", task.name,
        "department", task.department,
        "earliestStart", task.earliestStart.toString,
        "duration", task.duration.toString,
        "latestEnd", task.latestEnd.toString
      ))
  }

}
