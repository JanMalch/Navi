package io.github.janmalch.navi.ksp.generator

import com.google.devtools.ksp.processing.CodeGenerator
import com.squareup.kotlinpoet.AnnotationSpec
import com.squareup.kotlinpoet.ClassName
import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.FileSpec
import com.squareup.kotlinpoet.FunSpec
import com.squareup.kotlinpoet.KModifier
import com.squareup.kotlinpoet.LambdaTypeName
import com.squareup.kotlinpoet.ParameterSpec
import com.squareup.kotlinpoet.ParameterizedTypeName.Companion.parameterizedBy
import com.squareup.kotlinpoet.PropertySpec
import com.squareup.kotlinpoet.TypeSpec
import com.squareup.kotlinpoet.UNIT
import com.squareup.kotlinpoet.asClassName
import com.squareup.kotlinpoet.buildCodeBlock
import com.squareup.kotlinpoet.ksp.toTypeName
import com.squareup.kotlinpoet.ksp.writeTo
import com.squareup.kotlinpoet.withIndent
import io.github.janmalch.navi.ksp.NAV_CONTROLLER_FQN
import io.github.janmalch.navi.ksp.NAV_GRAPH_BUILDER_COMPOSABLE_FQN
import io.github.janmalch.navi.ksp.NAV_GRAPH_BUILDER_FQN
import io.github.janmalch.navi.ksp.NaviVisitor
import io.github.janmalch.navi.ksp.ParsedNavArgument
import io.github.janmalch.navi.ksp.animatedContentScopeClass
import io.github.janmalch.navi.ksp.animatedContentTransitionScopeWithNavBackStackEntryGenericClass
import io.github.janmalch.navi.ksp.bundleClass
import io.github.janmalch.navi.ksp.composableAnnotation
import io.github.janmalch.navi.ksp.fqn
import io.github.janmalch.navi.ksp.jvmSuppressWildcardsAnnotation
import io.github.janmalch.navi.ksp.navBackStackEntryClass
import io.github.janmalch.navi.ksp.navControllerClass
import io.github.janmalch.navi.ksp.navDeepLinkClass
import io.github.janmalch.navi.ksp.navGraphBuilderClass
import io.github.janmalch.navi.ksp.navOptionsBuilderClass
import io.github.janmalch.navi.ksp.nullableEnterTransitionClass
import io.github.janmalch.navi.ksp.nullableExitTransitionClass
import io.github.janmalch.navi.ksp.nullableNavOptionsClass
import io.github.janmalch.navi.ksp.plusAssign
import io.github.janmalch.navi.ksp.savedStateHandleClass

private const val PATH_PRIVATE_VAR_NAME = "__PATH"
private const val PATH_PUBLIC_GETTER_NAME = "path"
private const val ROUTE_PRIVATE_VAR_NAME = "__ROUTE"
private const val ROUTE_PUBLIC_GETTER_NAME = "route"

