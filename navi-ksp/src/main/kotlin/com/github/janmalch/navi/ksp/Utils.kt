package com.github.janmalch.navi.ksp

import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec

val KSClassDeclaration.fqn: String
    get() = checkNotNull(qualifiedName?.asString()) { "Qualified name of class must be determinable for $this." }

val KSPropertyDeclaration.fqn: String
    get() = checkNotNull(qualifiedName?.asString()) { "Qualified name of property must be determinable for $this." }

operator fun FileSpec.Builder.plusAssign(spec: PropertySpec) {
    addProperty(spec)
}

operator fun FileSpec.Builder.plusAssign(spec: FunSpec) {
    addFunction(spec)
}

operator fun FileSpec.Builder.plusAssign(spec: TypeSpec) {
    addType(spec)
}
