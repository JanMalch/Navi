package com.example.navidemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.SavedStateHandle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.navidemo.ui.profile.ProfileScreen
import com.example.navidemo.ui.profile.ProfileScreenUi
import com.example.navidemo.ui.profile.navigateToProfileScreen
import com.example.navidemo.ui.profile.profileScreen
import com.example.navidemo.ui.theme.NaviTheme
import com.example.navidemo.ui.welcome.WelcomeScreen
import com.example.navidemo.ui.welcome.WelcomeScreenUi
import com.example.navidemo.ui.welcome.welcomeScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            NaviTheme {
                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(
                        navController = navController,
                        startDestination = "WelcomeScreen.route",
                    ) {
                        welcomeScreen {
                            WelcomeScreenUi(onGoToProfile = {
                                navController.navigateToProfileScreen(
                                    ProfileScreen.Args(
                                        id = "ID",
                                        name = null,
                                        bar = listOf(42, 3012),
                                    )
                                )
                            })
                        }
                        profileScreen {
                            ProfileScreenUi(
                                text = it.arguments.toString() + "\n\n" + ProfileScreen.Args(
                                    id = it.arguments?.getString("id")!!,
                                    name = it.arguments?.getString("name"),
                                    bar = it.arguments?.getIntArray("bar")?.toList().orEmpty(),
                                ).toString()
                            )
                        }
                    }
                }
            }
        }
    }
}
