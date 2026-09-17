
package pfa.redouaneachak.securescope.ui.screens.onboarding

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Help
import androidx.compose.material.icons.filled.Memory
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material.icons.filled.SentimentVerySatisfied
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import kotlinx.coroutines.launch
import pfa.redouaneachak.securescope.ui.theme.SecureScopeColors

private val pages = listOf(

    // 1. Welcome
    OnboardingPage(
        icon = Icons.Filled.SentimentVerySatisfied,
        title = "Welcome to SecureScope",
        description = "SecureScope is your personal surveillance and security companion, designed to help you understand, monitor, and protect your phone."
    ),

    // 2. Surveillance
    OnboardingPage(
        icon = Icons.Filled.Visibility,
        title = "Know your phone",
        description = "SecureScope analyzes your device and provides useful information about your installed apps, their permissions and activity."
    ),

    // 3. Threat detection
    OnboardingPage(
        icon = Icons.Filled.Security,
        title = "Scan for threats",
        description = "Check your installed apps for suspicious behavior, trackers, dangerous permissions, and potential security threats using local analysis and cloud verification."
    ),

    // 4. Network protection
    OnboardingPage(
        icon = Icons.Filled.Wifi,
        title = "Browse safely",
        description = "Turn on Network Protection to help identify harmful websites and monitor the servers your applications communicate with."
    ),

    // 5. Device monitoring
    OnboardingPage(
        icon = Icons.Filled.Memory,
        title = "Monitor your device",
        description = "Keep track of important device information such as RAM, battery, storage, network activity, and data usage — all from one dashboard."
    ),

    // 6. Help
    OnboardingPage(
        icon = Icons.Filled.Help,
        title = "Need more information?",
        description = "For additional information about SecureScope, its features, and how your data is handled, please check our User Agreement or contact us."
    )
)

@Composable
fun OnboardingScreen(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = hiltViewModel()
) {
    val pagerState = rememberPagerState(pageCount = { pages.size })
    val scope = rememberCoroutineScope()

    Column(modifier = Modifier.fillMaxSize()) {

        // Skip button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.End
        ) {
            if (pagerState.currentPage < pages.lastIndex) {
                TextButton(
                    onClick = {
                        viewModel.completeOnboarding(onFinished)
                    }
                ) {
                    Text(
                        "Skip",
                        color = SecureScopeColors.Gray
                    )
                }
            }
        }

        // Pages
        HorizontalPager(
            state = pagerState,
            modifier = Modifier.weight(1f)
        ) { pageIndex ->
            OnboardingPageContent(pages[pageIndex])
        }

        // Page indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.Center
        ) {
            pages.indices.forEach { index ->
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(
                            if (index == pagerState.currentPage)
                                SecureScopeColors.Blue
                            else
                                SecureScopeColors.LightGray
                        )
                )
            }
        }

        // Next / Done button
        Button(
            onClick = {
                if (pagerState.currentPage < pages.lastIndex) {
                    scope.launch {
                        pagerState.animateScrollToPage(
                            pagerState.currentPage + 1
                        )
                    }
                } else {
                    viewModel.completeOnboarding(onFinished)
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = SecureScopeColors.Blue
            )
        ) {
            Text(
                if (pagerState.currentPage < pages.lastIndex)
                    "Next"
                else
                    "Get Started"
            )
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {

        // Icon
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(
                    SecureScopeColors.Blue.copy(alpha = 0.12f)
                ),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                tint = SecureScopeColors.Blue,
                modifier = Modifier.size(56.dp)
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Title
        Text(
            text = page.title,
            fontWeight = FontWeight.Bold,
            fontSize = 22.sp,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Description
        Text(
            text = page.description,
            fontSize = 14.sp,
            color = SecureScopeColors.Gray,
            textAlign = TextAlign.Center,
            lineHeight = 20.sp
        )
    }
}
