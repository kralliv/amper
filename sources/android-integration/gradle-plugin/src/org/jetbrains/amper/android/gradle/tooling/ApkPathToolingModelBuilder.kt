/*
 * Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.amper.android.gradle.tooling

import org.jetbrains.amper.android.AndroidBuildResult
import org.jetbrains.amper.android.ApkPathAndroidBuildResult
import com.android.build.gradle.api.BaseVariant


data class AndroidBuildResultImpl(override val paths: List<String>) : ApkPathAndroidBuildResult

class ApkPathToolingModelBuilder : BaseAndroidToolingModelBuilder() {
    override fun canBuild(modelName: String): Boolean = ApkPathAndroidBuildResult::class.java.name == modelName

    override fun List<BaseVariant>.getArtifactsFromVariants(): List<String> = flatMap { it.outputs }
        .map { it.outputFile }
        .map { it.toString() }

    override fun buildResult(paths: List<String>): AndroidBuildResult = AndroidBuildResultImpl(paths)
}