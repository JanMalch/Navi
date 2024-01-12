package io.github.janmalch.navi.ksp

import com.google.devtools.ksp.processing.CodeGenerator
import com.google.devtools.ksp.processing.KSPLogger
import com.google.devtools.ksp.processing.Resolver
import com.google.devtools.ksp.processing.SymbolProcessor
import com.google.devtools.ksp.processing.SymbolProcessorEnvironment
import com.google.devtools.ksp.processing.SymbolProcessorProvider
import com.google.devtools.ksp.symbol.ClassKind
import com.google.devtools.ksp.symbol.KSAnnotated
import com.google.devtools.ksp.symbol.KSAnnotation
import com.google.devtools.ksp.symbol.KSClassDeclaration
import com.google.devtools.ksp.symbol.KSType
import com.google.devtools.ksp.symbol.KSVisitorVoid
import com.squareup.kotlinpoet.ksp.toTypeName
import io.github.janmalch.navi.ksp.generator.GeneratorVisitor

class NaviProcessor(
    private val codeGenerator: CodeGenerator,
    private val options: Map<String, String>,
    private val logger: KSPLogger,
) : SymbolProcessor {
    override fun process(resolver: Resolver): List<KSAnnotated> {
        val symbols = resolver.getSymbolsWithAnnotation(SCREEN_FQN)

        val funNamePrefix = options.getOrDefault("naviExtFunNavigatePrefix", "navigateTo")
        val tryFunNamePrefix = options.getOrDefault("naviExtFunTryNavigatePrefix", "tryNavigateTo")
        val funNameSuffix = options.getOrDefault("naviExtFunNavigateSuffix", "")
        val tryFunNameSuffix = options.getOrDefault("naviExtFunTryNavigateSuffix", "")
        symbols.forEach {
            it.accept(LogVisitor(), Unit)
            it.accept(
                visitor = GeneratorVisitor(
                    codeGenerator = codeGenerator,
                    funNamePrefix = funNamePrefix,
                    tryFunNamePrefix = tryFunNamePrefix,
                    funNameSuffix = funNameSuffix,
                    tryFunNameSuffix = tryFunNameSuffix,
                ),
                data = Unit
            )
        }
        return emptyList()
    }

    inner class LogVisitor : NaviVisitor() {
        override fun handle() {
            logger.info(message = "- \"$screenPath\" -> $screenClass")
        }
    }
}

abstract class NaviVisitor : KSVisitorVoid() {

    protected lateinit var screenClass: KSClassDeclaration
    protected lateinit var screenPath: String
    protected lateinit var screenArgClass: KSClassDeclaration
    protected lateinit var screenArgs: Sequence<ParsedNavArgument>

    protected val packageName by lazy { screenClass.packageName.asString() }
    protected val screenObjectName by lazy { screenClass.simpleName.asString() }
    protected val screenClassAsReceiver by lazy { screenClass.asType(emptyList()).toTypeName() }

    override fun visitAnnotated(annotated: KSAnnotated, data: Unit) {
        val classDeclaration = requireNotNull(annotated as? KSClassDeclaration) {
            "@Screen annotation must be defined on Kotlin objects, but found on $annotated."
        }
        visitClassDeclaration(classDeclaration, data)
    }

    override fun visitClassDeclaration(classDeclaration: KSClassDeclaration, data: Unit) {
        require(classDeclaration.classKind == ClassKind.OBJECT) {
            "@Screen annotation must be defined on Kotlin objects, but found on $classDeclaration."
        }
        screenClass = classDeclaration
        classDeclaration.annotations.forEach { it.accept(this, data) }
        handle()
    }

    override fun visitAnnotation(annotation: KSAnnotation, data: Unit) {
        if (annotation.annotationType.resolve().declaration.qualifiedName?.asString() != SCREEN_FQN) return
        val pathArg = annotation.arguments.first { it.name?.asString() == "path" }
        screenPath =
            requireNotNull(pathArg.value?.toString()) { "'path' argument must be present in @Screen annotation." }
        val argsArg = annotation.arguments.first { it.name?.asString() == "args" }

        screenArgClass =
            (requireNotNull(argsArg.value as? KSType) { "'args' argument must be present in @Screen annotation. Can default to 'Unit::class'." })
                .declaration as KSClassDeclaration
        screenArgs = screenArgClass.getAllProperties()
            .map(ParsedNavArgument::of)
    }

    protected abstract fun handle()
}

class NaviProcessorProvider : SymbolProcessorProvider {
    override fun create(
        environment: SymbolProcessorEnvironment
    ): SymbolProcessor {
        return NaviProcessor(environment.codeGenerator, environment.options, environment.logger)
    }
}
