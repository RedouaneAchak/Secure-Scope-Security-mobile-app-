package pfa.redouaneachak.securescope

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import dagger.hilt.android.AndroidEntryPoint
import pfa.redouaneachak.securescope.ui.screens.home.HomeScreen
import pfa.redouaneachak.securescope.ui.theme.SecureScopeTheme
import pfa.redouaneachak.securescope.ui.screens.splash.SplashScreen

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)

        setContent {
            SecureScopeTheme {
                Surface(modifier = Modifier.fillMaxSize()) {
                    var showSplash by rememberSaveable { mutableStateOf(true) }

                    if (showSplash) {
                        SplashScreen(onFinished = { showSplash = false })
                    } else {
                        HomeScreen()
                    }
                }
            }
        }
    }
}