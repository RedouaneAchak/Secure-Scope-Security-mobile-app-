package pfa.redouaneachak.securescope.ui.screens.useragreement

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UserAgreementScreen(onBack: () -> Unit) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("User Agreement") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .verticalScroll(rememberScrollState())
                .padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            AgreementSection(
                title = "About Secure Scope",
                body = "Secure Scope is a security and monitoring application for Android. It analyzes installed apps, monitors device resources, and helps you stay aware of network activity on your device."
            )
            AgreementSection(
                title = "About The Developer",
                body = "This application was developed by Redouane Achak as a part of his internship in Inovext enterprise. For additional details or questions, contact this email: redouane.achak@enim.ac.ma"
            )
            AgreementSection(
                title = "Data collected on your device",
                body = "Secure Scope reads installed app information, permissions, and network activity directly on your device to perform its core features (scanning, monitoring, and network protection). This data is stored locally on your device and is not transmitted to any server operated by Secure Scope."
            )
            AgreementSection(
                title = "Cloud analysis",
                body = "When an app is flagged as potentially risky during a scan, Secure Scope may send that app's file hash to VirusTotal, a third-party malware analysis service, to verify its safety. No personal data is included in this request."
            )
            AgreementSection(
                title = "Network protection",
                body = "When enabled, Secure Scope routes eligible network traffic through a local proxy on your device to identify and block known harmful domains. This processing happens entirely on your device."
            )
            AgreementSection(
                title = "Your consent",
                body = "By using Secure Scope, you agree to the local processing described above. You can disable Network Protection and clear scan history at any time from within the app."
            )
        }
    }
}

@Composable
private fun AgreementSection(title: String, body: String) {
    Column {
        Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        Spacer(modifier = Modifier.height(6.dp))
        Text(body, fontSize = 13.sp, lineHeight = 20.sp)
    }
}