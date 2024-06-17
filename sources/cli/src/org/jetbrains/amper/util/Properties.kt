/*
 * Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.amper.util

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.nio.file.Path
import java.util.*
import kotlin.io.path.outputStream

internal suspend fun writeProperties(
    path: Path,
    comment: String? = null,
    builder: Properties.() -> Unit
) = withContext(Dispatchers.IO) {
    val properties = Properties()
    properties.builder()
    path.outputStream().buffered().use { properties.store(it, comment) }
}

internal fun Properties.property(key: String, value: Any?){
    if (value!=null){
        put(key, value.toString())
    }else {
        remove(key)
    }
}
