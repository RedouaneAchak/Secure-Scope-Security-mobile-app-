package pfa.redouaneachak.securescope.ui.screens.hardware

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pfa.redouaneachak.securescope.ui.components.SecureScopeLoadingIndicator
import pfa.redouaneachak.securescope.ui.theme.SecureScopeColors
import pfa.redouaneachak.securescope.util.ByteFormatUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HardwareScreen(onBack: () -> Unit, viewModel: HardwareViewModel = hiltViewModel()) {
    val stats by viewModel.stats.collectAsStateWithLifecycle()
    val breakdown by viewModel.storageBreakdown.collectAsStateWithLifecycle()
    val appsExpanded by viewModel.appsExpanded.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(Unit) {
        viewModel.loadStorageBreakdown()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Hardware Monitoring") },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") } }
            )
        }
    ) { padding ->
        val current = stats
        if (current == null) {
            Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                SecureScopeLoadingIndicator(label = "Reading hardware stats...")
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {
                item {
                    StatCard(Icons.Filled.Memory, SecureScopeColors.Blue, "RAM", "${current.ramUsedMb} MB used", "of ${current.ramTotalMb} MB", current.ramUsedMb.toFloat() / current.ramTotalMb.toFloat())
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    StatCard(Icons.Filled.BatteryFull, SecureScopeColors.Green, "Battery", "${current.batteryLevelPercent}%", "${current.batteryTemperatureCelsius}°C", current.batteryLevelPercent / 100f)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    StatCard(Icons.Filled.Storage, SecureScopeColors.Gray, "Storage", "%.1f GB used".format(current.storageUsedGb), "of %.1f GB".format(current.storageTotalGb), current.storageUsedGb / current.storageTotalGb)
                    Spacer(modifier = Modifier.height(16.dp))
                }
                item {
                    if (breakdown == null) {
                        Box(modifier = Modifier.fillMaxWidth().height(80.dp), contentAlignment = Alignment.Center) {
                            SecureScopeLoadingIndicator(label = "Analyzing storage...")
                        }
                    } else {
                        StorageBreakdownCard(
                            breakdown = breakdown!!,
                            appsExpanded = appsExpanded,
                            onToggleApps = viewModel::toggleAppsExpanded
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StatCard(icon: ImageVector, color: Color, title: String, usedLabel: String, totalLabel: String, progress: Float) {
    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SecureScopeColors.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(22.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text(title, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            }
            Spacer(modifier = Modifier.height(10.dp))
            LinearProgressIndicator(
                progress = { progress.coerceIn(0f, 1f) },
                modifier = Modifier.fillMaxWidth().height(8.dp).clip(RoundedCornerShape(4.dp)),
                color = color, trackColor = SecureScopeColors.LightGray
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                Text(usedLabel, fontSize = 13.sp, fontWeight = FontWeight.Medium)
                Text(totalLabel, fontSize = 12.sp, color = SecureScopeColors.Gray)
            }
        }
    }
}

@Composable
private fun StorageBreakdownCard(
    breakdown: pfa.redouaneachak.securescope.data.model.StorageBreakdown,
    appsExpanded: Boolean,
    onToggleApps: () -> Unit
) {
    val categoryColors = listOf(SecureScopeColors.Blue, SecureScopeColors.Green, SecureScopeColors.Gray, SecureScopeColors.WarningAmber, SecureScopeColors.LightGray)
    val total = breakdown.categories.sumOf { it.bytes }.coerceAtLeast(1)

    Card(modifier = Modifier.fillMaxWidth(), shape = RoundedCornerShape(16.dp), colors = CardDefaults.cardColors(containerColor = SecureScopeColors.White)) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("Storage Breakdown", fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(modifier = Modifier.fillMaxWidth().height(10.dp).clip(RoundedCornerShape(5.dp))) {
                breakdown.categories.forEachIndexed { index, cat ->
                    val weight = (cat.bytes.toFloat() / total.toFloat()).coerceAtLeast(0.01f)
                    Box(modifier = Modifier.weight(weight).fillMaxHeight().background(categoryColors[index % categoryColors.size]))
                }
            }
            Spacer(modifier = Modifier.height(12.dp))

            breakdown.categories.forEachIndexed { index, cat ->
                if (cat.category == "Apps") {
                    Row(
                        modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleApps).padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(categoryColors[index % categoryColors.size]))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cat.category, fontSize = 13.sp)
                        }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(ByteFormatUtil.formatBytes(cat.bytes), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                            Icon(if (appsExpanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown, contentDescription = null)
                        }
                    }
                    if (appsExpanded) {
                        Column(modifier = Modifier.padding(start = 18.dp, top = 4.dp, bottom = 8.dp)) {
                            breakdown.appUsages.take(20).forEach { app ->
                                Row(modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp), horizontalArrangement = Arrangement.SpaceBetween) {
                                    Text(app.appName, fontSize = 12.sp, color = SecureScopeColors.Gray)
                                    Text(ByteFormatUtil.formatBytes(app.bytes), fontSize = 12.sp, color = SecureScopeColors.Gray)
                                }
                            }
                        }
                    }
                } else {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                        horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(modifier = Modifier.size(10.dp).clip(RoundedCornerShape(3.dp)).background(categoryColors[index % categoryColors.size]))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(cat.category, fontSize = 13.sp)
                        }
                        Text(ByteFormatUtil.formatBytes(cat.bytes), fontSize = 13.sp, fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }
}