class GeneratorVisitor(
    private val codeGenerator: CodeGenerator,
    private val funNamePrefix: String,
    private val tryFunNamePrefix: String,
    private val funNameSuffix: String,
    private val tryFunNameSuffix: String,
) : NaviVisitor() {
    /**
     * The name of the extension function on the `NavGraphBuilder`,
     * which will setup the `composable`.
     */
    private val navGraphBuilderExtFunName: String
        get() = screenObjectName.replaceFirstChar(Char::lowercaseChar)

    override fun handle() {
        FileSpec.builder(
            packageName = packageName,
            fileName = "${screenObjectName}Navi",
        )
            .addImport("androidx.compose.animation", "AnimatedContentScope")
            .addImport("androidx.compose.runtime", "Composable")
            .addImport(
                "androidx.navigation",
                "NavBackStackEntry",
                "NavController",
                "NavGraphBuilder",
                "NavOptions",
                "navArgument",
                "NavType",
            )
            .addImport("androidx.navigation.compose", "composable")
            .also(::writeConstants)
            .also(::writeNavGraphBuilderExt)
            .also(::writeNavControllerExt)
            .also(::writeArgParsers)
            .build()
            .writeTo(codeGenerator, false)
    }

    internal fun writeArgParsers(builder: FileSpec.Builder) {
        if (screenArgs.isEmpty()) return

        builder += FunSpec.builder("argsFrom")
            .addKdoc(
                "%L",
                """
                    Creates a new instance for the screen's arguments, based on the given [bundle].
                    
                    @throws IllegalArgumentException if the required arguments are missing 
                    @author Auto-generated with Navi
                """.trimIndent()
            )
            .receiver(screenClassAsReceiver)
            .addParameter("bundle", bundleClass)
            .returns(screenArgClass.asType(emptyList()).toTypeName())
            .addCode(buildCodeBlock {
                add("return %L(\n", screenArgClass.fqn)
                withIndent {
                    screenArgs.forEach { arg ->
                        add("%L = ", arg.name)
                        add(BundleFactory.getCode(arg))
                        addStatement(",")
                    }
                }
                add("\n)")
            })
            .build()

        builder += FunSpec.builder("argsFrom")
            .addKdoc(
                "%L",
                """
                    Creates a new instance for the screen's arguments, based on the given [savedStateHandle].
                    
                    @throws IllegalArgumentException if the required arguments are missing
                    @author Auto-generated with Navi
                """.trimIndent()
            )
            .receiver(screenClassAsReceiver)
            .addParameter("savedStateHandle", savedStateHandleClass)
            .returns(screenArgClass.asType(emptyList()).toTypeName())
            .addCode(buildCodeBlock {
                add("return %L(\n", screenArgClass.fqn)
                withIndent {
                    screenArgs.forEach { arg ->
                        add("%L = ", arg.name)
                        add(SavedStateHandleFactory.getCode(arg))
                        addStatement(",")
                    }
                }
                add(")")
            })
            .build()

        builder += FunSpec.builder("argsFrom")
            .addKdoc(
                "%L",
                """
                    Creates a new instance for the screen's arguments, based on the given [navBackStackEntry]'s `arguments`, if available.
                    
                    @throws IllegalArgumentException if the required arguments are missing
                    @see ${navBackStackEntryClass.canonicalName}.arguments
                    @author Auto-generated with Navi
                """.trimIndent()
            )
            .receiver(screenClassAsReceiver)
            .addParameter("navBackStackEntry", navBackStackEntryClass)
            .returns(screenArgClass.asType(emptyList()).toTypeName().copy(nullable = true))
            .addCode("return navBackStackEntry.arguments?.let(::argsFrom)")
            .build()
    }

    internal fun writeConstants(builder: FileSpec.Builder) {
        // add base path to file
        builder += PropertySpec.builder(
            PATH_PRIVATE_VAR_NAME, String::class, KModifier.PRIVATE, KModifier.CONST
        ).initializer("%S", screenPath)
            .addKdoc(
                "%L", """
                          The path defined by the [${screenClass.fqn}] class: `"$screenPath"`.
                          Base for building the [$screenClass.fqn.route].
                          
                          @see ${screenClass.fqn}.route
                          @see io.github.janmalch.navi.Screen.path
                          @author Auto-generated with Navi
                      """.trimIndent()
            )
            .build()
        // public getter for path
        builder += PropertySpec.builder(PATH_PUBLIC_GETTER_NAME, String::class)
            .receiver(screenClassAsReceiver)
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %L", PATH_PRIVATE_VAR_NAME)
                    .build()
            ).addKdoc(
                "%L", """
                        This screen's path: `"$screenPath"`
                        
                        @see ${screenClass.fqn}.$ROUTE_PUBLIC_GETTER_NAME
                        @see $NAV_GRAPH_BUILDER_FQN.$navGraphBuilderExtFunName
                        @author Auto-generated with Navi
                        """.trimIndent()
            ).build()


        // add args to file
        screenArgs.forEach { arg ->
            builder +=
                PropertySpec.builder(
                    arg.varNameInCode, String::class, KModifier.PRIVATE, KModifier.CONST
                ).initializer("%S", arg.name)
                    .addKdoc(
                        "%L", """
                                The name of the argument mapping to the [${arg.prop.fqn}] property: `"${arg.name}"`.
                                
                                @see io.github.janmalch.navi.Screen.args
                                @author Auto-generated with Navi
                            """.trimIndent()
                    )
                    .build()

        }

        // add private val for route pattern
        builder += PropertySpec.builder(
            ROUTE_PRIVATE_VAR_NAME, String::class, KModifier.PRIVATE, KModifier.CONST
        )
            .initializer("%L", '"' + screenRoute + '"')
            .build()

        // add public getter for route pattern
        builder += PropertySpec.builder(ROUTE_PUBLIC_GETTER_NAME, String::class)
            .receiver(screenClassAsReceiver)
            .getter(
                FunSpec.getterBuilder()
                    .addStatement("return %L", ROUTE_PRIVATE_VAR_NAME)
                    .build()
            ).addKdoc(
                "%L", """
                        This screen's route pattern: `"$screenRoutePretty"`
                        
                        @see ${screenClass.fqn}.$PATH_PUBLIC_GETTER_NAME
                        @see $NAV_GRAPH_BUILDER_FQN.$navGraphBuilderExtFunName
                        @author Auto-generated with Navi
                        """.trimIndent()
            ).build()
    }

    internal fun writeNavControllerExt(builder: FileSpec.Builder) {
        fun withOptions(funName: String, kdoc: String): FunSpec.Builder {
            return FunSpec.builder(funName)
                .receiver(navControllerClass)
                .addKdoc("%L", kdoc)
                .apply {
                    if (screenArgs.isNotEmpty()) {
                        addParameter(
                            ParameterSpec.builder(
                                "args",
                                screenArgClass.asStarProjectedType().toTypeName()
                            ).build()
                        )
                    }
                }
                .addParameter(
                    ParameterSpec.builder("navOptions", nullableNavOptionsClass)
                        .defaultValue("null")
                        .build()
                )
        }

        fun withBuilder(funName: String, kdoc: String): FunSpec.Builder {
            return FunSpec.builder(funName)
                .receiver(navControllerClass)
                .addKdoc("%L", kdoc)
                .apply {
                    if (screenArgs.isNotEmpty()) {
                        addParameter(
                            ParameterSpec.builder(
                                "args",
                                screenArgClass.asStarProjectedType().toTypeName()
                            ).build()
                        )
                    }
                }
                .addParameter(
                    "builder",
                    LambdaTypeName.get(
                        receiver = navOptionsBuilderClass,
                        returnType = UNIT
                    )
                )
        }

        val funName = "$funNamePrefix$screenObjectName$funNameSuffix".trim()
            .replaceFirstChar(Char::lowercaseChar)
        val tryFunName = "$tryFunNamePrefix$screenObjectName$tryFunNameSuffix".trim()
            .replaceFirstChar(Char::lowercaseChar)

        val kdoc = """
            Navigates to the destination defined by the [${screenClass.fqn}].
            
            @throws IllegalArgumentException if the given route is invalid
            @see $NAV_CONTROLLER_FQN.navigate
            @see $NAV_CONTROLLER_FQN.$tryFunName
            @see $NAV_GRAPH_BUILDER_FQN.$navGraphBuilderExtFunName
            @author Auto-generated with Navi
        """.trimIndent()

        val navStringVarName: String
        val codeThatBuildsNavString: CodeBlock

        if (screenArgs.isEmpty()) {
            navStringVarName = PATH_PRIVATE_VAR_NAME
            codeThatBuildsNavString = buildCodeBlock {  }
        } else {
            navStringVarName = "sb.toString()"
            codeThatBuildsNavString   = buildCodeBlock {
                addStatement("val sb = buildString {")

                withIndent {
                    addStatement("append(%L)", PATH_PRIVATE_VAR_NAME)


                    val (queryParams, pathParams) = screenArgs.partition(ParsedNavArgument::isQueryParam)

                    if (pathParams.isNotEmpty()) {
                        addStatement("if (!endsWith('/')) append('/')")
                        pathParams.forEachIndexed { idx, pp ->
                            addStatement("append(args.%L.toString())", pp.name)
                            if (idx < pathParams.lastIndex) {
                                addStatement("append('/')")
                            }
                        }
                    }

                    if (queryParams.isNotEmpty()) {
                        addStatement("append('?')")
                        queryParams.forEachIndexed { idx, qp ->
                            if (qp.isMarkedNullable) {
                                addStatement("if (args.%L != null) {", qp.name)
                                indent()
                            }
                            if (qp.navType.isArray) {
                                addStatement("args.%L.forEachIndexed { idx, av -> ", qp.name)
                                withIndent {
                                    addStatement("append(%L)", qp.varNameInCode)
                                    addStatement("append('=')")
                                    addStatement("append(%L)", "av")
                                    addStatement("if (idx < args.%L.lastIndex) append('&')", qp.name)
                                }
                                addStatement("}")
                            } else {
                                addStatement("append(%L)", qp.varNameInCode)
                                addStatement("append('=')")
                                addStatement("append(args.%L.toString())", qp.name)
                            }

                            if (idx < queryParams.lastIndex) {
                                addStatement("append('&')")
                            }
                            if (qp.isMarkedNullable) {
                                unindent()
                                addStatement("}", qp.name)
                            }
                        }

                        addStatement("if (endsWith('&')) deleteCharAt(lastIndex)")
                        addStatement("if (endsWith('?')) deleteCharAt(lastIndex)")
                    }
                }

                addStatement("}")
            }
        }


        builder += withOptions(funName, kdoc)
            .addCode(codeThatBuildsNavString)
            .addStatement(
                "this.navigate(%L, navOptions)", navStringVarName
            )
            .build()

        builder += withBuilder(funName, kdoc)
            .addCode(codeThatBuildsNavString)
            .addStatement(
                "this.navigate(%L, builder)", navStringVarName
            )
            .build()

        // catch variations
        val tryKdoc = """
            Navigates to the destination defined by the [${screenClass.fqn}].
            
            Catches any [IllegalArgumentException]s and returns `false`, indicating that navigation failed.
            Otherwise returns `true`, if navigation succeeded.
            
            @see $NAV_CONTROLLER_FQN.navigate
            @see $NAV_CONTROLLER_FQN.$funName
            @see $NAV_GRAPH_BUILDER_FQN.$navGraphBuilderExtFunName
            @return `true`, if and only if navigation succeeded
            @author Auto-generated with Navi
        """.trimIndent()

        builder += withOptions(tryFunName, tryKdoc)
            .returns(Boolean::class)
            .addCode(buildCodeBlock {
                addStatement("try {")
                withIndent {
                    add(codeThatBuildsNavString)
                    addStatement(
                        "this.navigate(%L, navOptions)", navStringVarName
                    )
                    addStatement("return true")
                }
                addStatement("} catch (e: IllegalArgumentException) {")
                withIndent {
                    addStatement("return false")
                }
                addStatement("}")
            })
            .build()

        builder += withBuilder(tryFunName, tryKdoc)
            .returns(Boolean::class)
            .addCode(buildCodeBlock {
                addStatement("try {")
                withIndent {
                    add(codeThatBuildsNavString)
                    addStatement(
                        "this.navigate(%L, builder)", navStringVarName
                    )
                    addStatement("return true")
                }
                addStatement("} catch (e: IllegalArgumentException) {")
                withIndent {
                    addStatement("return false")
                }
                addStatement("}")
            })
            .build()

    }

    internal fun writeNavGraphBuilderExt(builder: FileSpec.Builder) {
        if (skipNavGraphBuilderExt) return

        builder += FunSpec.builder(navGraphBuilderExtFunName)
            .receiver(navGraphBuilderClass)
            .addParameter(
                ParameterSpec.builder(
                    "deepLinks",
                    List::class.asClassName()
                        .parameterizedBy(navDeepLinkClass)
                )
                    .defaultValue("emptyList()").build()
            )
            // transitions
            .addParameter(
                buildTransitionParameter("enterTransition", true, "null")
            )
            .addParameter(
                buildTransitionParameter("exitTransition", false, "null")
            )
            .addParameter(
                buildTransitionParameter("popEnterTransition", true, "enterTransition")
            )
            .addParameter(
                buildTransitionParameter("popExitTransition", false, "exitTransition")
            )
            // compose content
            .addParameter(
                ParameterSpec.builder(
                    "content",
                    LambdaTypeName.get(
                        receiver = animatedContentScopeClass,
                        parameters = arrayOf(navBackStackEntryClass),
                        returnType = UNIT
                    ).copy(
                        annotations = listOf(
                            AnnotationSpec.builder(composableAnnotation).build()
                        )
                    )
                ).build()
            )
            .addCode(buildCodeBlock {
                addStatement("composable(")
                withIndent {
                    addStatement("route = %L,", ROUTE_PRIVATE_VAR_NAME)
                    addStatement("deepLinks = deepLinks,")
                    addStatement("enterTransition = enterTransition,")
                    addStatement("exitTransition = exitTransition,")
                    addStatement("popEnterTransition = popEnterTransition,")
                    addStatement("popExitTransition = popExitTransition,")
                    addStatement("arguments = listOf(")
                    withIndent {
                        screenArgs.forEach { arg ->
                            add("navArgument(%L) {\n", arg.varNameInCode)
                            withIndent {
                                addStatement(
                                    "type = %L", when (arg.navType) {
                                        ParsedNavArgument.Type.String -> "NavType.StringType"
                                        ParsedNavArgument.Type.StringArray -> "NavType.StringArrayType"
                                        ParsedNavArgument.Type.Bool -> "NavType.BoolType"
                                        ParsedNavArgument.Type.BoolArray -> "NavType.BoolArrayType"
                                        ParsedNavArgument.Type.Int -> "NavType.IntType"
                                        ParsedNavArgument.Type.IntArray -> "NavType.IntArrayType"
                                        ParsedNavArgument.Type.Float -> "NavType.FloatType"
                                        ParsedNavArgument.Type.FloatArray -> "NavType.FloatArrayType"
                                        ParsedNavArgument.Type.Long -> "NavType.LongType"
                                        ParsedNavArgument.Type.LongArray -> "NavType.LongArrayType"
                                    }
                                )
                                addStatement("nullable = %L", arg.isMarkedNullable)
                                // TODO: add support for default values via annotation on props
                            }
                            add("},\n")
                        }
                    }
                    addStatement("),")
                    addStatement("content = content,")
                }
                addStatement(")")
            }).addKdoc(
                "%L", """
                        Sets up the destination as defined by [${screenClass.fqn}].
                        
                        @see ${screenClass.fqn}.$ROUTE_PUBLIC_GETTER_NAME
                        @see $NAV_GRAPH_BUILDER_COMPOSABLE_FQN
                        @author Auto-generated with Navi
                        """.trimIndent()
            )
            .build()
    }
}

private fun buildTransitionParameter(
    name: String,
    enter: Boolean,
    defaultValue: String
): ParameterSpec {
    return ParameterSpec.builder(
        name,
        LambdaTypeName.get(
            receiver = animatedContentTransitionScopeWithNavBackStackEntryGenericClass,
            returnType = if (enter) nullableEnterTransitionClass else nullableExitTransitionClass
        ).copy(
            nullable = true,
            annotations = listOf(
                AnnotationSpec.builder(jvmSuppressWildcardsAnnotation).build()
            )
        )
    )
        .defaultValue(defaultValue)
        .build()
}
