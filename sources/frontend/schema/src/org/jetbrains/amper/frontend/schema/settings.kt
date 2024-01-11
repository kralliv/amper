/*
 * Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.amper.frontend.schema

import org.jetbrains.amper.frontend.api.SchemaNode
import java.nio.file.Path

class Settings : SchemaNode() {
    var java by nullableValue<JavaSettings>()
    var jvm by nullableValue<JvmSettings>()
    var kotlin by value<KotlinSettings>().default(KotlinSettings())
    var android by nullableValue<AndroidSettings>()
    var compose by nullableValue<ComposeSettings>()
    var junit by nullableValue<String>() // TODO Replace with enum.
    var ios by nullableValue<IosSettings>()
    var publishing by nullableValue<PublishingSettings>()
    var kover by nullableValue<KoverSettings>()
    var native by nullableValue<NativeSettings>()
}

class JavaSettings : SchemaNode() {
    // TODO Replace with enum
    var source by nullableValue<String>()
}

class JvmSettings : SchemaNode() {
    // TODO Replace with enum
    var target by value<String>().default("17")
    var mainClass by nullableValue<String>()
}

class AndroidSettings : SchemaNode() {
    var compileSdk by nullableValue<String>()
    var minSdk by nullableValue<String>()
    var maxSdk by nullableValue<String>()
    var targetSdk by nullableValue<String>()
    var applicationId by nullableValue<String>()
    var namespace by nullableValue<String>()
}

class KotlinSettings : SchemaNode() {
    var serialization by nullableValue<SerializationSettings>()

    // TODO Replace with enum
    var languageVersion by value<String>().default("1.9")
    // TODO Replace with enum
    var apiVersion by nullableValue<String>()
    var allWarningsAsErrors by value<Boolean>().default(false)
    var freeCompilerArgs by nullableValue<List<String>>()
    var suppressWarnings by value<Boolean>().default(false)
    var verbose by value<Boolean>().default(false)
    var linkerOpts by nullableValue<List<String>>()
    var debug by value<Boolean>().default(false)
    var progressiveMode by value<Boolean>().default(false)
    // TODO Replace with enum
    var languageFeatures by nullableValue<List<String>>()
    // TODO Replace with enum
    var optIns by nullableValue<List<String>>()
}

class ComposeSettings : SchemaNode() {
    var enabled by value<Boolean>()
}

class SerializationSettings : SchemaNode() {
    var engine by value<String>().default("json")
}

class IosSettings : SchemaNode() {
    var teamId by nullableValue<String>()
    var framework by nullableValue<IosFrameworkSettings>()
}

class IosFrameworkSettings : SchemaNode() {
    var basename by nullableValue<String>()
    var isStatic by nullableValue<Boolean>().default(false)
}

class PublishingSettings : SchemaNode() {
    var group by nullableValue<String>()
    var version by nullableValue<String>()
}

class KoverSettings : SchemaNode() {
    var enabled by nullableValue<Boolean>().default(false)
    var xml by nullableValue<KoverXmlSettings>()
    var html by nullableValue<KoverHtmlSettings>()
}

class KoverXmlSettings : SchemaNode() {
    var onCheck by nullableValue<Boolean>()
    var reportFile by nullableValue<Path>()
}

class KoverHtmlSettings : SchemaNode() {
    var title by nullableValue<String>()
    var charset by nullableValue<String>()
    var onCheck by nullableValue<Boolean>()
    var reportDir by nullableValue<Path>()
}

class NativeSettings : SchemaNode() {
    // TODO other options from NativeApplicationPart
    var entryPoint by nullableValue<String>()
}