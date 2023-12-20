package org.jetbrains.amper.frontend.schemaConverter.psi

/*
 * Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
 */

import org.jetbrains.amper.core.messages.ProblemReporterContext
import org.jetbrains.amper.frontend.Platform
import org.jetbrains.amper.frontend.schema.*
import org.jetbrains.amper.frontend.schemaConverter.asAbsolutePath
import org.jetbrains.yaml.psi.*

context(ProblemReporterContext)
public fun YAMLDocument.convertTemplate() = Template().apply {
  val documentMapping = getTopLevelValue()?.asMappingNode()!!
  convertBase(this)
}

context(ProblemReporterContext)
public fun YAMLDocument.convertModule() = Module().apply {
  val documentMapping = getTopLevelValue()?.asMappingNode()!!
  product(documentMapping.tryGetChildElement("product")?.convertProduct()) { /* TODO report */ }
  apply(documentMapping.tryGetScalarSequenceNode("apply")?.map { it.asAbsolutePath() /* TODO check path */ })
  aliases(documentMapping.tryGetMappingNode("aliases")
    ?.keyValues
    ?.mapNotNull {
      val alias = it.keyText
      val platforms = it.value?.asSequenceNode()?.asScalarSequenceNode()?.map { it.convertEnum(Platform) }?.toSet() ?: return@mapNotNull null
      alias to platforms
    }?.toMap()
  )
  convertBase(this)
}

context(ProblemReporterContext)
internal fun <T : Base> YAMLDocument.convertBase(base: T) = base.apply {
  val documentMapping = getTopLevelValue()?.asMappingNode()!!
  repositories(documentMapping.tryGetChildElement("repositories")?.convertRepositories())
  dependencies(documentMapping.convertWithModifiers("dependencies") { convertDependencies() })
  settings(documentMapping.convertWithModifiers("settings") { asMappingNode()?.convertSettings() })
  `test-dependencies`(documentMapping.convertWithModifiers("test-dependencies") { convertDependencies() })
  `test-settings`(documentMapping.convertWithModifiers("test-settings") { asMappingNode()?.convertSettings() })
}

context(ProblemReporterContext)
private fun YAMLKeyValue.convertProduct() = ModuleProduct().apply {
  val productMapping = value?.asMappingNode()
  type(productMapping?.tryGetScalarNode("type")?.convertEnum(ProductType)) { /* TODO report */ }
  platforms(productMapping?.tryGetScalarSequenceNode("platforms")
    ?.map { it.convertEnum(Platform) /* TODO report */ }
  )
}

context(ProblemReporterContext)
private fun YAMLPsiElement.convertRepositories(): List<Repository>? {
  if (this@convertRepositories !is YAMLSequence) return null
  // TODO Report wrong type.
  return items.mapNotNull { it.convertRepository() }
}

context(ProblemReporterContext)
private fun YAMLSequenceItem.convertRepository() = when (value) {
  is YAMLScalar -> Repository().apply {
    url((value as YAMLScalar).textValue)
    id((value as YAMLScalar).textValue)
  }

  is YAMLSequence -> Repository().apply {
    url((value as YAMLSequence).tryGetScalarNode("url")?.textValue) { /* TODO report */ }
    id((value as YAMLSequence).tryGetScalarNode("id")?.textValue)
    // TODO Report wrong type. Introduce helper for boolean maybe?
    publish((value as YAMLSequence).tryGetScalarNode("publish")?.textValue?.toBoolean())

    credentials(
      (value as YAMLSequence).tryGetChildNode("credentials")?.asMappingNode()?.let {
        Repository.Credentials().apply {
          // TODO Report non existent path.
          file(it.tryGetScalarNode("file")?.asAbsolutePath()) { /* TODO report */ }
          usernameKey(it.tryGetScalarNode("usernameKey")?.textValue) { /* TODO report */ }
          passwordKey(it.tryGetScalarNode("passwordKey")?.textValue) { /* TODO report */ }
        }
      }
    )
  }

  else -> null
}

context(ProblemReporterContext)
private fun YAMLValue.convertDependencies() = assertNodeType<YAMLSequence, List<Dependency>>("dependencies") {
  items.mapNotNull { it.convertDependency() }
}

context(ProblemReporterContext)
private fun YAMLSequenceItem.convertDependency(): Dependency? = when {
  this.value is YAMLScalar && this.value!!.text.startsWith(".") ->
    // TODO Report non existent path.
    this.value!!.text.let { InternalDependency().apply { path(it.asAbsolutePath()) } }

  this.value is YAMLScalar ->
    this.value!!.text.let { ExternalMavenDependency().apply { coordinates(it) } }
  this.value is YAMLPsiElement && getYAMLElements().size > 1 -> TODO("report")
  this.value is YAMLPsiElement && getYAMLElements().isEmpty() -> TODO("report")
  this.value is YAMLMapping && (this.value as YAMLMapping).keyValues.first()?.keyText?.startsWith(".") == true ->
    (this.value as YAMLMapping).keyValues.first().convertInternalDep()
  this.value is YAMLMapping && (this.value as YAMLMapping).keyValues.first().keyText != null ->
    (this.value as YAMLMapping).keyValues.first().convertExternalMavenDep()
  else -> null // Report wrong type
}

context(ProblemReporterContext)
private fun YAMLKeyValue.convertExternalMavenDep() = ExternalMavenDependency().apply {
  coordinates(keyText)
  adjustScopes(this)
}

context(ProblemReporterContext)
private fun YAMLKeyValue.convertInternalDep(): InternalDependency = InternalDependency().apply {
  path(keyText.asAbsolutePath())
  adjustScopes(this)
}

context(ProblemReporterContext)
private fun YAMLKeyValue.adjustScopes(dep: Dependency) = with(dep) {
  val valueNode = value
  when {
    valueNode is YAMLScalar && valueNode.textValue == "compile-only" -> `compile-only`(true)
    valueNode is YAMLScalar && valueNode.textValue == "runtime-only" -> `runtime-only`(true)
    valueNode is YAMLScalar && valueNode.textValue == "exported" -> exported(true)
    valueNode is YAMLMapping -> {
      `compile-only`(valueNode.tryGetScalarNode("compile-only")?.textValue?.toBoolean())
      `runtime-only`(valueNode.tryGetScalarNode("runtime-only")?.textValue?.toBoolean())
      exported(valueNode.tryGetScalarNode("exported")?.textValue?.toBoolean())
    }
  }
}
