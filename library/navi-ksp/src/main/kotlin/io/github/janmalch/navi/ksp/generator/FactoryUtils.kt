package io.github.janmalch.navi.ksp.generator

import com.squareup.kotlinpoet.CodeBlock
import com.squareup.kotlinpoet.buildCodeBlock
import io.github.janmalch.navi.ksp.ParsedNavArgument
import io.github.janmalch.navi.ksp.isList


private fun emptyArrayFn(navType: ParsedNavArgument.Type): String {
    return when (navType) {
        ParsedNavArgument.Type.StringArray -> "emptyArray()"
        ParsedNavArgument.Type.BoolArray -> "booleanArrayOf()"
        ParsedNavArgument.Type.IntArray -> "intArrayOf()"
        ParsedNavArgument.Type.FloatArray -> "floatArrayOf()"
        ParsedNavArgument.Type.LongArray -> "longArrayOf()"
        else -> error("Cannot get emptyArray function for $navType.")
    }
}

internal object BundleFactory {
    fun getCode(argument: ParsedNavArgument): CodeBlock {
        return buildCodeBlock {
            var getCall = when (argument.navType) {
                ParsedNavArgument.Type.String -> "bundle.getString(${argument.varNameInCode})"
                ParsedNavArgument.Type.StringArray -> "bundle.getStringArray(${argument.varNameInCode})"
                ParsedNavArgument.Type.Bool -> "bundle.getBoolean(${argument.varNameInCode})"
                ParsedNavArgument.Type.BoolArray -> "bundle.getBooleanArray(${argument.varNameInCode})"
                ParsedNavArgument.Type.Int -> "bundle.getInt(${argument.varNameInCode})"
                ParsedNavArgument.Type.IntArray -> "bundle.getIntArray(${argument.varNameInCode})"
                ParsedNavArgument.Type.Float -> "bundle.getFloat(${argument.varNameInCode})"
                ParsedNavArgument.Type.FloatArray -> "bundle.getFloatArray(${argument.varNameInCode})"
                ParsedNavArgument.Type.Long -> "bundle.getLong(${argument.varNameInCode})"
                ParsedNavArgument.Type.LongArray -> "bundle.getLongArray(${argument.varNameInCode})"
            }
            getCall = when (argument.navType) {
                ParsedNavArgument.Type.Bool,
                ParsedNavArgument.Type.Int,
                ParsedNavArgument.Type.Float,
                ParsedNavArgument.Type.Long -> "(if (bundle.containsKey(${argument.varNameInCode})) { $getCall } else { null })"
                else -> getCall
            }

            add(getCall)

            if (argument.navType.isArray) {
                if (argument.isList) {
                    add("?.toList().orEmpty()")
                } else {
                    add("?: ${emptyArrayFn(argument.navType)}")
                }
            } else if (!argument.isMarkedNullable) {
                add("\n ?: throw IllegalArgumentException(\"'$%L' is required.\")", argument.varNameInCode)
            }
        }
    }
}

internal object SavedStateHandleFactory {
    fun getCode(argument: ParsedNavArgument): CodeBlock {
        return buildCodeBlock {
            val type = when (argument.navType) {
                ParsedNavArgument.Type.String -> "String"
                ParsedNavArgument.Type.StringArray -> "Array<String>"
                ParsedNavArgument.Type.Bool -> "Boolean"
                ParsedNavArgument.Type.BoolArray -> "BooleanArray"
                ParsedNavArgument.Type.Int -> "Int"
                ParsedNavArgument.Type.IntArray -> "IntArray"
                ParsedNavArgument.Type.Float -> "Float"
                ParsedNavArgument.Type.FloatArray -> "FloatArray"
                ParsedNavArgument.Type.Long -> "Long"
                ParsedNavArgument.Type.LongArray -> "LongArray"
            }
            add("savedStateHandle.get<$type>(${argument.varNameInCode})")

            if (argument.navType.isArray) {
                if (argument.isList) {
                    add("?.toList().orEmpty()")
                } else {
                    add(" ?: ${emptyArrayFn(argument.navType)}")
                }
            } else if (!argument.isMarkedNullable) {
                add("\n ?: throw IllegalArgumentException(\"'$%L' is required.\")", argument.varNameInCode)
            }
        }
    }
}