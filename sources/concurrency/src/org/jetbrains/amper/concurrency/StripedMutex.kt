/*
 * Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.amper.concurrency

import kotlinx.coroutines.sync.Mutex

// initially from intellij:community/platform/build-scripts/downloader/src/striped.kt

private const val MAX_POWER_OF_TWO = 1 shl Integer.SIZE - 2
private const val ALL_SET = 0.inv()

class StripedMutex(stripeCount: Int = 64) {
    private val mask = if (stripeCount > MAX_POWER_OF_TWO) ALL_SET else (1 shl (Integer.SIZE - Integer.numberOfLeadingZeros(stripeCount - 1))) - 1
    private val locks = Array(mask + 1) { Mutex() }

    fun getLock(hash: Int): Mutex {
        return locks[hash and mask]
    }
}
