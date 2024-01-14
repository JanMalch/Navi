package io.github.janmalch.navi

import kotlin.reflect.KClass

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.SOURCE)
public annotation class Screen(
    val path: String,
    val args: KClass<*> = Unit::class,
    val skipNavGraphBuilderExt: Boolean = false,
)
