package com.github.janmalch.navi.ksp

import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy

const val SCREEN_FQN = "com.github.janmalch.navi.Screen"
const val NAV_CONTROLLER_FQN = "androidx.navigation.NavController"
const val NAV_GRAPH_BUILDER_FQN = "androidx.navigation.NavGraphBuilder"
const val NAV_GRAPH_BUILDER_COMPOSABLE_FQN = "$NAV_GRAPH_BUILDER_FQN.composable"
const val COMPOSE_NAV_HOST_FQN = "androidx.navigation.compose.NavHost"

val savedStateHandleClass = ClassName("androidx.lifecycle", "SavedStateHandle")
val bundleClass = ClassName("android.os", "Bundle")
val navControllerClass = ClassName("androidx.navigation", "NavController")
val nullableNavOptionsClass = ClassName("androidx.navigation", "NavOptions").copy(nullable = true)
val navOptionsBuilderClass = ClassName("androidx.navigation", "NavOptionsBuilder")
val navGraphBuilderClass = ClassName("androidx.navigation", "NavGraphBuilder")
val navDeepLinkClass = ClassName("androidx.navigation", "NavDeepLink")
val composableAnnotation = ClassName("androidx.compose.runtime", "Composable")
val navBackStackEntryClass = ClassName("androidx.navigation", "NavBackStackEntry")
val animatedContentScopeClass =
    ClassName("androidx.compose.animation", "AnimatedContentScope")
val animatedContentTransitionScopeWithNavBackStackEntryGenericClass =
    ClassName("androidx.compose.animation", "AnimatedContentTransitionScope")
        .parameterizedBy(navBackStackEntryClass)
val nullableEnterTransitionClass =
    ClassName("androidx.compose.animation", "EnterTransition").copy(nullable = true)
val nullableExitTransitionClass =
    ClassName("androidx.compose.animation", "ExitTransition").copy(nullable = true)
val jvmSuppressWildcardsAnnotation =
    ClassName("kotlin.jvm", "JvmSuppressWildcards")