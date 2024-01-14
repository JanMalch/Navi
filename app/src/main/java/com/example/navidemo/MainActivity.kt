package com.example.navidemo

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.example.navidemo.ui.alltypes.AllTypesScreen
import com.example.navidemo.ui.alltypes.allTypesScreen
import com.example.navidemo.ui.alltypes.argsFrom
import com.example.navidemo.ui.alltypes.navigateToAllTypesScreen
import com.example.navidemo.ui.profile.ProfileScreen
import com.example.navidemo.ui.profile.argsFrom
import com.example.navidemo.ui.profile.navigateToProfileScreen
import com.example.navidemo.ui.profile.profileScreen
import com.example.navidemo.ui.theme.NaviTheme
import com.example.navidemo.ui.welcome.WelcomeScreen
import com.example.navidemo.ui.welcome.WelcomeScreenUi
import com.example.navidemo.ui.welcome.route
import com.example.navidemo.ui.welcome.welcomeScreen
import java.util.UUID

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val uuid = UUID.randomUUID().toString()
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
                        startDestination = WelcomeScreen.route,
                    ) {
                        welcomeScreen {
                            WelcomeScreenUi(
                                onGoToProfile = {
                                    navController.navigateToProfileScreen(
                                        ProfileScreen.Args(
                                            id = uuid,
                                            name = null,
                                            bar = listOf(42, 3012),
                                        )
                                    )
                                },
                                onGoToAllTypes = {
                                    navController.navigateToAllTypesScreen(
                                        AllTypesScreen.Args(
                                            string = "all the examples",
                                            nullableString = "but im not null",
                                            boolean = true,
                                            int = 2353,
                                            float = 2.3f,
                                            long = 3296,
                                            booleanArray = booleanArrayOf(true, false, true),
                                            intArray = intArrayOf(1, 2, 3, 4, 5),
                                            floatArray = floatArrayOf(30.12f, 29.04f),
                                            longArray = longArrayOf(1987, 1962, 1961),
                                            booleanList = listOf(false),
                                            intList = listOf(591, 1238, 571, 21),
                                            floatList = listOf(19.7f),
                                            longList = listOf(80)
                                        )
                                    )
                                }
                            )
                        }
                        profileScreen {
                            Column {
                                Text(text = "Profile")
                                Text(
                                    text = it.arguments.toString() + "\n\n" + ProfileScreen.argsFrom(it)
                                )
                            }
                        }
                        allTypesScreen {
                            Column {
                                Text(text = "AllTypes")
                                Text(
                                    text = it.arguments.toString() + "\n\n" + AllTypesScreen.argsFrom(it)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}
