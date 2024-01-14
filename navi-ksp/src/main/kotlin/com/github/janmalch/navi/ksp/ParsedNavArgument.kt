package com.github.janmalch.navi.ksp

import com.google.devtools.ksp.symbol.KSPropertyDeclaration
import com.google.devtools.ksp.symbol.KSType

class ParsedNavArgument private constructor(
    val prop: KSPropertyDeclaration,
    /**
     * The type of this argument, which can be mapped to Android's `NavType`.
     */
    val navType: Type,
    val resolvedType: KSType,
) {
    /**
     * The plain name of the argument.
     */
    val name = prop.simpleName.asString()
    val isMarkedNullable = resolvedType.isMarkedNullable

    /**
     * The name of the variable in generated code,
     * that will hold the name for the name of this argument.
     */
    val varNameInCode: String = "__ARG_NAME__$name"

    /**
     * Is `true` if and only if this argument is used in the query parameter place.
     */
    val isQueryParam: Boolean = navType.isArray || isMarkedNullable

    /**
     * Internal enum type that resembles Android's `NavType`s.
     */
    enum class Type(
        val isArray: Boolean,
    ) {
        String(false),
        StringArray(true),
        Bool(false),
        BoolArray(true),
        Int(false),
        IntArray(true),
        Float(false),
        FloatArray(true),
        Long(false),
        LongArray(true),
        // NResource(false),
    }

    companion object {
        fun of(prop: KSPropertyDeclaration): ParsedNavArgument {
            val resolvedType = prop.type.resolve()
            val isMarkedNullable = resolvedType.isMarkedNullable
            val parsedType = parsedNavArgumentType(resolvedType)
            if (isMarkedNullable) {
                require(parsedType == Type.String) {
                    "Only String arguments may be nullable. ${prop.parent} > $prop"
                }
            }
            return ParsedNavArgument(
                prop = prop,
                navType = parsedType,
                resolvedType = resolvedType,
            )
        }
    }
}

val ParsedNavArgument.isList: Boolean
    // TODO: improve implementation (isAssignableFrom)
    get() = requireNotNull(resolvedType.declaration.qualifiedName?.asString()) {
    "Unable to infer NavType for property for unknown reasons."
} == List::class.qualifiedName

/**
 * Android's equivalent for this internal NavType representation,
 * e.g. `"NavType.StringType" == ParsedNavArgument.Type.String.androidEquivName`
 */
val ParsedNavArgument.Type.androidEquivTypeName: String
    get() = "NavType." + when(this) {
        ParsedNavArgument.Type.String -> "StringType"
        ParsedNavArgument.Type.StringArray -> "StringArrayType"
        ParsedNavArgument.Type.Bool -> "BoolType"
        ParsedNavArgument.Type.BoolArray -> "BoolArrayType"
        ParsedNavArgument.Type.Int -> "IntType"
        ParsedNavArgument.Type.IntArray -> "IntArrayType"
        ParsedNavArgument.Type.Float -> "FloatType"
        ParsedNavArgument.Type.FloatArray -> "FloatArrayType"
        ParsedNavArgument.Type.Long -> "LongType"
        ParsedNavArgument.Type.LongArray -> "LongArrayType"
    }

private fun parsedNavArgumentTypePrimitive(fqn: String): ParsedNavArgument.Type? {
    return when (fqn) {
        String::class.qualifiedName -> ParsedNavArgument.Type.String
        Boolean::class.qualifiedName -> ParsedNavArgument.Type.Bool
        Float::class.qualifiedName -> ParsedNavArgument.Type.Float
        Long::class.qualifiedName -> ParsedNavArgument.Type.Long
        Int::class.qualifiedName -> ParsedNavArgument.Type.Int // TODO: support Type.Resource
        else -> null
    }
}

private fun parsedNavArgumentType(type: KSType): ParsedNavArgument.Type {
    val fqn = requireNotNull(type.declaration.qualifiedName?.asString()) {
        "Unable to infer NavType for property for unknown reasons."
    }

    val primitive = parsedNavArgumentTypePrimitive(fqn)
    if (primitive != null) return primitive

    return when (fqn) {
        BooleanArray::class.qualifiedName -> ParsedNavArgument.Type.BoolArray
        FloatArray::class.qualifiedName -> ParsedNavArgument.Type.FloatArray
        LongArray::class.qualifiedName -> ParsedNavArgument.Type.LongArray
        IntArray::class.qualifiedName -> ParsedNavArgument.Type.IntArray
        List::class.qualifiedName -> {
            val argType = type.arguments.single()
            val listItemFqn = argType.type?.resolve()?.declaration?.qualifiedName?.asString() ?: ""
            val listItemType = requireNotNull(parsedNavArgumentTypePrimitive(listItemFqn)) {
                "Failed to infer item type from List for '$fqn'."
            }
            when (listItemType) {
                ParsedNavArgument.Type.String -> ParsedNavArgument.Type.StringArray
                ParsedNavArgument.Type.Bool -> ParsedNavArgument.Type.BoolArray
                ParsedNavArgument.Type.Int -> ParsedNavArgument.Type.IntArray
                ParsedNavArgument.Type.Float -> ParsedNavArgument.Type.FloatArray
                ParsedNavArgument.Type.Long -> ParsedNavArgument.Type.LongArray
                else -> throw IllegalArgumentException("Cannot use '$fqn' for list items. List types must match array types.")
            }
        }

        else -> throw IllegalArgumentException("Cannot map type '$fqn' to a NavType.")
    }
}

