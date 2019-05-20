package routes

import java.util.UUID

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.headers.RawHeader
import akka.http.scaladsl.server.Route
import com.pkon.service.dnetwork.DNetworkModel._
import com.pkon.service.dnetwork.{DNetworkRoutes, DNetworkServiceDefault}
import com.pkon.service.user.UserModel.Token
import com.pkon.utils.jwt.JWTUtils
import io.circe.generic.auto._
import io.circe.syntax._
import org.scalatest.BeforeAndAfterAll
import routes.DNetworkRoutesITData._
import routes.helpers.ServiceSuite

import scala.collection.JavaConverters._
import scala.language.postfixOps

class DNetworkRoutesIT extends ServiceSuite with BeforeAndAfterAll {

  val dNetworkService = new DNetworkServiceDefault(dNetworkPersistence)
  val dNetworkRoutes: Route = new DNetworkRoutes(dNetworkService).dNetworkRoutes

  private val roles: List[String] = config.getStringList("authentication.roles").asScala.toList
  val accessToken: Token = JWTUtils.getAccessToken(UUID.randomUUID().toString, roles.head)

  "DNetwork Routes" should {

    "successfully add a dependency network without tasks" in {
      Post("/api/v01/dependencynetworks", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithoutTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[DependencyNetworkDto]
        assert(result.name === expectedNetworkWithoutTasks.name)
      }
    }

    "successfully add multiple dependency networks without tasks" in {
      Post("/api/v01/dependencynetworks?mode=batch", RequestData[List[DependencyNetworkDto]](dependencyNetworkDtoListWithoutTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[List[DependencyNetworkDto]]
        assert(result.nonEmpty)
      }
    }

    "successfully add a dependency network with tasks" in {
      Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[DependencyNetworkDto]
        assert(result.name === expectedNetworkWithTasks.name && result.tasks.nonEmpty && result.tasks.sortBy(_.name).zip(expectedNetworkWithTasks.tasks.sortBy(_.name))
          .forall(tuple => compareTasksWithoutUUID(tuple._1, tuple._2)))
      }
    }

    "successfully add multiple dependency networks with tasks" in {
      Post("/api/v01/dependencynetworks?mode=batch", RequestData[List[DependencyNetworkDto]](dependencyNetworkDtoListWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[List[DependencyNetworkDto]]
        val actualResponseDNetwork = result.find(_.name == "KLM 542").get
        assert(actualResponseDNetwork.name === expectedNetworkWithTasks.name && actualResponseDNetwork.tasks.nonEmpty && actualResponseDNetwork.tasks.sortBy(_.name).zip(expectedNetworkWithTasks.tasks.sortBy(_.name))
          .forall(tuple => compareTasksWithoutUUID(tuple._1, tuple._2)))
      }
    }

    "successfully retrieve dependency networks in a compact mode" in {
      val result: List[DependencyNetworkDto] = Post("/api/v01/dependencynetworks?mode=batch", RequestData[List[DependencyNetworkDto]](dependencyNetworkDtoListWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        responseAs[List[DependencyNetworkDto]]
      }
      Get("/api/v01/dependencynetworks?mode=compact") ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.OK)
        val result = responseAs[List[DependencyNetworkDto]]
        assert(result.nonEmpty)
      }
    }

    "successfully retrieve dependency networks in a normal mode" in {
      Post("/api/v01/dependencynetworks?mode=batch", RequestData[List[DependencyNetworkDto]](dependencyNetworkDtoListWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        Get("/api/v01/dependencynetworks?mode=normal") ~> dNetworkRoutes ~> check {
          handled shouldBe true
          status should ===(StatusCodes.OK)
          val result = responseAs[List[DependencyNetworkDto]]
          val actualResponseDNetwork = result.find(_.name == "KLM 542").get
          assert(actualResponseDNetwork.name === expectedNetworkWithTasks.name)
        }
      }
    }

    "successfully retrieve a dependency network in a compact mode" in {
      val dNetworkId = Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        responseAs[DependencyNetworkDto].id.get
      }
      Get(s"/api/v01/dependencynetworks/$dNetworkId?mode=compact") ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.OK)
        val result = responseAs[DependencyNetworkDto]
        assert(result.name === expectedNetworkWithoutTasks.name)
      }
    }

    "successfully retrieve a dependency network in a normal mode" in {
      val dNetworkId = Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        responseAs[DependencyNetworkDto].id.get
      }
      Get(s"/api/v01/dependencynetworks/$dNetworkId?mode=normal") ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.OK)
        val result = responseAs[DependencyNetworkDto]
        assert(result.name === expectedNetworkWithTasks.name && result.tasks.nonEmpty && result.tasks.sortBy(_.name).zip(expectedNetworkWithTasks.tasks.sortBy(_.name))
          .forall(tuple => compareTasksWithoutUUID(tuple._1, tuple._2)))
      }
    }

    "successfully update a dependency network" in {
      val dNetworkId = Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        responseAs[DependencyNetworkDto].id.get
      }
      val result = Put(s"/api/v01/dependencynetworks/$dNetworkId", RequestData[DependencyNetworkUpdate](DependencyNetworkUpdate(name = "Update Network")))~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.OK)
        val result = responseAs[DependencyNetworkDto]
        assert(result.name === "Update Network" && result.tasks.sortBy(_.name).zip(expectedNetworkWithTasks.tasks.sortBy(_.name))
          .forall(tuple => compareTasksWithoutUUID(tuple._1, tuple._2)))
        result
      }
      Get(s"/api/v01/dependencynetworks/${result.id.get}?mode=compact") ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.OK)
        val result = responseAs[DependencyNetworkDto]
        assert(result.name == "Update Network")
      }
    }

    "successfully delete a dependency network" in {
      val dNetworkId = Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        responseAs[DependencyNetworkDto].id.get
      }
      Delete(s"/api/v01/dependencynetworks/$dNetworkId")~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.NoContent)
      }
      Get(s"/api/v01/dependencynetworks/$dNetworkId?mode=compact") ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.NotFound)
      }
    }

    "successfully add a task to a dependency network" in {
      Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[DependencyNetworkDto]
        val taskDependsOn = result.tasks.filter(_.name == "E").head
        val taskDto: TaskDto = TaskDto(name = "K", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4,
          dependsOn = List(
            TaskInfoDto(id = Some(taskDependsOn.id.get), name = taskDependsOn.name)
          )
        )
        Post(s"/api/v01/dependencynetworks/${result.id.get}/tasks?mode=simple", RequestData[TaskDto](taskDto)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
          handled shouldBe true
          status should ===(StatusCodes.Created)
          Get(s"/api/v01/dependencynetworks/${result.id.get}?mode=normal") ~> dNetworkRoutes ~> check {
            handled shouldBe true
            status should ===(StatusCodes.OK)
            val result = responseAs[DependencyNetworkDto]
            assert(result.name === expectedNetworkWithTasks.name)

            val updatedTaskList = expectedNetworkWithTasks.tasks.::(taskDto)

            assert(result.tasks.size == updatedTaskList.size &&
              result.tasks.sortBy(_.name).zip(updatedTaskList.sortBy(_.name))
                .forall(tuple => compareTasksWithoutUUID(tuple._1, tuple._2)))
          }
        }
      }
    }

    "successfully add multiple tasks to a dependency network" in {
      Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[DependencyNetworkDto]

        val taskE = result.tasks.filter(_.name == "E").head
        val taskC = result.tasks.filter(_.name == "C").head

        val taskDtoList: List[TaskDto] = List(
          TaskDto(name = "K", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4,
            dependsOn = List(TaskInfoDto(id = Some(taskE.id.get), name = taskE.name))),
          TaskDto(name = "Z", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4,
            dependsOn = List(TaskInfoDto(id = Some(taskC.id.get), name = taskC.name)))
        )
        Post(s"/api/v01/dependencynetworks/${result.id.get}/tasks?mode=batch", RequestData[List[TaskDto]](taskDtoList)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
          handled shouldBe true
          status should ===(StatusCodes.Created)
          Get(s"/api/v01/dependencynetworks/${result.id.get}?mode=normal") ~> dNetworkRoutes ~> check {
            handled shouldBe true
            status should ===(StatusCodes.OK)
            val result = responseAs[DependencyNetworkDto]
            assert(result.name === expectedNetworkWithTasks.name)

            val updatedTaskList: List[TaskDto] = expectedNetworkWithTasks.tasks ::: taskDtoList

            assert(result.tasks.size == updatedTaskList.size &&
              result.tasks.sortBy(_.name).zip(updatedTaskList.sortBy(_.name))
                .forall(tuple => compareTasksWithoutUUID(tuple._1, tuple._2)))
          }
        }
      }
    }

    "successfully retrieve the tasks of a dependency network" in {
      Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[DependencyNetworkDto]
        Get(s"/api/v01/dependencynetworks/${result.id.get}/tasks") ~> dNetworkRoutes ~> check {
          handled shouldBe true
          status should ===(StatusCodes.OK)
          val tasks = responseAs[List[TaskDto]]
          assert(tasks.size == expectedNetworkWithTasks.tasks.size &&
            tasks.sortBy(_.name).zip(expectedNetworkWithTasks.tasks.sortBy(_.name))
              .forall(tuple => compareTasksWithoutUUID(tuple._1, tuple._2)))
        }
      }
    }

    "successfully retrieve a task of a dependency network" in {
      Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[DependencyNetworkDto]
        val taskId = result.tasks.find(_.name == "E").get.id.get
        Get(s"/api/v01/dependencynetworks/${result.id.get}/tasks/$taskId") ~> dNetworkRoutes ~> check {
          handled shouldBe true
          status should ===(StatusCodes.OK)
          val result = responseAs[TaskDto]
          assert(compareTasksWithoutUUID(result, expectedTask))
        }
      }
    }

    "successfully retrieve predecessors of a task" in {
      Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[DependencyNetworkDto]
        val taskId = result.tasks.find(_.name == "D").get.id.get
        val expectedDependOn: List[TaskDto] = tasksWithDependencies.filterNot(task => task.name == "D" || task.name == "A4" || task.name == "FINISH")
        Get(s"/api/v01/dependencynetworks/${result.id.get}/tasks/$taskId/predecessors") ~> dNetworkRoutes ~> check {
          handled shouldBe true
          status should ===(StatusCodes.OK)
          val tasks = responseAs[List[TaskDto]]
          assert(tasks.size == expectedDependOn.size)
        }
      }
    }

    "successfully retrieve successors of a task" in {
      Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[DependencyNetworkDto]
        val taskId = result.tasks.find(_.name == "E").get.id.get
        val expectedDependOn: List[TaskDto] = tasksWithDependencies.filter(task => task.name == "D" || task.name == "FINISH")
        Get(s"/api/v01/dependencynetworks/${result.id.get}/tasks/$taskId/successors") ~> dNetworkRoutes ~> check {
          handled shouldBe true
          status should ===(StatusCodes.OK)
          val tasks = responseAs[List[TaskDto]]
          assert(tasks.size == expectedDependOn.size)
        }
      }
    }

    "successfully add a relation between tasks" in {
      Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[DependencyNetworkDto]

        val taskE = result.tasks.find(_.name == "E").get.id.get
        val taskA4 = result.tasks.find(_.name == "A4").get.id.get
        val taskB = result.tasks.find(_.name == "B").get.id.get
        val relationDto = RelationDto(dependentOn = List(taskA4), dependsOn = List(taskB))
        Post(s"/api/v01/dependencynetworks/${result.id.get}/tasks/$taskE/relations", RequestData[RelationDto](relationDto)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
          handled shouldBe true
          status should ===(StatusCodes.Created)
          Get(s"/api/v01/dependencynetworks/${result.id.get}/tasks") ~> dNetworkRoutes ~> check {
            handled shouldBe true
            status should ===(StatusCodes.OK)
            val tasks = responseAs[List[TaskDto]]
            val tasksWithDependencies: List[TaskDto] = List(
              TaskDto(name = "A1", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4, dependsOn = List()),
              TaskDto(name = "A2", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4, dependsOn = List()),
              TaskDto(name = "A3", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4, dependsOn = List()),
              TaskDto(name = "A4", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4, dependsOn = List()),
              TaskDto(name = "B", department = "Dep 1", earliestStart = 5, duration = 2, latestEnd = 9,
                dependsOn = List(TaskInfoDto(name = "A1"), TaskInfoDto(name = "E"), TaskInfoDto(name = "A2"))),
              TaskDto(name = "C", department = "Dep 1", earliestStart = 8, duration = 1, latestEnd = 10,
                dependsOn = List(TaskInfoDto(name = "B"))),
              TaskDto(name = "D", department = "Dep 1", earliestStart = 11, duration = 1, latestEnd = 13,
                dependsOn = List(TaskInfoDto(name = "C"), TaskInfoDto(name = "E"))),
              TaskDto(name = "E", department = "Dep 1", earliestStart = 5, duration = 3, latestEnd = 10,
                dependsOn = List(TaskInfoDto(name = "A3"), TaskInfoDto(name = "A4"))),
              TaskDto(name = "FINISH", department = "Dep 1", earliestStart = 14, duration = 2, latestEnd = 17,
                dependsOn = List(TaskInfoDto(name = "D"), TaskInfoDto(name = "A4")))
            )
            assert(tasks.size == tasksWithDependencies.size &&
              tasks.sortBy(_.name).zip(tasksWithDependencies.sortBy(_.name))
                .forall(tuple => compareTasksWithoutUUID(tuple._1, tuple._2)))
          }
        }
      }
    }

    "successfully delete a relation between tasks" in {
      Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[DependencyNetworkDto]

        val taskB = result.tasks.find(_.name == "B").get.id.get

        val taskA2 = result.tasks.find(_.name == "A2").get.id.get
        val taskC = result.tasks.find(_.name == "C").get.id.get
        val relationDto = RelationDto(dependentOn = List(taskC), dependsOn = List(taskA2))

        Delete(s"/api/v01/dependencynetworks/${result.id.get}/tasks/$taskB/relations", RequestData[RelationDto](relationDto)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
          handled shouldBe true
          status should ===(StatusCodes.OK)
          Get(s"/api/v01/dependencynetworks/${result.id.get}/tasks") ~> dNetworkRoutes ~> check {
            handled shouldBe true
            status should ===(StatusCodes.OK)
            val tasks = responseAs[List[TaskDto]]
            val tasksWithDependencies: List[TaskDto] = List(
              TaskDto(name = "A1", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4, dependsOn = List()),
              TaskDto(name = "A2", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4, dependsOn = List()),
              TaskDto(name = "A3", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4, dependsOn = List()),
              TaskDto(name = "A4", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4, dependsOn = List()),
              TaskDto(name = "B", department = "Dep 1", earliestStart = 5, duration = 2, latestEnd = 9,
                dependsOn = List(TaskInfoDto(name = "A1"))),
              TaskDto(name = "C", department = "Dep 1", earliestStart = 8, duration = 1, latestEnd = 10,
                dependsOn = List()),
              TaskDto(name = "D", department = "Dep 1", earliestStart = 11, duration = 1, latestEnd = 13,
                dependsOn = List(TaskInfoDto(name = "C"), TaskInfoDto(name = "E"))),
              TaskDto(name = "E", department = "Dep 1", earliestStart = 5, duration = 3, latestEnd = 10,
                dependsOn = List(TaskInfoDto(name = "A3"))),
              TaskDto(name = "FINISH", department = "Dep 1", earliestStart = 14, duration = 2, latestEnd = 17,
                dependsOn = List(TaskInfoDto(name = "D"), TaskInfoDto(name = "A4")))
            )
            assert(tasks.size == tasksWithDependencies.size &&
              tasks.sortBy(_.name).zip(tasksWithDependencies.sortBy(_.name))
                .forall(tuple => compareTasksWithoutUUID(tuple._1, tuple._2)))
          }
        }
      }
    }

    "successfully update a task of a dependency network" in {
      Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[DependencyNetworkDto]

        val taskE = result.tasks.find(_.name == "E").get.id.get

        val taskUpdate: TaskUpdate = TaskUpdate(name = "UpdatedTask", department = "Dep 1", earliestStart = 5, duration = 3, latestEnd = 10)

        Put(s"/api/v01/dependencynetworks/${result.id.get}/tasks/$taskE", RequestData[TaskUpdate](taskUpdate)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
          handled shouldBe true
          status should ===(StatusCodes.OK)
        }

        val expectedTask: TaskDto = TaskDto(name = "UpdatedTask", department = "Dep 1", earliestStart = 5, duration = 3, latestEnd = 10,
          dependsOn = List(TaskInfoDto(name = "A3")))

        Get(s"/api/v01/dependencynetworks/${result.id.get}/tasks/$taskE") ~> dNetworkRoutes ~> check {
          handled shouldBe true
          status should ===(StatusCodes.OK)
          val result = responseAs[TaskDto]
          assert(compareTasksWithoutUUID(result, expectedTask))
        }
      }
    }

    "successfully delete a task of a dependency network" in {
      Post("/api/v01/dependencynetworks?mode=simple", RequestData[DependencyNetworkDto](dependencyNetworkDtoWithTasks)) ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
        handled shouldBe true
        status should ===(StatusCodes.Created)
        val result = responseAs[DependencyNetworkDto]

        val taskE = result.tasks.find(_.name == "E").get.id.get

        Delete(s"/api/v01/dependencynetworks/${result.id.get}/tasks/$taskE") ~> RawHeader("Authorization", accessToken.token) ~> dNetworkRoutes ~> check {
          handled shouldBe true
          status should ===(StatusCodes.NoContent)
        }

        Get(s"/api/v01/dependencynetworks/${result.id.get}/tasks") ~> dNetworkRoutes ~> check {
          handled shouldBe true
          status should ===(StatusCodes.OK)
          val tasks = responseAs[List[TaskDto]]
          val tasksWithDependencies: List[TaskDto] = List(
            TaskDto(name = "A1", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4, dependsOn = List()),
            TaskDto(name = "A2", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4, dependsOn = List()),
            TaskDto(name = "A3", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4, dependsOn = List()),
            TaskDto(name = "A4", department = "Dep 1", earliestStart = 0, duration = 2, latestEnd = 4, dependsOn = List()),
            TaskDto(name = "B", department = "Dep 1", earliestStart = 5, duration = 2, latestEnd = 9,
              dependsOn = List(TaskInfoDto(name = "A1"), TaskInfoDto(name = "A2"))),
            TaskDto(name = "C", department = "Dep 1", earliestStart = 8, duration = 1, latestEnd = 10,
              dependsOn = List(TaskInfoDto(name = "B"))),
            TaskDto(name = "D", department = "Dep 1", earliestStart = 11, duration = 1, latestEnd = 13,
              dependsOn = List(TaskInfoDto(name = "C"))),
            TaskDto(name = "FINISH", department = "Dep 1", earliestStart = 14, duration = 2, latestEnd = 17,
              dependsOn = List(TaskInfoDto(name = "D"), TaskInfoDto(name = "A4")))
          )
          assert(tasks.size == tasksWithDependencies.size &&
            tasks.sortBy(_.name).zip(tasksWithDependencies.sortBy(_.name))
              .forall(tuple => compareTasksWithoutUUID(tuple._1, tuple._2)))
        }
      }
    }

  }
}
