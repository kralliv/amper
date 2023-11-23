/*
 * Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.amper.dependency.resolution

import org.intellij.lang.annotations.Language
import org.junit.jupiter.api.TestInfo
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class BuildGraphTest {

    @Test
    fun `org_jetbrains_kotlin kotlin-test 1_9_10`(testInfo: TestInfo) {
        doTest(
            testInfo,
            expected = """root
                |\--- org.jetbrains.kotlin:kotlin-test:1.9.10
                |     \--- org.jetbrains.kotlin:kotlin-stdlib:1.9.10
                |          +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.9.10
                |          \--- org.jetbrains:annotations:13.0
            """.trimMargin()
        )
    }

    @Test
    fun `org_jetbrains_kotlin kotlin-test 1_9_20`(testInfo: TestInfo) {
        doTest(
            testInfo,
            expected = """root
                |\--- org.jetbrains.kotlin:kotlin-test:1.9.20
                |     \--- org.jetbrains.kotlin:kotlin-stdlib:1.9.20
                |          \--- org.jetbrains:annotations:13.0
            """.trimMargin()
        )
    }

    @Test
    fun `org_jetbrains_kotlinx kotlinx-coroutines-core 1_6_4`(testInfo: TestInfo) {
        doTest(
            testInfo,
            expected = """root
                |\--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4
                |     \--- org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4
                |          +--- org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4
                |          +--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.6.21
                |          |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.6.21
                |          |    |    +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.6.21
                |          |    |    \--- org.jetbrains:annotations:13.0
                |          |    \--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.6.21
                |          |         \--- org.jetbrains.kotlin:kotlin-stdlib:1.6.21 (*)
                |          \--- org.jetbrains.kotlin:kotlin-stdlib-common:1.6.21
            """.trimMargin()
        )
    }

    @Test
    fun `org_jetbrains_skiko skiko 0_7_85`(testInfo: TestInfo) {
        doTest(
            testInfo,
            repositories = REDIRECTOR_MAVEN2 + "https://cache-redirector.jetbrains.com/maven.pkg.jetbrains.space/public/p/compose/dev",
            emptyMessages = false, // skiko
            expected = """root
                |\--- org.jetbrains.skiko:skiko:0.7.85
                |     +--- org.jetbrains.skiko:skiko-android:0.7.85
                |     |    \--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20
                |     |         +--- org.jetbrains.kotlin:kotlin-stdlib:1.8.20
                |     |         |    +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.8.20
                |     |         |    \--- org.jetbrains:annotations:13.0
                |     |         \--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.20
                |     |              \--- org.jetbrains.kotlin:kotlin-stdlib:1.8.20 (*)
                |     \--- org.jetbrains.skiko:skiko-awt:0.7.85
                |          \--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20 (*)
            """.trimMargin()
        )
    }

    /**
     * TODO: org.jetbrains.skiko:skiko-android:0.7.85 should be absent for pure JVM
     */
    @Test
    fun `org_jetbrains_compose_desktop desktop-jvm-macos-arm64 1_5_10`(testInfo: TestInfo) {
        doTest(
            testInfo,
            repositories = REDIRECTOR_MAVEN2 + "https://cache-redirector.jetbrains.com/maven.pkg.jetbrains.space/public/p/compose/dev",
            emptyMessages = false, // skiko
            expected = """root
                |\--- org.jetbrains.compose.desktop:desktop-jvm-macos-arm64:1.5.10
                |     \--- org.jetbrains.compose.desktop:desktop:1.5.10
                |          \--- org.jetbrains.compose.desktop:desktop-jvm:1.5.10
                |               +--- org.jetbrains.compose.foundation:foundation:1.5.10
                |               |    \--- org.jetbrains.compose.foundation:foundation-desktop:1.5.10
                |               |         +--- org.jetbrains.compose.animation:animation:1.5.10
                |               |         |    \--- org.jetbrains.compose.animation:animation-desktop:1.5.10
                |               |         |         +--- org.jetbrains.compose.animation:animation-core:1.5.10
                |               |         |         |    \--- org.jetbrains.compose.animation:animation-core-desktop:1.5.10
                |               |         |         |         \--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4
                |               |         |         |              \--- org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:1.6.4
                |               |         |         |                   +--- org.jetbrains.kotlinx:kotlinx-coroutines-bom:1.6.4
                |               |         |         |                   +--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20
                |               |         |         |                   |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.8.20
                |               |         |         |                   |    |    +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.8.20
                |               |         |         |                   |    |    \--- org.jetbrains:annotations:13.0
                |               |         |         |                   |    \--- org.jetbrains.kotlin:kotlin-stdlib-jdk7:1.8.20
                |               |         |         |                   |         \--- org.jetbrains.kotlin:kotlin-stdlib:1.8.20 (*)
                |               |         |         |                   \--- org.jetbrains.kotlin:kotlin-stdlib-common:1.6.21 -> 1.8.20
                |               |         |         +--- org.jetbrains.compose.foundation:foundation-layout:1.5.10
                |               |         |         |    \--- org.jetbrains.compose.foundation:foundation-layout-desktop:1.5.10
                |               |         |         |         \--- org.jetbrains.compose.ui:ui:1.5.10
                |               |         |         |              \--- org.jetbrains.compose.ui:ui-desktop:1.5.10
                |               |         |         |                   +--- org.jetbrains.compose.runtime:runtime-saveable:1.5.10
                |               |         |         |                   |    \--- org.jetbrains.compose.runtime:runtime-saveable-desktop:1.5.10
                |               |         |         |                   |         \--- org.jetbrains.compose.runtime:runtime:1.5.10
                |               |         |         |                   |              \--- org.jetbrains.compose.runtime:runtime-desktop:1.5.10
                |               |         |         |                   |                   \--- org.jetbrains.kotlinx:kotlinx-coroutines-core:1.6.4 (*)
                |               |         |         |                   +--- org.jetbrains.compose.ui:ui-geometry:1.5.10
                |               |         |         |                   |    \--- org.jetbrains.compose.ui:ui-geometry-desktop:1.5.10
                |               |         |         |                   +--- org.jetbrains.compose.ui:ui-graphics:1.5.10
                |               |         |         |                   |    \--- org.jetbrains.compose.ui:ui-graphics-desktop:1.5.10
                |               |         |         |                   |         +--- org.jetbrains.compose.ui:ui-unit:1.5.10
                |               |         |         |                   |         |    \--- org.jetbrains.compose.ui:ui-unit-desktop:1.5.10
                |               |         |         |                   |         |         \--- org.jetbrains.compose.ui:ui-geometry:1.5.10 (*)
                |               |         |         |                   |         \--- org.jetbrains.skiko:skiko:0.7.85
                |               |         |         |                   |              +--- org.jetbrains.skiko:skiko-android:0.7.85
                |               |         |         |                   |              |    \--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20 (*)
                |               |         |         |                   |              \--- org.jetbrains.skiko:skiko-awt:0.7.85
                |               |         |         |                   |                   \--- org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.8.20 (*)
                |               |         |         |                   +--- org.jetbrains.compose.ui:ui-text:1.5.10
                |               |         |         |                   |    \--- org.jetbrains.compose.ui:ui-text-desktop:1.5.10
                |               |         |         |                   |         +--- org.jetbrains.compose.ui:ui-graphics:1.5.10 (*)
                |               |         |         |                   |         +--- org.jetbrains.compose.ui:ui-unit:1.5.10 (*)
                |               |         |         |                   |         \--- org.jetbrains.skiko:skiko:0.7.85 (*)
                |               |         |         |                   +--- org.jetbrains.compose.ui:ui-unit:1.5.10 (*)
                |               |         |         |                   \--- org.jetbrains.skiko:skiko:0.7.85 (*)
                |               |         |         +--- org.jetbrains.compose.runtime:runtime:1.5.10 (*)
                |               |         |         +--- org.jetbrains.compose.ui:ui:1.5.10 (*)
                |               |         |         \--- org.jetbrains.compose.ui:ui-geometry:1.5.10 (*)
                |               |         +--- org.jetbrains.compose.runtime:runtime:1.5.10 (*)
                |               |         +--- org.jetbrains.compose.ui:ui:1.5.10 (*)
                |               |         \--- org.jetbrains.skiko:skiko:0.7.85 (*)
                |               +--- org.jetbrains.compose.material:material:1.5.10
                |               |    \--- org.jetbrains.compose.material:material-desktop:1.5.10
                |               |         +--- org.jetbrains.compose.animation:animation-core:1.5.10 (*)
                |               |         +--- org.jetbrains.compose.foundation:foundation:1.5.10 (*)
                |               |         +--- org.jetbrains.compose.material:material-icons-core:1.5.10
                |               |         |    \--- org.jetbrains.compose.material:material-icons-core-desktop:1.5.10
                |               |         |         \--- org.jetbrains.compose.ui:ui:1.5.10 (*)
                |               |         +--- org.jetbrains.compose.material:material-ripple:1.5.10
                |               |         |    \--- org.jetbrains.compose.material:material-ripple-desktop:1.5.10
                |               |         |         +--- org.jetbrains.compose.foundation:foundation:1.5.10 (*)
                |               |         |         \--- org.jetbrains.compose.runtime:runtime:1.5.10 (*)
                |               |         +--- org.jetbrains.compose.runtime:runtime:1.5.10 (*)
                |               |         +--- org.jetbrains.compose.ui:ui:1.5.10 (*)
                |               |         \--- org.jetbrains.compose.ui:ui-text:1.5.10 (*)
                |               +--- org.jetbrains.compose.runtime:runtime:1.5.10 (*)
                |               +--- org.jetbrains.compose.ui:ui:1.5.10 (*)
                |               \--- org.jetbrains.compose.ui:ui-tooling-preview:1.5.10
                |                    \--- org.jetbrains.compose.ui:ui-tooling-preview-desktop:1.5.10
                |                         \--- org.jetbrains.compose.runtime:runtime:1.5.10 (*)
            """.trimMargin()
        )
    }

    @Test
    fun `androidx_annotation annotation 1_6_0`(testInfo: TestInfo) {
        doTest(
            testInfo,
            repositories = REDIRECTOR_MAVEN2 + "https://cache-redirector.jetbrains.com/maven.google.com",
            expected = """root
                |\--- androidx.annotation:annotation:1.6.0
                |     \--- androidx.annotation:annotation-jvm:1.6.0
                |          \--- org.jetbrains.kotlin:kotlin-stdlib:1.8.0
                |               +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.8.0
                |               \--- org.jetbrains:annotations:13.0
            """.trimMargin()
        )
    }

    @Test
    fun `androidx_activity activity-compose 1_7_2`(testInfo: TestInfo) {
        doTest(
            testInfo,
            platform = "android",
            repositories = REDIRECTOR_MAVEN2 + "https://cache-redirector.jetbrains.com/maven.google.com",
            expected = """root
                |\--- androidx.activity:activity-compose:1.7.2
                |     +--- androidx.activity:activity-ktx:1.7.2
                |     |    +--- androidx.activity:activity:1.7.2
                |     |    |    +--- androidx.annotation:annotation:1.1.0 -> 1.2.0
                |     |    |    +--- androidx.core:core:1.8.0
                |     |    |    |    +--- androidx.annotation:annotation:1.2.0
                |     |    |    |    +--- androidx.annotation:annotation-experimental:1.1.0
                |     |    |    |    +--- androidx.lifecycle:lifecycle-runtime:2.6.1
                |     |    |    |    |    +--- androidx.annotation:annotation:1.2.0
                |     |    |    |    |    +--- androidx.arch.core:core-common:2.2.0
                |     |    |    |    |    |    \--- androidx.annotation:annotation:1.2.0
                |     |    |    |    |    +--- androidx.lifecycle:lifecycle-common:2.6.1
                |     |    |    |    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.8.10
                |     |    |    |    |         +--- org.jetbrains.kotlin:kotlin-stdlib-common:1.8.10
                |     |    |    |    |         \--- org.jetbrains:annotations:13.0
                |     |    |    |    \--- androidx.versionedparcelable:versionedparcelable:1.1.1
                |     |    |    |         +--- androidx.annotation:annotation:1.2.0
                |     |    |    |         \--- androidx.collection:collection:1.0.0
                |     |    |    |              \--- androidx.annotation:annotation:1.2.0
                |     |    |    +--- androidx.lifecycle:lifecycle-runtime:2.6.1 (*)
                |     |    |    +--- androidx.lifecycle:lifecycle-viewmodel:2.6.1
                |     |    |    |    +--- androidx.annotation:annotation:1.1.0 -> 1.2.0
                |     |    |    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.8.10 (*)
                |     |    |    +--- androidx.lifecycle:lifecycle-viewmodel-savedstate:2.6.1
                |     |    |    |    +--- androidx.annotation:annotation:1.2.0
                |     |    |    |    +--- androidx.core:core-ktx:1.2.0
                |     |    |    |    |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.8.10 (*)
                |     |    |    |    |    +--- androidx.annotation:annotation:1.2.0
                |     |    |    |    |    \--- androidx.core:core:1.8.0 (*)
                |     |    |    |    +--- androidx.lifecycle:lifecycle-livedata-core:2.6.1
                |     |    |    |    |    +--- androidx.lifecycle:lifecycle-common:2.6.1
                |     |    |    |    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.8.10 (*)
                |     |    |    |    +--- androidx.lifecycle:lifecycle-viewmodel:2.6.1 (*)
                |     |    |    |    +--- androidx.savedstate:savedstate:1.2.1
                |     |    |    |    |    +--- androidx.annotation:annotation:1.2.0
                |     |    |    |    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.8.10 (*)
                |     |    |    |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.8.10 (*)
                |     |    |    |    \--- org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4
                |     |    |    +--- androidx.savedstate:savedstate:1.2.1 (*)
                |     |    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.8.10 (*)
                |     |    +--- androidx.core:core-ktx:1.1.0 -> 1.2.0 (*)
                |     |    +--- androidx.lifecycle:lifecycle-runtime-ktx:2.6.1
                |     |    |    +--- androidx.annotation:annotation:1.1.0 -> 1.2.0
                |     |    |    +--- androidx.lifecycle:lifecycle-runtime:2.6.1 (*)
                |     |    |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.8.10 (*)
                |     |    |    \--- org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4
                |     |    +--- androidx.lifecycle:lifecycle-viewmodel-ktx:2.6.1
                |     |    |    +--- androidx.lifecycle:lifecycle-viewmodel:2.6.1 (*)
                |     |    |    +--- org.jetbrains.kotlin:kotlin-stdlib:1.8.10 (*)
                |     |    |    \--- org.jetbrains.kotlinx:kotlinx-coroutines-android:1.6.4
                |     |    +--- androidx.savedstate:savedstate-ktx:1.2.1
                |     |    |    +--- androidx.savedstate:savedstate:1.2.1 (*)
                |     |    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.8.10 (*)
                |     |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.8.10 (*)
                |     +--- androidx.compose.runtime:runtime:1.0.1
                |     |    \--- org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0 -> 1.6.4
                |     +--- androidx.compose.runtime:runtime-saveable:1.0.1
                |     |    +--- androidx.compose.runtime:runtime:1.0.1 (*)
                |     |    \--- androidx.annotation:annotation:1.1.0 -> 1.2.0
                |     +--- androidx.compose.ui:ui:1.0.1
                |     |    +--- androidx.compose.runtime:runtime-saveable:1.0.1 (*)
                |     |    +--- androidx.compose.ui:ui-geometry:1.0.1
                |     |    |    \--- androidx.annotation:annotation:1.1.0 -> 1.2.0
                |     |    +--- androidx.compose.ui:ui-graphics:1.0.1
                |     |    |    +--- androidx.annotation:annotation:1.2.0
                |     |    |    \--- androidx.compose.ui:ui-unit:1.0.1
                |     |    |         +--- androidx.compose.ui:ui-geometry:1.0.1 (*)
                |     |    |         \--- androidx.annotation:annotation:1.2.0
                |     |    +--- androidx.compose.ui:ui-text:1.0.1
                |     |    |    +--- androidx.compose.ui:ui-graphics:1.0.1 (*)
                |     |    |    +--- androidx.compose.ui:ui-unit:1.0.1 (*)
                |     |    |    \--- androidx.annotation:annotation:1.2.0
                |     |    +--- androidx.compose.ui:ui-unit:1.0.1 (*)
                |     |    \--- androidx.annotation:annotation:1.1.0 -> 1.2.0
                |     \--- androidx.lifecycle:lifecycle-viewmodel:2.6.1 (*)
            """.trimMargin()
        )
    }

    @Test
    fun `org_tinylog slf4j-tinylog 2_7_0-M1`(testInfo: TestInfo) {
        doTest(
            testInfo,
            expected = """root
                |\--- org.tinylog:slf4j-tinylog:2.7.0-M1
                |     +--- org.slf4j:slf4j-api:2.0.9
                |     \--- org.tinylog:tinylog-api:2.7.0-M1
            """.trimMargin()
        )
    }

    @Test
    fun `org_tinylog tinylog-api 2_7_0-M1`(testInfo: TestInfo) {
        doTest(
            testInfo,
            expected = """root
                |\--- org.tinylog:tinylog-api:2.7.0-M1
            """.trimMargin()
        )
    }

    /**
     * TODO: org.jetbrains.kotlin:kotlin-test-junit:1.9.20 (*) is missing from org.jetbrains.kotlin:kotlin-test:1.9.20
     */
    @Test
    fun `kotlin test with junit`() {
        val root = Resolver.createFor({
            listOf(
                "org.jetbrains.kotlin:kotlin-stdlib:1.9.20",
                "org.jetbrains.kotlin:kotlin-test-junit:1.9.20",
                "org.jetbrains.kotlin:kotlin-test:1.9.20",
                "junit:junit:4.12",
            ).toRootNode(it)
        }) { repositories = REDIRECTOR_MAVEN2 }.buildGraph(ResolutionLevel.FULL).root
        assertEquals(
            """root
            |+--- org.jetbrains.kotlin:kotlin-stdlib:1.9.20
            ||    \--- org.jetbrains:annotations:13.0
            |+--- org.jetbrains.kotlin:kotlin-test-junit:1.9.20
            ||    +--- org.jetbrains.kotlin:kotlin-test:1.9.20
            ||    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.9.20 (*)
            ||    \--- junit:junit:4.13.2
            ||         \--- org.hamcrest:hamcrest-core:1.3
            |+--- org.jetbrains.kotlin:kotlin-test:1.9.20 (*)
            |\--- junit:junit:4.12 -> 4.13.2 (*)
        """.trimMargin(),
            root
        )
    }

    @Test
    fun `kotlin test with junit5`() {
        val root = Resolver.createFor({
            listOf(
                "org.jetbrains.kotlin:kotlin-test-junit5:1.9.20",
                "org.jetbrains.kotlin:kotlin-stdlib:1.9.20",
                "org.jetbrains.kotlin:kotlin-stdlib-common:1.9.20",
            ).toRootNode(it)
        }) { repositories = REDIRECTOR_MAVEN2 }.buildGraph(ResolutionLevel.FULL).root
        assertEquals(
            """root
            |+--- org.jetbrains.kotlin:kotlin-test-junit5:1.9.20
            ||    +--- org.jetbrains.kotlin:kotlin-test:1.9.20
            ||    |    \--- org.jetbrains.kotlin:kotlin-stdlib:1.9.20
            ||    |         \--- org.jetbrains:annotations:13.0
            ||    \--- org.junit.jupiter:junit-jupiter-api:5.6.3
            ||         +--- org.junit:junit-bom:5.6.3
            ||         +--- org.apiguardian:apiguardian-api:1.1.0
            ||         +--- org.opentest4j:opentest4j:1.2.0
            ||         \--- org.junit.platform:junit-platform-commons:1.6.3
            ||              +--- org.junit:junit-bom:5.6.3
            ||              \--- org.apiguardian:apiguardian-api:1.1.0
            |+--- org.jetbrains.kotlin:kotlin-stdlib:1.9.20 (*)
            |\--- org.jetbrains.kotlin:kotlin-stdlib-common:1.9.20
        """.trimMargin(),
            root
        )
    }

    /**
     * TODO: org.jetbrains.kotlin:kotlin-stdlib-common:1.7.0 has to be upgraded
     */
    @Test
    fun `datetime and kotlin test with junit`() {
        val root = Resolver.createFor({
            listOf(
                "org.jetbrains.kotlin:kotlin-stdlib:1.9.20",
                "org.jetbrains.kotlinx:kotlinx-datetime:0.4.0",
                "org.jetbrains.kotlin:kotlin-test:1.9.0",
                "org.jetbrains.kotlin:kotlin-test-junit:1.9.20",
                "junit:junit:4.12",
            ).toRootNode(it)
        }) { repositories = REDIRECTOR_MAVEN2 }.buildGraph(ResolutionLevel.FULL).root
        assertEquals(
            """root
            |+--- org.jetbrains.kotlin:kotlin-stdlib:1.9.20
            ||    \--- org.jetbrains:annotations:13.0
            |+--- org.jetbrains.kotlinx:kotlinx-datetime:0.4.0
            ||    \--- org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.4.0
            ||         +--- org.jetbrains.kotlin:kotlin-stdlib:1.9.20 (*)
            ||         \--- org.jetbrains.kotlin:kotlin-stdlib-common:1.7.0
            |+--- org.jetbrains.kotlin:kotlin-test:1.9.0 -> 1.9.20
            ||    \--- org.jetbrains.kotlin:kotlin-stdlib:1.9.20 (*)
            |+--- org.jetbrains.kotlin:kotlin-test-junit:1.9.20
            ||    +--- org.jetbrains.kotlin:kotlin-test:1.9.20 (*)
            ||    \--- junit:junit:4.13.2
            ||         \--- org.hamcrest:hamcrest-core:1.3
            |\--- junit:junit:4.12 -> 4.13.2 (*)
        """.trimMargin(),
            root
        )
    }

    @Test
    fun `jackson and guava`() {
        val root = Resolver.createFor({
            listOf(
                "org.antlr:antlr4-runtime:4.7.1",
                "org.abego.treelayout:org.abego.treelayout.core:1.0.3",
                "com.fasterxml.jackson.core:jackson-core:2.9.9",
                "com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.9.9",
                "org.apache.commons:commons-lang3:3.9",
                "commons-io:commons-io:2.6",
                "org.reflections:reflections:0.9.8",
                "javax.inject:javax.inject:1",
                "net.openhft:compiler:2.3.4",
            ).toRootNode(it)
        }) { repositories = REDIRECTOR_MAVEN2 }.buildGraph(ResolutionLevel.FULL).root
        assertEquals(
            """root
            |+--- org.antlr:antlr4-runtime:4.7.1
            |+--- org.abego.treelayout:org.abego.treelayout.core:1.0.3
            |+--- com.fasterxml.jackson.core:jackson-core:2.9.9
            |+--- com.fasterxml.jackson.jaxrs:jackson-jaxrs-json-provider:2.9.9
            ||    +--- com.fasterxml.jackson.jaxrs:jackson-jaxrs-base:2.9.9
            ||    |    +--- com.fasterxml.jackson.core:jackson-core:2.9.9
            ||    |    \--- com.fasterxml.jackson.core:jackson-databind:2.9.9
            ||    |         +--- com.fasterxml.jackson.core:jackson-annotations:2.9.0
            ||    |         \--- com.fasterxml.jackson.core:jackson-core:2.9.9
            ||    \--- com.fasterxml.jackson.module:jackson-module-jaxb-annotations:2.9.9
            ||         +--- com.fasterxml.jackson.core:jackson-annotations:2.9.0
            ||         +--- com.fasterxml.jackson.core:jackson-core:2.9.9
            ||         \--- com.fasterxml.jackson.core:jackson-databind:2.9.9 (*)
            |+--- org.apache.commons:commons-lang3:3.9
            |+--- commons-io:commons-io:2.6
            |+--- org.reflections:reflections:0.9.8
            ||    +--- com.google.guava:guava:11.0.2
            ||    |    \--- com.google.code.findbugs:jsr305:1.3.9
            ||    +--- javassist:javassist:3.12.1.GA
            ||    \--- dom4j:dom4j:1.6.1
            ||         \--- xml-apis:xml-apis:1.0.b2
            |+--- javax.inject:javax.inject:1
            |\--- net.openhft:compiler:2.3.4
            |     +--- org.slf4j:slf4j-api:1.7.25
            |     \--- com.intellij:annotations:12.0
        """.trimMargin(),
            root
        )
    }

    private fun doTest(
        testInfo: TestInfo,
        dependency: String = testInfo.nameToDependency(),
        scope: Scope = Scope.COMPILE,
        platform: String = "jvm",
        repositories: List<String> = REDIRECTOR_MAVEN2,
        emptyMessages: Boolean = true,
        @Language("text") expected: String
    ) {
        val root = Resolver.createFor({ dependency.toRootNode(it) }) {
            this.scope = scope
            this.platform = platform
            this.repositories = repositories
        }.buildGraph(ResolutionLevel.FULL).root
        assertEquals(expected, root)
        if (emptyMessages) {
            val messages = root.asSequence().flatMap { it.messages }.distinct().toList()
            assertTrue(messages.isEmpty(), "There should be no messages: $messages")
        }
    }

    private fun assertEquals(@Language("text") expected: String, root: DependencyNode) =
        assertEquals(expected, root.prettyPrint().trimEnd())

    companion object {
        private val REDIRECTOR_MAVEN2 = listOf("https://cache-redirector.jetbrains.com/repo1.maven.org/maven2")
    }
}

private fun String.toRootNode(resolver: Resolver) =
    ModuleDependencyNode("root", listOf(toMavenNode(resolver)))

private fun List<String>.toRootNode(resolver: Resolver) =
    ModuleDependencyNode("root", map { it.toMavenNode(resolver) })

private fun String.toMavenNode(resolver: Resolver): MavenDependencyNode {
    val (group, module, version) = split(":")
    return MavenDependencyNode(resolver, group, module, version)
}
