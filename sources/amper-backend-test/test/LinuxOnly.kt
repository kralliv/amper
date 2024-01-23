/*
 * Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import org.junit.jupiter.api.condition.EnabledIfSystemProperty

@Target(AnnotationTarget.CLASS, AnnotationTarget.FUNCTION)
@Retention(AnnotationRetention.RUNTIME)
@EnabledIfSystemProperty(named = "os.name", matches = "Linux.*")
annotation class LinuxOnly
