/*
 * Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.amper.gradle.buildinfo

import org.eclipse.jgit.internal.storage.file.FileRepository
import org.gradle.api.tasks.WriteProperties
import org.jetbrains.amper.frontend.Platform
import org.jetbrains.amper.gradle.base.PluginPartCtx
import org.jetbrains.amper.gradle.base.SpecificPlatformPluginPart
import org.jetbrains.amper.gradle.contains

class BuildInfoBindingPluginPart(
    ctx: PluginPartCtx,
) : SpecificPlatformPluginPart(ctx, Platform.JVM) {
    override val needToApply: Boolean by lazy { Platform.JVM in module }

    override fun applyBeforeEvaluate() {
        val fragment = leafPlatformFragments.firstOrNull() ?: return
        val outputFile = fragment.settings.buildInfo?.file ?: return

        val generateBuildProperties =
            project.tasks.create("generateBuildProperties", WriteProperties::class.java) { config ->
                config.destinationFile.set(project.layout.buildDirectory.file(outputFile))

                config.comment = "Build info"
                config.property("version", project.version)

                // no git directory in bootstrap test
                if (project.findProperty("inBootstrapMode") != "true") {
                    val gitRoot = project.rootProject.projectDir.resolve(".git")
                    if (gitRoot.exists()) {
                        val git = FileRepository(gitRoot)
                        val head = git.getReflogReader("HEAD").lastEntry
                        val shortHash = git.newObjectReader().use { it.abbreviate(head.newId).name() }
                        config.property("commitHash", head.newId.name)
                        config.property("commitShortHash", shortHash)
                        config.property("commitDate", head.who.`when`.toInstant())
                    }
                }
            }

        project.tasks.named("jvmProcessResources", org.gradle.language.jvm.tasks.ProcessResources::class.java)
            .configure {
                it.dependsOn(generateBuildProperties)
                it.from(generateBuildProperties)
            }
    }
}