package com.pkon.dnetwork.persistence

import java.util.UUID

import com.pkon.service.dnetwork.DNetworkModel._
import com.pkon.service.dnetwork.persistence.Helpers._
import org.scalatest.{FunSpecLike, Matchers}

class HelpersSpec extends FunSpecLike with Matchers {

  val dNetworkOne = DependencyNetwork(id = UUID.randomUUID().toString, name = "DNet Name One", List())
  val dNetworkTwo = DependencyNetwork(id = UUID.randomUUID().toString, name = "DNet Name Two", List())

  val taskThree = Task(UUID.randomUUID().toString, "Task Three", "Dep1", 1, 2, 6, List())
  val taskFour = Task(UUID.randomUUID().toString, "Task Four", "Dep1", 1, 2, 6, List())
  val taskOne: Task = Task(UUID.randomUUID().toString, "Task One", "Dep1", 1, 2, 6, List(TaskInfo(taskThree.id, taskThree.name), TaskInfo(taskFour.id, taskFour.name)).sortBy(_.id))
  val taskTwo: Task = Task(UUID.randomUUID().toString, "Task Two", "Dep1", 1, 2, 6, List(TaskInfo(taskOne.id, taskOne.name), TaskInfo(taskFour.id, taskFour.name)).sortBy(_.id))

  def graphDBRowsToDNetwork(network: DependencyNetwork): List[DependencyNetworkCypherRS] = List(
    DependencyNetworkCypherRS(dId = network.id, dnName = network.name, tId = Some(taskOne.id), taskName = Some(taskOne.name), taskDuration = Some(taskOne.duration), taskLatestEnd = Some(taskOne.latestEnd), taskDepartment = Some(taskOne.department), taskEarliestStart = Some(taskOne.earliestStart), depId = Some(taskThree.id), depName = Some(taskThree.name)),
    DependencyNetworkCypherRS(dId = network.id, dnName = network.name, tId = Some(taskOne.id), taskName = Some(taskOne.name), taskDuration = Some(taskOne.duration), taskLatestEnd = Some(taskOne.latestEnd), taskDepartment = Some(taskOne.department), taskEarliestStart = Some(taskOne.earliestStart), depId = Some(taskFour.id), depName = Some(taskFour.name)),
    DependencyNetworkCypherRS(dId = network.id, dnName = network.name, tId = Some(taskTwo.id), taskName = Some(taskTwo.name), taskDuration = Some(taskTwo.duration), taskLatestEnd = Some(taskTwo.latestEnd), taskDepartment = Some(taskTwo.department), taskEarliestStart = Some(taskTwo.earliestStart), depId = Some(taskOne.id), depName = Some(taskOne.name)),
    DependencyNetworkCypherRS(dId = network.id, dnName = network.name, tId = Some(taskTwo.id), taskName = Some(taskTwo.name), taskDuration = Some(taskTwo.duration), taskLatestEnd = Some(taskTwo.latestEnd), taskDepartment = Some(taskTwo.department), taskEarliestStart = Some(taskTwo.earliestStart), depId = Some(taskFour.id), depName = Some(taskFour.name))
  )

  def expectedDNetwork(network: DependencyNetwork) = DependencyNetwork(network.id, network.name, List(taskOne, taskTwo).sortBy(_.id))

  val graphDBRowsToTask: List[TaskCypherRS] = List(
    TaskCypherRS(tId = taskOne.id, taskName = taskOne.name, taskDuration = taskOne.duration, taskLatestEnd = taskOne.latestEnd, taskDepartment = taskOne.department, taskEarliestStart = taskOne.earliestStart, depId = Some(taskThree.id), depName = Some(taskThree.name)),
    TaskCypherRS(tId = taskOne.id, taskName = taskOne.name, taskDuration = taskOne.duration, taskLatestEnd = taskOne.latestEnd, taskDepartment = taskOne.department, taskEarliestStart = taskOne.earliestStart, depId = Some(taskFour.id), depName = Some(taskFour.name)),
    TaskCypherRS(tId = taskTwo.id, taskName = taskTwo.name, taskDuration = taskTwo.duration, taskLatestEnd = taskTwo.latestEnd, taskDepartment = taskTwo.department, taskEarliestStart = taskTwo.earliestStart, depId = Some(taskOne.id), depName = Some(taskOne.name)),
    TaskCypherRS(tId = taskTwo.id, taskName = taskTwo.name, taskDuration = taskTwo.duration, taskLatestEnd = taskTwo.latestEnd, taskDepartment = taskTwo.department, taskEarliestStart = taskTwo.earliestStart, depId = Some(taskFour.id), depName = Some(taskFour.name))
  )

  val expectedTaskList: List[Task] = List(taskOne, taskTwo).sortBy(_.id)

  describe("DNetwork Persistence Graph") {

    it("Successfully group rows into dependency network") {
      groupRowsIntoDependencyNetwork(graphDBRowsToDNetwork(dNetworkOne)) shouldEqual expectedDNetwork(dNetworkOne)
    }

    it("Successfully group rows into a list of tasks") {
      groupRowsIntoTaskList(graphDBRowsToTask) shouldEqual expectedTaskList
    }

    it("Successfully group rows into a list of dependency networks") {
      groupRowsIntoDependencyNetworks(graphDBRowsToDNetwork(dNetworkOne) ::: graphDBRowsToDNetwork(dNetworkTwo)) shouldEqual List(expectedDNetwork(dNetworkOne), expectedDNetwork(dNetworkTwo)).sortBy(_.id)
    }

    it("Successfully group rows into a task") {
      val graphDBRowsToTask: List[TaskCypherRS] = List(
        TaskCypherRS(tId = taskOne.id, taskName = taskOne.name, taskDuration = taskOne.duration, taskLatestEnd = taskOne.latestEnd, taskDepartment = taskOne.department, taskEarliestStart = taskOne.earliestStart, depId = Some(taskThree.id), depName = Some(taskThree.name)),
        TaskCypherRS(tId = taskOne.id, taskName = taskOne.name, taskDuration = taskOne.duration, taskLatestEnd = taskOne.latestEnd, taskDepartment = taskOne.department, taskEarliestStart = taskOne.earliestStart, depId = Some(taskFour.id), depName = Some(taskFour.name)),
      )
      groupRowsInfoTaskToTask(graphDBRowsToTask) shouldEqual taskOne
    }
  }
}
