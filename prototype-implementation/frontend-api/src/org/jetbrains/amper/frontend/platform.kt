/*
 * Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.amper.frontend

/**
 * Enum, that describes the concrete platform the sources are built for.
 *
 * [parent] — Parent of the platform in natural KMP hierarchy.
 * [isLeaf] — Helper flag to indicate that the platform is a leaf in the hierarchy.
 */
enum class Platform(val parent: Platform? = null, val isLeaf: Boolean = false) {
    COMMON,

    JS(COMMON, isLeaf = true),
    JVM(COMMON, isLeaf = true),
    WASM(COMMON, isLeaf = true),
    ANDROID(COMMON, isLeaf = true),
    NATIVE(COMMON),

    LINUX(NATIVE),
    LINUX_X64(LINUX, isLeaf = true),
    LINUX_ARM64(LINUX, isLeaf = true),

    APPLE(NATIVE),

    TVOS(APPLE),
    TVOS_ARM64(TVOS, isLeaf = true),
    TVOS_X64(TVOS, isLeaf = true),
    TVOS_SIMULATOR_ARM64(TVOS, isLeaf = true),

    MACOS(APPLE),
    MACOS_X64(MACOS, isLeaf = true),
    MACOS_ARM64(MACOS, isLeaf = true),

    IOS(APPLE),
    IOS_ARM64(IOS, isLeaf = true),
    IOS_SIMULATOR_ARM64(IOS, isLeaf = true),
    IOS_X64(IOS, isLeaf = true),

    WATCHOS(APPLE),
    WATCHOS_ARM64(WATCHOS, isLeaf = true),
    WATCHOS_ARM32(WATCHOS, isLeaf = true),
    WATCHOS_DEVICE_ARM64(WATCHOS, isLeaf = true),
    WATCHOS_SIMULATOR_ARM64(WATCHOS, isLeaf = true),

    MINGW(NATIVE),
    MINGW_X64(MINGW, isLeaf = true),

    ANDROID_NATIVE(NATIVE),
    ANDROID_NATIVE_ARM32(ANDROID_NATIVE, isLeaf = true),
    ANDROID_NATIVE_ARM64(ANDROID_NATIVE, isLeaf = true),
    ANDROID_NATIVE_X64(ANDROID_NATIVE, isLeaf = true),
    ANDROID_NATIVE_X86(ANDROID_NATIVE, isLeaf = true), ;

    companion object : EnumMap<Platform, String>(Platform::values, Platform::pretty, Platform::class) {
        val leafPlatforms: Set<Platform> = entries.filterTo(mutableSetOf()) { it.isLeaf }

        /**
         * Parent-child relations throughout parent hierarchy for every leaf child.
         * For example, MACOS/MACOS_X64 and APPLE/MACOS_X64 are both present.
         * Excluding COMMON platform.
         */
        val naturalHierarchy: Map<Platform, Set<Platform>> = buildMap<Platform, MutableSet<Platform>> {
            // Add parent-child relation for every parent in hierarchy.
            fun add(parent: Platform?, child: Platform): Unit = if (parent != null && parent != COMMON) {
                this[parent] = (this[parent] ?: mutableSetOf()).apply { add(child) }
                add(parent.parent, child)
            } else Unit
            Platform.leafPlatforms.forEach { add(it.parent, it) }
        }

        /**
         * Get leaf children of this parent if it is not a parent, and list of self if it is.
         */
        val Platform.leafChildren get(): Set<Platform> =
            if (isLeaf) setOf(this)
            else naturalHierarchy[this]!! // here we must have some.
    }
}

fun Platform.isParent(possibleParent: Platform): Boolean =
    parent == possibleParent || parent?.isParent(possibleParent) ?: false

val Platform.pretty get() = name.doCamelCase()
val Platform.prettySuffix get() = pretty.doCapitalize()