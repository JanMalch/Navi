package com.example.navidemo.ui.welcome

import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier

@Composable
fun WelcomeScreenUi(
    onGoToProfile: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column {
        Text(text = "Welcome")
        Button(onClick = onGoToProfile) {
            Text(text = "Go to Profile")
        }
    }
}