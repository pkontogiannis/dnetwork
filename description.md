#### What is a **dependency network**?

A **dependency network** is a set of rules that define the _earliest start_, _duration_ and _latest end_ of operational tasks
in the **turnaround** and the dependencies between those tasks.
The duration is not necessarily as long as the time between the earlist start and latest end (eg. sometimes slack is built into the design).
The dependency network is effectively a representation of the process design of ground services, as described in the Ground Operations Manuals (GOMS).
The _earliest start_ and _latest end_ can be defined in terms of number of minutes since the arrival the aircraft (eg. A+1, A+5, etc.)
or the number of minutes until the departure of the aircraft (eg. D-45, D-40, etc.)

#### Dependency Network API

The **Dependency Network API** is a web API that allows dependency networks to be stored, accessed, and changed.
This happens quite often, as process designers are constantly trying to optimize the turnaround - and by extension all of its tasks.

The end-users of the **Dependency Network API** are:

- developers building a web application (e.g. in Angular) for these process designers.
- developers building optimization tools that need the dependency network as input
  (these only need to retrieve the dependency network, not make changes)

## Functional Requirements

The **Dependency Network API** has the following _functional requirements_:

- It stores tasks with their dependencies on other tasks. **For this assignment, it is not required to store these in a database.
  It is sufficient to keep them in memory**.
- The dependency network can be retrieved as a whole.
- Given a task, you can retrieve all other tasks it depends on (ie. _upstream tasks_).
- Given a task, you can retrieve all other tasks that are dependent on it (ie. _downstream tasks_).
- You can add a new task, including dependencies with other tasks.
- You can create new dependencies between existing tasks.
- You can delete tasks.
- You can remove dependencies between tasks.
- The dependency network (or parts of it) should be viewable/retrievable by anyone.
- Only a select few should be able to change the dependency network (eg. add tasks or dependencies, change tasks/dependencies, remove tasks/dependencies),
  so some form of user/API authentication/authorization is required.
