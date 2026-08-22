package pfa.redouaneachak.securescope.ui.screens.home

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
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
import androidx.compose.ui.text.style.TextAlign
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
import pfa.redouaneachak.securescope.ui.components.SecureScopeLoadingIndicator
import pfa.redouaneachak.securescope.util.ByteFormatUtil
import pfa.redouaneachak.securescope.util.TimeFormatUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onNavigateToScan: () -> Unit = {},
    onNavigateToDataUsage: () -> Unit = {},
    onNavigateToNetworkScan: () -> Unit = {},
    onNavigateToHardware: () -> Unit = {},
    onNavigateToRecentApps: () -> Unit = {},
    onNavigateToAppList: () -> Unit = {},
    onNavigateToAppDetail: (String) -> Unit = {},
    onOpenMenu: () -> Unit = {},
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        "SECURE SCOPE",
                        color = Color(0xFF0F7B6C),
                        fontWeight = FontWeight.Bold,
                        fontSize = 18.sp
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onOpenMenu) {
                        Icon(Icons.Filled.Menu, contentDescription = "Menu")
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
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            DataUsageBar(
                hasPermission = uiState.hasUsageAccessPermission,
                sentBytes = uiState.totalDataSentBytes,
                receivedBytes = uiState.totalDataReceivedBytes,
                onGrantPermission = viewModel::requestUsageAccess,
                onClick = onNavigateToDataUsage
            )

            AppGridPreview(
                apps = uiState.installedAppsPreview,
                totalCount = uiState.installedAppsCount,
                isLoading = uiState.isLoadingApps,
                onMoreClick = onNavigateToAppList,
                onAppClick = onNavigateToAppDetail
            )

            HomeActionGrid(
                lastScanTimestamp = uiState.lastScanTimestamp,
                onScanDevice = onNavigateToScan,
                onScanNetwork = onNavigateToNetworkScan,
                onHardwareMonitoring = onNavigateToHardware,
                onRecentApps = onNavigateToRecentApps
            )
        }
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
        shape = RoundedCornerShape(20.dp)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(Color(0xFF0F9B7E), Color(0xFF1565A8))))
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            if (hasPermission) {
                Column {
                    Text(
                        text = "Today's Data Usage",
                        color = Color.White.copy(alpha = 0.85f),
                        fontSize = 11.sp
                    )
                    // Reduced from 6.dp to 4.dp to offset the extra line of text below
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        UsageStat(
                            icon = Icons.Filled.ArrowUpward,
                            label = "Sent",
                            value = ByteFormatUtil.formatBytes(sentBytes)
                        )
                        UsageStat(
                            icon = Icons.Filled.ArrowDownward,
                            label = "Received",
                            value = ByteFormatUtil.formatBytes(receivedBytes)
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("Enable usage access", color = Color.White, fontSize = 13.sp)
                    TextButton(onClick = onGrantPermission) {
                        Text("Grant", color = Color.White, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Composable
private fun UsageStat(icon: ImageVector, value: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(4.dp))
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 14.sp)
    }
}
@Composable
private fun UsageStat(icon: ImageVector, label: String, value: String) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun UsageStat(icon: ImageVector, label: String, value: String, align: Alignment.Horizontal, modifier: Modifier = Modifier) {
    Column(horizontalAlignment = align, modifier = modifier) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun UsageStat(icon: ImageVector, label: String, value: String, align: Alignment.Horizontal) {
    Column(horizontalAlignment = align) {
        Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
        Spacer(modifier = Modifier.height(4.dp))
        Text(label, color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp)
        Text(value, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)
    }
}

@Composable
private fun AppGridPreview(
    apps: List<AppInfo>,
    totalCount: Int,
    isLoading: Boolean,
    onMoreClick: () -> Unit,
    onAppClick: (String) -> Unit
) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Installed Apps ($totalCount)", fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(12.dp))

            if (isLoading && apps.isEmpty()) {
                Box(modifier = Modifier.fillMaxWidth().height(150.dp), contentAlignment = Alignment.Center) {
                    SecureScopeLoadingIndicator(label = "Loading apps...")
                }
            } else {
                val columns = 5
                val maxSlots = columns * 3 // 3 rows
                val visibleApps = apps.take(maxSlots - 1) // reserve last slot for "more"

                LazyVerticalGrid(
                    columns = GridCells.Fixed(columns),
                    modifier = Modifier.heightIn(max = 220.dp),
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    userScrollEnabled = false
                ) {
                    items(visibleApps) { app -> AppIconTile(app, onClick = { onAppClick(app.packageName) }) }
                    item { MoreTile(onClick = onMoreClick) }
                }
            }
        }
    }
}
@Composable
private fun AppIconTile(app: AppInfo, onClick: () -> Unit) {
    val bitmap = remember(app.packageName) { app.icon.toBitmap().asImageBitmap() }
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(56.dp).clickable(onClick = onClick)
    ) {
        Image(bitmap = bitmap, contentDescription = app.appName, modifier = Modifier.size(30.dp))
        Spacer(modifier = Modifier.height(3.dp))
        Text(app.appName, fontSize = 9.sp, maxLines = 1, overflow = TextOverflow.Ellipsis, textAlign = TextAlign.Center)
    }
}

@Composable
private fun MoreTile(onClick: () -> Unit) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.width(56.dp).clip(RoundedCornerShape(8.dp)).clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.size(30.dp), contentAlignment = Alignment.Center) {
            Text("•••", fontSize = 16.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(3.dp))
        Text("More", fontSize = 9.sp)
    }
}

@Composable
private fun HomeActionGrid(
    lastScanTimestamp: Long?,
    onScanDevice: () -> Unit,
    onScanNetwork: () -> Unit,
    onHardwareMonitoring: () -> Unit,
    onRecentApps: () -> Unit
) {
    val scanSubtitle = lastScanTimestamp?.let { "Last scan: ${TimeFormatUtil.formatRelativeTime(it)}" } ?: "Never scanned"

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ActionTile(Icons.Filled.Shield, Color(0xFF2196F3), "Scan Apps", scanSubtitle, Modifier.weight(1f), onScanDevice)
            ActionTile(Icons.Filled.Wifi, Color(0xFF22C55E), "Scan Network", "Tap to scan", Modifier.weight(1f), onScanNetwork)
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp), modifier = Modifier.fillMaxWidth()) {
            ActionTile(Icons.Filled.Memory, Color(0xFFF59E0B), "Hardware Monitoring", null, Modifier.weight(1f), onHardwareMonitoring)
            ActionTile(Icons.Filled.History, Color(0xFF14B8A6), "Recent Apps", null, Modifier.weight(1f), onRecentApps)
        }
    }
}

@Composable
private fun ActionTile(
    icon: ImageVector,
    badgeColor: Color,
    label: String,
    subtitle: String?,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        modifier = modifier.height(110.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize().padding(14.dp), verticalArrangement = Arrangement.SpaceBetween) {
            Box(
                modifier = Modifier.size(36.dp).clip(CircleShape).background(badgeColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(icon, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
            }
            Column {
                Text(label, fontWeight = FontWeight.Bold, fontSize = 13.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
                if (subtitle != null) {
                    Text(subtitle, fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}