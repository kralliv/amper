/*
 * Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.amper.dependency.resolution

import org.junit.jupiter.api.Test
import org.junit.jupiter.api.io.TempDir
import java.io.File
import java.nio.file.Files
import java.nio.file.Path
import kotlin.io.path.copyTo
import kotlin.test.assertTrue

class DependencyFileTest {

    @field:TempDir
    lateinit var temp: File

    @Test
    fun `kotlin-test-1_9_20 module hash`() {
        val path = temp.toPath()
        val settings = Context.build {
            cache = {
                localRepositories = listOf(GradleLocalRepository(path))
            }
        }.settings
        val dependency = MavenDependency(settings.fileCache, "org.jetbrains.kotlin", "kotlin-test", "1.9.20")
        val extension = "module"
        val name = "${getNameWithoutExtension(dependency)}.$extension"
        val target = path.resolve(
            "${dependency.group}/${dependency.module}/${dependency.version}/3bf4b49eb37b4aca302f99bd99769f6e310bdb2/$name"
        )
        Files.createDirectories(target.parent)
        Path.of("testData/metadata/json/$name").copyTo(target)

        val dependencyFile = DependencyFile(dependency, getNameWithoutExtension(dependency), extension)
        assertTrue(dependencyFile.path!!.startsWith(path))

        val downloaded = dependencyFile.isDownloaded(ResolutionLevel.PARTIAL, settings)
        assertTrue(dependency.messages.isEmpty(), "There must be no messages: ${dependency.messages}")
        assertTrue(downloaded, "File must be downloaded as it was created above")
    }
}
