package com.example.navidemo.ui.profile

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun ProfileScreenUi(
    text: String,
    modifier: Modifier = Modifier,
) {
    Column {
        Text(text = "Profile")
        Text(text = text)
    }
}