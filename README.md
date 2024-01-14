# Navi <img src="/.idea/icon.png" width="90" height="90" align="right">

_Android library to reduce boilerplate code for official Compose navigation._

## Who is this for?

This is for you, if you want to follow [Google's Compose navigation guidelines](https://developer.android.com/jetpack/compose/navigation),
but find its boilerplate code rather annoying. This library does not lock you into another third-party navigation library (framework?),
but rather adds to what is already there. If there's something that Navi doesn't solve, you can still use Google's foundations.

## Usage

Define a destination with just an `object` and Navi's `Screen` annotation:

```kotlin
@Screen(path = "welcome")
object WelcomeScreen
```

Navi will automatically generate the following code for you:

```kotlin
val WelcomeScreen.path: String                 // the simple path, as specified in annotation
val WelcomeScreen.route: String                // the complex pattern with arguments
fun NavGraphBuilder.welcomeScreen(/*...*/)     // to setup the composable
fun NavController.navigateToWelcomeScreen()    // to navigate
fun NavController.tryNavigateToWelcomeScreen() // to navigate without risk of crashes
```

Navi also supports arguments.
It does not support `Serializable` or `Parcelable`, 
[as recommended by Google](https://developer.android.com/jetpack/compose/navigation#retrieving-complex-data).

```kotlin
@Screen(
    path = "profile",
    // args must be a data class, but doesn't have to be a nested class and name doesn't matter
    args = ProfileScreen.Args::class,
)
object ProfileScreen {
    data class Args(
        val id: String,
        val name: String?,
        val bar: List<Int>,
    )
}
```

Aside from properly adjusting the navigation extension functions, 
it will also generate factories to recreate the `Args` instance:

```kotlin
fun ProfileScreen.argsFrom(bundle: Bundle): ProfileScreen.Args
fun ProfileScreen.argsFrom(savedStateHandle: SavedStateHandle): ProfileScreen.Args
fun ProfileScreen.argsFrom(navBackStackEntry: NavBackStackEntry): ProfileScreen.Args?
```


See the [demo app](./app/src/main/java/com/example/navidemo) for a full example.

## Installation

Add the two dependencies to your app's `build.gradle.kts` file.
Optionally, you may configure some behaviour of Navi.

```kotlin
dependencies {
    implementation("io.github.janmalch:navi-runtime:$navi_version")
    ksp("io.github.janmalch:navi-ksp:$navi_version")
}

// optional, displayed values are defaults
ksp {
    arg("naviExtFunNavigatePrefix", "navigateTo")
    arg("naviExtFunNavigateSuffix", "")
    arg("naviExtFunTryNavigatePrefix", "tryNavigateTo")
    arg("naviExtFunTryNavigateSuffix", "")
}
```

## Credit

- Logo created with [IconKitchen](https://icon.kitchen/i/H4sIAAAAAAAAA02PwQ7CIAyG36VevajT6K7G%2BAB6Mx46WhiRjQlsi1n27pYlJl4g%2Fejfj04woOs5QjkBYXjda24YSo0u8hoqc%2F90UoIJSJbbBGvQ5uxshyHlSGS5gFhj7%2FKjVb4V0OJgDSYrxZwTF61ZJZFArJH8KJ2VudW4zI7v3gblWGDKdvrTX39ecSnvfNhIYIWFOuxO0r%2BgbUY7InUsBGFrZFRZ7GcxN556l5d7CKfgLeU%2F%2BijnyBU85y%2Bb3Bu2%2FwAAAA%3D%3D).
