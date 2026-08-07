package pfa.redouaneachak.securescope

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import pfa.redouaneachak.securescope.ui.theme.SecureScopeTheme
import pfa.redouaneachak.securescope.ui.screens.splash.SplashScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            SecureScopeTheme {
                var showSplash by rememberSaveable { mutableStateOf(true) }

                if (showSplash) {
                    SplashScreen(onFinished = { showSplash = false })
                } else {
                    // your home screen composable goes here for now
                    Text("Home screen placeholder")
                }
            }
        }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    SecureScopeTheme {
        Greeting("Android")
    }
}