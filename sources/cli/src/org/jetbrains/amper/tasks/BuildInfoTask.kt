/*
 * Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.amper.tasks

import org.eclipse.jgit.internal.storage.file.FileRepository
import org.jetbrains.amper.cli.AmperProjectRoot
import org.jetbrains.amper.compilation.BuildInfoUserSettings
import org.jetbrains.amper.compilation.mergedBuildInfoSettings
import org.jetbrains.amper.core.extract.cleanDirectory
import org.jetbrains.amper.engine.Task
import org.jetbrains.amper.engine.TaskName
import org.jetbrains.amper.frontend.Fragment
import org.jetbrains.amper.frontend.LeafFragment
import org.jetbrains.amper.frontend.Platform
import org.jetbrains.amper.frontend.PotatoModule
import org.jetbrains.amper.tasks.CommonTaskUtils.userReadableList
import org.jetbrains.amper.util.property
import org.jetbrains.amper.util.writeProperties
import org.slf4j.LoggerFactory
import java.nio.file.Path
import kotlin.io.path.div
import kotlin.io.path.exists

class BuildInfoTask(
    override val taskName: TaskName,
    private val module: PotatoModule,
    private val isTest: Boolean,
    private val fragments: List<Fragment>,
    private val platform: Platform,
    private val projectRoot: AmperProjectRoot,
    private val taskOutputRoot: TaskOutputRoot,
) : Task {

    override suspend fun run(dependenciesResult: List<org.jetbrains.amper.tasks.TaskResult>): org.jetbrains.amper.tasks.TaskResult {
        require(fragments.isNotEmpty()) {
            "fragments list is empty for build-info task, module=${module.userReadableName}"
        }

        logger.debug("build-info ${module.userReadableName} -- ${fragments.userReadableList()}")

        val userSettings = fragments.mergedBuildInfoSettings()

        // has to run every time right now, git might have changed
        cleanDirectory(taskOutputRoot.path)

        createBuildInfo(userSettings)

        return TaskResult(
            resourcesOutputRoot = taskOutputRoot.path.toAbsolutePath(),
            dependencies = dependenciesResult,
            module = module,
            isTest = isTest,
        )
    }

    private suspend fun createBuildInfo(
        settings: BuildInfoUserSettings,
    ) {
        val file = settings.file ?: return

        val outputFile = taskOutputRoot.path / file

        writeProperties(outputFile, comment = "build-info") {
            property("version", fragments.version(platform))

            val gitRoot = projectRoot.path.resolve(".git")
            if (gitRoot.exists()) {
                val git = FileRepository(gitRoot.toFile())
                val head = git.getReflogReader("HEAD").lastEntry
                val shortHash = git.newObjectReader().use { it.abbreviate(head.newId).name() }
                property("commitHash", head.newId.name)
                property("commitShortHash", shortHash)
                property("commitDate", head.who.`when`.toInstant().toString())
            }
        }
    }

    private fun List<Fragment>.version(platform: Platform): String? {
        val fragment = find { !it.isTest && it.fragmentDependencies.isEmpty() }
            ?: filterIsInstance<LeafFragment>().singleOrNull { !it.isTest && platform in it.platforms }

        return fragment?.settings?.publishing?.version
    }

    class TaskResult(
        override val dependencies: List<org.jetbrains.amper.tasks.TaskResult>,
        val resourcesOutputRoot: Path,
        val module: PotatoModule,
        val isTest: Boolean,
    ) : org.jetbrains.amper.tasks.TaskResult

    private val logger = LoggerFactory.getLogger(javaClass)
}