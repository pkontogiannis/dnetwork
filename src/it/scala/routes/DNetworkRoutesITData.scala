package routes

import java.util.UUID

import com.pkon.service.dnetwork.DNetworkModel.{DependencyNetworkDto, TaskDto, TaskInfoDto}

object DNetworkRoutesITData {

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
      dependsOn = List(TaskInfoDto(name = "C"), TaskInfoDto(name = "E"))),
    TaskDto(name = "E", department = "Dep 1", earliestStart = 5, duration = 3, latestEnd = 10,
      dependsOn = List(TaskInfoDto(name = "A3"))),
    TaskDto(name = "FINISH", department = "Dep 1", earliestStart = 14, duration = 2, latestEnd = 17,
      dependsOn = List(TaskInfoDto(name = "D"), TaskInfoDto(name = "A4")))
  )

  val dependencyNetworkDtoWithoutTasks: DependencyNetworkDto = DependencyNetworkDto(name = "KLM 542", tasks = List())
  val dependencyNetworkDtoListWithoutTasks: List[DependencyNetworkDto] = List(
    DependencyNetworkDto(name = "KLM 542 1", tasks = List()), DependencyNetworkDto(name = "KLM 542 2", tasks = List())
  )

  val dependencyNetworkDtoWithTasks: DependencyNetworkDto = DependencyNetworkDto(name = "KLM 542", tasks = tasksWithDependencies)

  val dependencyNetworkDtoListWithTasks: List[DependencyNetworkDto] = List(
    DependencyNetworkDto(name = "KLM 542", tasks = tasksWithDependencies),
    DependencyNetworkDto(name = "KLM 542 Part 2", tasks = tasksWithDependencies)
  )

  val expectedNetworkWithoutTasks = DependencyNetworkDto(id = Some(UUID.randomUUID().toString), name = "KLM 542", tasks = List())

  val expectedNetworkWithTasks = DependencyNetworkDto(id = Some(UUID.randomUUID().toString), name = "KLM 542", tasks = tasksWithDependencies)

  val expectedTask: TaskDto = TaskDto(name = "E", department = "Dep 1", earliestStart = 5, duration = 3, latestEnd = 10,
    dependsOn = List(TaskInfoDto(name = "A3")))

}
