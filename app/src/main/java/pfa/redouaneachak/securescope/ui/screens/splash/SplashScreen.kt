package pfa.redouaneachak.securescope.ui.screens.splash

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import pfa.redouaneachak.securescope.R

@Composable
fun SplashScreen(onFinished: () -> Unit) {
    val logoScale = remember { Animatable(0.7f) }
    val logoAlpha = remember { Animatable(0f) }
    val titleAlpha = remember { Animatable(0f) }
    val subtitleAlpha = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        // Stage 1: logo fades + scales in together
        launch {
            logoScale.animateTo(1f, tween(600, easing = FastOutSlowInEasing))
        }
        launch {
            logoAlpha.animateTo(1f, tween(500))
        }
        delay(600) // let the logo settle before text starts

        // Stage 2: title fades in
        titleAlpha.animateTo(1f, tween(400))
        delay(200) // small stagger before subtitle, not simultaneous

        // Stage 3: subtitle fades in
        subtitleAlpha.animateTo(1f, tween(400))

        delay(1000) // hold everything on screen briefly before leaving
        onFinished()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF0F9B7E), Color(0xFF1565A8))
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Image(
                painter = painterResource(id = R.mipmap.ic_launcher_foreground),
                contentDescription = null,
                modifier = Modifier
                    .size(140.dp)
                    .scale(logoScale.value)
                    .alpha(logoAlpha.value)
            )

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "SECURE SCOPE",
                color = Color.White,
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 2.sp,
                modifier = Modifier.alpha(titleAlpha.value)
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Your Security Monitor",
                color = Color.White.copy(alpha = 0.8f),
                fontSize = 14.sp,
                modifier = Modifier.alpha(subtitleAlpha.value)
            )
        }
    }
}