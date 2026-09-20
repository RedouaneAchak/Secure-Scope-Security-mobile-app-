package pfa.redouaneachak.securescope.ui.screens.appdetail

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Stop
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pfa.redouaneachak.securescope.data.model.AppInfo
import pfa.redouaneachak.securescope.data.model.PermissionInfo
import pfa.redouaneachak.securescope.data.model.ScanResult
import pfa.redouaneachak.securescope.ui.components.RiskBadge
import pfa.redouaneachak.securescope.ui.components.SecureScopeLoadingIndicator
import pfa.redouaneachak.securescope.ui.theme.SecureScopeColors
import pfa.redouaneachak.securescope.util.TimeFormatUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppDetailScreen(
    onBack: () -> Unit,
    onNavigateToServerList: (String) -> Unit,
    viewModel: AppDetailViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(uiState.app?.appName ?: "App Details") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { padding ->
        if (uiState.isLoading) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                SecureScopeLoadingIndicator(label = "Loading...")
            }
        } else if (uiState.app == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Text("App not found — it may have been uninstalled.")
            }
        } else {
            AppDetailContent(
                uiState = uiState,
                viewModel = viewModel,
                onNavigateToServerList = onNavigateToServerList,
                modifier = Modifier.padding(padding)
            )
        }
    }
}

@Composable
private fun AppDetailContent(
    uiState: AppDetailUiState,
    viewModel: AppDetailViewModel,
    onNavigateToServerList: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val app = uiState.app!!

    LazyColumn(modifier = modifier.fillMaxSize(), contentPadding = PaddingValues(16.dp)) {

        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                uiState.icon?.let {
                    Image(bitmap = it, contentDescription = app.appName, modifier = Modifier.size(72.dp))
                }
                Spacer(modifier = Modifier.height(8.dp))
                Text(app.appName, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Text(app.packageName, fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Spacer(modifier = Modifier.height(20.dp))
        }

        item {
            InfoCard(app, uiState.installSource)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ScanCard(app, uiState.lastScanResult, uiState.isScanning, onScanNow = viewModel::scanNow)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ContactedServersCard(
                servers = uiState.contactedServers,
                onSeeMore = { onNavigateToServerList(app.packageName) }
            )
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            PermissionsCard(uiState.permissions)
            Spacer(modifier = Modifier.height(16.dp))
        }

        item {
            ActionButtons(onUninstall = viewModel::uninstall, onForceStop = viewModel::forceStop)
        }
    }
}

@Composable
private fun InfoCard(app: AppInfo, installSource: String?) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text("App Info", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            InfoRow("Version", app.versionName)
            InfoRow("Installed", TimeFormatUtil.formatRelativeTime(app.installedTimestamp))
            InfoRow("Last Updated", TimeFormatUtil.formatRelativeTime(app.lastUpdatedTimestamp))
            InfoRow("Type", if (app.isSystemApp) "System App" else "User App")
            InfoRow("Installed From", installSource ?: "Unknown")
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, fontSize = 13.sp, color = SecureScopeColors.Gray, modifier = Modifier.weight(1f))
        Spacer(modifier = Modifier.width(12.dp))
        Text(value, fontSize = 13.sp, fontWeight = FontWeight.Medium, textAlign = TextAlign.End, modifier = Modifier.weight(1f))
    }
}

@Composable
private fun ScanCard(
    app: AppInfo,
    result: ScanResult?,
    isScanning: Boolean,
    onScanNow: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                Text("Security Scan", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                if (result != null) RiskBadge(result.riskScore)
            }

            if (app.isSystemApp) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("System apps cannot be scanned.", fontSize = 13.sp, color = SecureScopeColors.Gray)
                return@Column
            }

            if (result == null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("Not scanned yet", fontSize = 13.sp, color = SecureScopeColors.Gray)
            } else {
                Spacer(modifier = Modifier.height(8.dp))
                InfoRow("Trackers found", result.trackerCount.toString())
                InfoRow("Malware detected", if (result.malwareDetected) "Yes" else "No")
                if (result.malwareNames.isNotEmpty()) {
                    Column(modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)) {
                        Text("Threat names", fontSize = 12.sp, color = SecureScopeColors.Gray)
                        Text(result.malwareNames.joinToString(", "), fontSize = 13.sp, fontWeight = FontWeight.Medium, color = SecureScopeColors.DangerRed)
                    }
                }
                InfoRow("Scanned", TimeFormatUtil.formatRelativeTime(result.scanTimestamp))
                InfoRow("Verification", if (result.cloudVerified) "Cloud-verified" else "Local only")
            }

            Spacer(modifier = Modifier.height(10.dp))
            Button(
                onClick = onScanNow,
                enabled = !isScanning,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = SecureScopeColors.Blue)
            ) {
                if (isScanning) {
                    Text("Scanning...")
                } else {
                    Icon(Icons.Filled.Security, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(if (result == null) "Scan Now" else "Rescan")
                }
            }
        }
    }
}

@Composable
private fun ContactedServersCard(servers: List<String>, onSeeMore: () -> Unit) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Contacted Servers (${servers.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(8.dp))

            if (servers.isEmpty()) {
                Text(
                    "No servers detected yet. Run network protection and use the application to see contacted servers.",
                    fontSize = 13.sp,
                    color = SecureScopeColors.Gray
                )
            } else {
                servers.take(3).forEach { server ->
                    Text(server, fontSize = 13.sp, modifier = Modifier.padding(vertical = 3.dp))
                }
                if (servers.size > 3) {
                    Spacer(modifier = Modifier.height(6.dp))
                    TextButton(onClick = onSeeMore, contentPadding = PaddingValues(0.dp)) {
                        Text("See more (${servers.size - 3})", color = SecureScopeColors.Blue, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}

@Composable
private fun PermissionsCard(permissions: List<PermissionInfo>) {
    val dangerous = permissions.filter { it.isDangerous }
    val normal = permissions.filterNot { it.isDangerous }

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Permissions (${permissions.size})", fontWeight = FontWeight.Bold, fontSize = 14.sp)

            if (permissions.isEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text("No permissions requested", fontSize = 13.sp, color = SecureScopeColors.Gray)
                return@Column
            }

            if (dangerous.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text("Dangerous (${dangerous.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SecureScopeColors.DangerRed)
                Spacer(modifier = Modifier.height(4.dp))
                dangerous.forEach { PermissionRow(it) }
            }

            if (normal.isNotEmpty()) {
                Spacer(modifier = Modifier.height(14.dp))
                Text("Other (${normal.size})", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = SecureScopeColors.Gray)
                Spacer(modifier = Modifier.height(4.dp))
                normal.forEach { PermissionRow(it) }
            }
        }
    }
}

@Composable
private fun PermissionRow(perm: PermissionInfo) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            perm.label,
            fontSize = 13.sp,
            modifier = Modifier.weight(1f),
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            if (perm.isGranted) "Granted" else "Denied",
            fontSize = 11.sp,
            color = if (perm.isGranted) SecureScopeColors.Green else SecureScopeColors.Gray,
            maxLines = 1,
            softWrap = false
        )
    }
}

@Composable
private fun ActionButtons(onUninstall: () -> Unit, onForceStop: () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        OutlinedButton(onClick = onForceStop, modifier = Modifier.fillMaxWidth()) {
            Icon(Icons.Filled.Stop, contentDescription = null, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(6.dp))
            Text("Take Action")
        }
    }
}