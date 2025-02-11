/*
 * Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

package org.jetbrains.amper.dependency.resolution

/**
 * Serves as a higher level holder for other dependency nodes.
 * It's statically defined, thus always resolved and has NO-OP implementations of the interface methods.
 * Its name must be unique to distinguish it from other `ModuleDependencyNode`s.
 * 
 * It's a responsibility of the caller to set a parent for this node if none was provided via the constructor.
 * 
 * @see [MavenDependencyNode]
 */
class ModuleDependencyNode(
    templateContext: Context,
    val name: String,
    override val children: List<DependencyNode>,
    parentNodes: List<DependencyNode> = emptyList(),
) : DependencyNode {

    init {
        children.forEach { it.context.nodeParents.add(this) }
    }

    override val context: Context = templateContext.copyWithNewNodeCache(parentNodes)
    override val key: Key<*> = Key<ModuleDependencyNode>(name)
    override val messages: List<Message> = listOf()

    override fun toString(): String = name

    override suspend fun resolveChildren(level: ResolutionLevel) {}

    override suspend fun downloadDependencies() {}
}
