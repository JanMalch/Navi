package com.example.navidemo.ui.profile

import com.github.janmalch.navi.Screen

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
}
