package pfa.redouaneachak.securescope.ui.screens.networkscan

import android.Manifest
import android.content.Intent
import android.net.VpnService
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pfa.redouaneachak.securescope.data.model.NetworkSessionWithApp
import pfa.redouaneachak.securescope.service.NetworkVpnService
import pfa.redouaneachak.securescope.ui.theme.SecureScopeColors
import pfa.redouaneachak.securescope.util.TimeFormatUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NetworkScanScreen(onBack: () -> Unit, viewModel: NetworkScanViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val blockedDomains by viewModel.blockedDomains.collectAsStateWithLifecycle()
    val context = LocalContext.current

    val vpnPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == android.app.Activity.RESULT_OK) {
            context.startService(Intent(context, NetworkVpnService::class.java))
        }
    }

    val notificationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { /* granted or not, the VPN starts regardless */ }

    fun startProtection() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            notificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }

        val consentIntent = VpnService.prepare(context)
        if (consentIntent != null) {
            vpnPermissionLauncher.launch(consentIntent)
        } else {
            context.startService(Intent(context, NetworkVpnService::class.java))
        }
    }
    val batteryOptimizationLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { /* result not critical — best-effort */ }

    fun requestBatteryOptimizationExemption() {
        val powerManager = context.getSystemService(android.os.PowerManager::class.java)
        if (!powerManager.isIgnoringBatteryOptimizations(context.packageName)) {
            val intent = Intent(android.provider.Settings.ACTION_REQUEST_IGNORE_BATTERY_OPTIMIZATIONS).apply {
                data = android.net.Uri.parse("package:${context.packageName}")
            }
            batteryOptimizationLauncher.launch(intent)
        }
    }
    fun stopProtection() {
        requestBatteryOptimizationExemption()
        val stopIntent = Intent(context, NetworkVpnService::class.java).apply {
            action = NetworkVpnService.ACTION_STOP
        }
        context.startService(stopIntent)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Network") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            MonitorToggleCard(
                isActive = uiState.isVpnActive,
                blockedCount = blockedDomains.size,
                onToggle = { if (uiState.isVpnActive) stopProtection() else startProtection() }
            )

            if (blockedDomains.isNotEmpty()) {
                Text("Blocked Attempts", fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp))
                LazyColumn(modifier = Modifier.heightIn(max = 150.dp), contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(blockedDomains, key = { "${it.timestamp}_${it.domain}" }) { blocked ->
                        Row(modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                            Column {
                                Text(blocked.domain, fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SecureScopeColors.DangerRed)
                                Text(blocked.category, fontSize = 11.sp, color = SecureScopeColors.Gray)
                            }
                            Text(TimeFormatUtil.formatRelativeTime(blocked.timestamp), fontSize = 11.sp, color = SecureScopeColors.Gray)
                        }
                    }
                }
            }

            Text(
                "Recently Contacted Servers",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )

            if (uiState.recentSessions.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(
                        if (uiState.isVpnActive) "No servers detected yet."
                        else "Start protection to see contacted servers.",
                        color = SecureScopeColors.Gray, fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp)) {
                    items(uiState.recentSessions, key = { "${it.timestamp}_${it.remoteAddress}" }) { session ->
                        SessionRow(session)
                    }
                }
            }
        }
    }
}

@Composable
private fun MonitorToggleCard(isActive: Boolean, blockedCount: Int, onToggle: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().padding(16.dp),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = SecureScopeColors.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text("Network Protection", fontWeight = FontWeight.Bold, fontSize = 15.sp)
                    Text(
                        if (isActive) "Active — blocking harmful sites" else "Inactive",
                        fontSize = 12.sp,
                        color = if (isActive) SecureScopeColors.Green else SecureScopeColors.Gray
                    )
                }
                Switch(checked = isActive, onCheckedChange = { onToggle() })
            }
            if (isActive) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("$blockedCount blocked today", fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SecureScopeColors.Blue)
            }
        }
    }
}

@Composable
private fun SessionRow(session: NetworkSessionWithApp) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Column(modifier = Modifier.weight(1f)) {
            Text(session.remoteAddress, fontSize = 13.sp, fontWeight = FontWeight.Medium)
            Text(session.appName, fontSize = 11.sp, color = SecureScopeColors.Gray)
        }
        Text(TimeFormatUtil.formatRelativeTime(session.timestamp), fontSize = 11.sp, color = SecureScopeColors.Gray)
    }
}