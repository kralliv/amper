/*
 * Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.amper.tasks.android

import com.android.ddmlib.IDevice
import com.android.ddmlib.Log
import com.android.instantapp.utils.LogcatService
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.isActive
import org.jetbrains.amper.engine.Task
import org.jetbrains.amper.engine.TaskName
import org.slf4j.LoggerFactory

class LogcatTask(override val taskName: TaskName) : Task {
    override suspend fun run(dependenciesResult: List<org.jetbrains.amper.tasks.TaskResult>): org.jetbrains.amper.tasks.TaskResult = coroutineScope {
        val deferred = CompletableDeferred<Boolean>()
        val device: IDevice = dependenciesResult
            .filterIsInstance<AndroidRunTask.TaskResult>()
            .firstOrNull()
            ?.device ?: return@coroutineScope TaskResult(dependenciesResult)
        val logcatService = LogcatService(device)
        logcatService.startListening { message ->
            if (!isActive) {
                deferred.complete(true)
            }
            message.header.logLevel?.let { logLevel ->
                when(logLevel) {
                    Log.LogLevel.VERBOSE -> logger.debug(message.toString())
                    Log.LogLevel.DEBUG -> logger.debug(message.toString())
                    Log.LogLevel.INFO -> logger.info(message.toString())
                    Log.LogLevel.WARN -> logger.warn(message.toString())
                    Log.LogLevel.ERROR -> logger.error(message.toString())
                    Log.LogLevel.ASSERT -> logger.debug(message.toString())
                }
            }
        }
        Runtime.getRuntime().addShutdownHook(Thread { deferred.complete(true) })
        deferred.await()
        logcatService.stopListening()
        TaskResult(dependenciesResult)
    }

    data class TaskResult(override val dependencies: List<org.jetbrains.amper.tasks.TaskResult>) :
        org.jetbrains.amper.tasks.TaskResult

    private val logger = LoggerFactory.getLogger(javaClass)
}
