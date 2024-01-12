package com.example.navidemo.ui.profile

import io.github.janmalch.navi.Screen

@Screen(
    path = "profile",
    args = ProfileScreen.Args::class,
)
object ProfileScreen {
    data class Args(
        val id: String,
        val name: String?,
        val bar: List<Int>,
    )
    // FIXME: generate extension function "argsFrom" for Bundle, BackStackEntry, SavedStateHandle..
}
