package com.example.navidemo.ui.alltypes

import com.github.janmalch.navi.Screen

@Screen(
    path = "all-types",
    args = AllTypesScreen.Args::class,
)
object AllTypesScreen {
    data class Args(
        val string: String,
        val nullableString: String?,
        val boolean: Boolean,
        val int: Int,
        val float: Float,
        val long: Long,
        val booleanArray: BooleanArray,
        val intArray: IntArray,
        val floatArray: FloatArray,
        val longArray: LongArray,
        val booleanList: List<Boolean>,
        val intList: List<Int>,
        val floatList: List<Float>,
        val longList: List<Long>,
        val default: String? = "I'm a default value"
    )
}
