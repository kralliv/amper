package org.jetbrains.amper.tasks

import org.jetbrains.amper.engine.Task
import org.jetbrains.amper.engine.TaskName
import org.jetbrains.amper.util.BuildType

fun TaskName.suffixed(suffix: String): TaskName {
    return TaskName("$name$suffix")
}

class AndroidCompileTask(
    private val jvmCompileTask: JvmCompileTask,
    private val buildType: BuildType
) : Task {
    override val taskName: TaskName
        get() = jvmCompileTask.taskName.suffixed(buildType.name)

    override suspend fun run(dependenciesResult: List<TaskResult>): TaskResult = jvmCompileTask.run(dependenciesResult)
}