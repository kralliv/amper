/*
 * Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.amper.compilation

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import java.nio.file.Path

internal data class CompilerPlugin(
    /**
     * The plugin ID used to associate arguments with the corresponding plugin.
     * It is exposed by each plugin's implementation in their `CommandLineProcessor.pluginId` property.
     */
    val id: String,
    val jarPath: Path,
    val options: Map<String, String> = emptyMap(),
) {
    companion object {
        fun serialization(jarPath: Path) = CompilerPlugin(
            id = "org.jetbrains.kotlinx.serialization",
            jarPath = jarPath,
        )

        fun compose(jarPath: Path) = CompilerPlugin(
            id = "androidx.compose.compiler.plugins.kotlin",
            jarPath = jarPath,
        )
    }
}

internal suspend fun KotlinCompilerDownloader.downloadCompilerPlugins(
    kotlinVersion: String,
    kotlinUserSettings: KotlinUserSettings,
): List<CompilerPlugin> = coroutineScope {
    buildList {
        if (kotlinUserSettings.serializationEnabled) {
            val plugin = async {
                CompilerPlugin.serialization(downloadKotlinSerializationPlugin(kotlinVersion))
            }
            add(plugin)
        }
        if (kotlinUserSettings.composeEnabled) {
            val plugin = async {
                CompilerPlugin.compose(downloadKotlinComposePlugin(kotlinVersion))
            }
            add(plugin)
        }
    }.awaitAll()
}
