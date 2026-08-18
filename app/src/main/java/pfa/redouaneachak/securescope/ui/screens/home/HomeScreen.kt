package pfa.redouaneachak.securescope.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pfa.redouaneachak.securescope.data.model.AppInfo
import pfa.redouaneachak.securescope.util.ByteFormatUtil

@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit = {},
    onNavigateToNetworkScan: () -> Unit = {},
    onNavigateToHardware: () -> Unit = {},
    onNavigateToRecentApps: () -> Unit = {},
    onNavigateToAppList: () -> Unit = {},
    viewModel: HomeViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadDashboard()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        DataUsageBar(
            hasPermission = uiState.hasUsageAccessPermission,
            sentBytes = uiState.totalDataSentBytes,
            receivedBytes = uiState.totalDataReceivedBytes,
            onGrantPermission = viewModel::requestUsageAccess,
            onClick = onNavigateToNetworkScan
        )

        AppGridPreview(
            apps = uiState.installedAppsPreview,
            totalCount = uiState.installedAppsCount,
            isLoading = uiState.isLoading,
            onMoreClick = onNavigateToAppList
        )

        HomeActionButtons(
            onScanDevice = onNavigateToScan,
            onScanNetwork = onNavigateToNetworkScan,
            onHardwareMonitoring = onNavigateToHardware,
            onRecentApps = onNavigateToRecentApps
        )
    }
}

@Composable
private fun DataUsageBar(
    hasPermission: Boolean,
    sentBytes: Long,
    receivedBytes: Long,
    onGrantPermission: () -> Unit,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFF0F9B7E), Color(0xFF1565A8))))
                .padding(20.dp)
        ) {
            if (hasPermission) {
                Column {
                    Text("Data Usage", color = Color.White.copy(alpha = 0.85f), fontSize = 14.sp)
                    Spacer(modifier = Modifier.height(12.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        UsageStat(Icons.Filled.ArrowUpward, "Sent", ByteFormatUtil.formatBytes(sentBytes))
                        UsageStat(Icons.Filled.ArrowDownward, "Received", ByteFormatUtil.formatBytes(receivedBytes))
                    }
                }
            } else {
                Column {
                    Text("Data Usage", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                    Text(
                        "Enable usage access to see stats",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(
                        onClick = onGrantPermission,
                        colors = ButtonDefaults.buttonColors(containerColor = Color.White)
                    ) {
                        Text("Grant Access", color = Color(0xFF0F7B6C))
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageStat(icon: ImageVector, label: String, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(6.dp))
        Column {
            Text(label, color = Color.White.copy(alpha = 0.75f), fontSize = 11.sp)
            Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp)
        }
    }
}

@Composable
private fun AppGridPreview(
    apps: List<AppInfo>,
    totalCount: Int,
    isLoading: Boolean,
    onMoreClick: () -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Installed Apps ($totalCount)", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading && apps.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxWidth().height(120.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        CircularProgressIndicator(modifier = Modifier.size(28.dp), strokeWidth = 3.dp)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Loading apps...", fontSize = 12.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            } else {
                LazyVerticalGrid(
                    columns = GridCells.Fixed(4),
                    modifier = Modifier.heightIn(max = 220.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(apps) { app -> AppIconTile(app) }
                    item { MoreTile(onClick = onMoreClick) }
                }
            }
        }
    }
}

@Composable
private fun AppIconTile(app: AppInfo) {
    val bitmap = remember(app.packageName) { app.icon.toBitmap().asImageBitmap() }
    Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.width(64.dp)) {
        Image(bitmap = bitmap, contentDescription = app.appName, modifier = Modifier.size(32.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            app.appName,
            fontSize = 10.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun MoreTile(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(64.dp).clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.secondaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.MoreHoriz, contentDescription = "More", modifier = Modifier.size(18.dp))
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text("More", fontSize = 10.sp)
    }
}

@Composable
private fun HomeActionButtons(
    onScanDevice: () -> Unit,
    onScanNetwork: () -> Unit,
    onHardwareMonitoring: () -> Unit,
    onRecentApps: () -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ActionTile(Icons.Filled.Security, "Scan Device", Modifier.weight(1f), onScanDevice)
            ActionTile(Icons.Filled.Wifi, "Scan Network", Modifier.weight(1f), onScanNetwork)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ActionTile(Icons.Filled.Memory, "Hardware", Modifier.weight(1f), onHardwareMonitoring)
            ActionTile(Icons.Filled.History, "Recent Apps", Modifier.weight(1f), onRecentApps)
        }
    }
}

@Composable
private fun ActionTile(icon: ImageVector, label: String, modifier: Modifier = Modifier, onClick: () -> Unit) {
    Card(
        onClick = onClick,
        modifier = modifier.height(96.dp),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.secondaryContainer)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(12.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.Start
        ) {
            Icon(icon, contentDescription = null, modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(8.dp))
            Text(label, fontWeight = FontWeight.Bold, fontSize = 14.sp)
        }
    }
}