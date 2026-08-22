package pfa.redouaneachak.securescope.ui.screens.scan

import androidx.compose.animation.animateContentSize
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pfa.redouaneachak.securescope.data.model.ScanResult
import pfa.redouaneachak.securescope.ui.components.RiskBadge
import pfa.redouaneachak.securescope.ui.theme.SecureScopeColors
import pfa.redouaneachak.securescope.util.TimeFormatUtil
import androidx.compose.ui.graphics.drawscope.rotate
import pfa.redouaneachak.securescope.ui.components.SecureScopeLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScanScreen(
    onBack: () -> Unit,
    onNavigateToAppDetail: (String) -> Unit,
    viewModel: ScanViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.loadLastResults()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Scan Apps") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                },
                actions = {
                    if (uiState.phase == ScanPhase.RESULTS) {
                        TextButton(onClick = viewModel::startScan) {
                            Text("Scan", fontWeight = FontWeight.Bold, color = SecureScopeColors.Blue)
                        }
                    }
                }
            )
        }
    ) { padding ->
        Box(modifier = Modifier.fillMaxSize().padding(padding)) {
            when (uiState.phase) {
                ScanPhase.LOADING -> Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    SecureScopeLoadingIndicator()
                }
                ScanPhase.IDLE -> IdleScanContent(onScan = viewModel::startScan)
                ScanPhase.SCANNING -> ScanningContent(
                    current = uiState.progressCurrent,
                    total = uiState.progressTotal,
                    currentAppName = uiState.currentAppName
                )
                ScanPhase.RESULTS -> ResultsList(
                    results = uiState.results,
                    expandedPackageName = uiState.expandedPackageName,
                    onToggleExpand = viewModel::toggleExpanded,
                    onUninstall = viewModel::uninstall,
                    onForceStop = viewModel::forceStop,
                    onManagePermissions = onNavigateToAppDetail
                )
            }
        }
    }
}

@Composable
private fun IdleScanContent(onScan: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(
            modifier = Modifier
                .size(140.dp)
                .clip(CircleShape)
                .background(Brush.horizontalGradient(listOf(SecureScopeColors.Blue, SecureScopeColors.Green)))
                .clickable(onClick = onScan),
            contentAlignment = Alignment.Center
        ) {
            Icon(Icons.Filled.Shield, contentDescription = "Scan", tint = SecureScopeColors.White, modifier = Modifier.size(64.dp))
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Scan Your Device", fontWeight = FontWeight.Bold, fontSize = 18.sp)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            "Check installed apps for trackers, dangerous permissions, and malware.",
            fontSize = 13.sp,
            color = SecureScopeColors.Gray,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
    }
}

@Composable
private fun ScanningContent(current: Int, total: Int, currentAppName: String) {
    val transition = rememberInfiniteTransition(label = "scan_ring")
    val rotation by transition.animateFloat(
        initialValue = 0f, targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(1200, easing = LinearEasing)),
        label = "rotation"
    )

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Box(modifier = Modifier.size(160.dp), contentAlignment = Alignment.Center) {
            Canvas(modifier = Modifier.size(160.dp)) {
                rotate(degrees = rotation) {
                    drawArc(
                        brush = Brush.sweepGradient(listOf(SecureScopeColors.Blue, SecureScopeColors.Green, androidx.compose.ui.graphics.Color.Transparent)),
                        startAngle = 0f, sweepAngle = 300f, useCenter = false,
                        style = androidx.compose.ui.graphics.drawscope.Stroke(width = 8.dp.toPx(), cap = androidx.compose.ui.graphics.StrokeCap.Round),
                        size = Size(size.width, size.height)
                    )
                }
            }
            Box(
                modifier = Modifier.size(100.dp).clip(CircleShape)
                    .background(Brush.horizontalGradient(listOf(SecureScopeColors.Blue, SecureScopeColors.Green))),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Shield, contentDescription = null, tint = SecureScopeColors.White, modifier = Modifier.size(44.dp))
            }
        }
        Spacer(modifier = Modifier.height(24.dp))
        Text("Scanning...", fontWeight = FontWeight.Bold, fontSize = 16.sp)
        Spacer(modifier = Modifier.height(4.dp))
        if (total > 0) {
            Text("$current of $total — $currentAppName", fontSize = 13.sp, color = SecureScopeColors.Gray)
            Spacer(modifier = Modifier.height(12.dp))
            LinearProgressIndicator(
                progress = { current.toFloat() / total.toFloat() },
                modifier = Modifier.fillMaxWidth(0.7f).height(6.dp).clip(RoundedCornerShape(3.dp)),
                color = SecureScopeColors.Blue,
                trackColor = SecureScopeColors.LightGray
            )
        }
    }
}

@Composable
private fun ResultsList(
    results: List<ScanResult>,
    expandedPackageName: String?,
    onToggleExpand: (String) -> Unit,
    onUninstall: (String) -> Unit,
    onForceStop: (String) -> Unit,
    onManagePermissions: (String) -> Unit
) {
    if (results.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No apps to display", color = SecureScopeColors.Gray)
        }
        return
    }

    LazyColumn(contentPadding = PaddingValues(16.dp)) {
        items(results, key = { it.app.packageName }) { result ->
            ScanResultCard(
                result = result,
                expanded = expandedPackageName == result.app.packageName,
                onToggleExpand = { onToggleExpand(result.app.packageName) },
                onUninstall = { onUninstall(result.app.packageName) },
                onForceStop = { onForceStop(result.app.packageName) },
                onManagePermissions = { onManagePermissions(result.app.packageName) }
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
    }
}

@Composable
private fun ScanResultCard(
    result: ScanResult,
    expanded: Boolean,
    onToggleExpand: () -> Unit,
    onUninstall: () -> Unit,
    onForceStop: () -> Unit,
    onManagePermissions: () -> Unit
) {
    val bitmap = remember(result.app.packageName) { result.app.icon.toBitmap().asImageBitmap() }
    var actionMenuExpanded by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier.fillMaxWidth().animateContentSize(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = SecureScopeColors.White)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth().clickable(onClick = onToggleExpand),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Image(bitmap = bitmap, contentDescription = result.app.appName, modifier = Modifier.size(36.dp))
                Spacer(modifier = Modifier.width(12.dp))
                Text(result.app.appName, fontWeight = FontWeight.Bold, fontSize = 14.sp, modifier = Modifier.weight(1f))
                RiskBadge(result.riskScore)
                Spacer(modifier = Modifier.width(6.dp))
                Icon(
                    if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                    contentDescription = null
                )
            }

            if (expanded) {
                Spacer(modifier = Modifier.height(12.dp))
                Divider()
                Spacer(modifier = Modifier.height(12.dp))

                Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Trackers: ${result.trackerCount}", fontSize = 13.sp)
                        if (result.detectedTrackers.isNotEmpty()) {
                            Text(
                                result.detectedTrackers.joinToString(", ") { it.name },
                                fontSize = 12.sp,
                                color = SecureScopeColors.Gray
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Malware: ${if (result.malwareDetected) "Detected" else "None"}",
                            fontSize = 13.sp,
                            color = if (result.malwareDetected) SecureScopeColors.DangerRed else SecureScopeColors.Green
                        )
                        if (result.malwareNames.isNotEmpty()) {
                            Text(
                                result.malwareNames.joinToString(", "),
                                fontSize = 12.sp,
                                color = SecureScopeColors.DangerRed
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            "Scanned ${TimeFormatUtil.formatRelativeTime(result.scanTimestamp)}",
                            fontSize = 11.sp,
                            color = SecureScopeColors.Gray
                        )
                    }

                    Box {
                        OutlinedButton(onClick = { actionMenuExpanded = true }) {
                            Text("Action", fontSize = 12.sp)
                        }
                        DropdownMenu(expanded = actionMenuExpanded, onDismissRequest = { actionMenuExpanded = false }) {
                            DropdownMenuItem(
                                text = { Text("Force Stop") },
                                leadingIcon = { Icon(Icons.Filled.Stop, contentDescription = null) },
                                onClick = { actionMenuExpanded = false; onForceStop() }
                            )
                            DropdownMenuItem(
                                text = { Text("Uninstall") },
                                leadingIcon = { Icon(Icons.Filled.Delete, contentDescription = null) },
                                onClick = { actionMenuExpanded = false; onUninstall() }
                            )
                            DropdownMenuItem(
                                text = { Text("Manage Permissions") },
                                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                                onClick = { actionMenuExpanded = false; onManagePermissions() }
                            )
                        }
                    }
                }
            }
        }
    }
}