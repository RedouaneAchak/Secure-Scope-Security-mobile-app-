package pfa.redouaneachak.securescope.ui.screens.datausage

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
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
import pfa.redouaneachak.securescope.ui.components.SecureScopeLoadingIndicator
import pfa.redouaneachak.securescope.util.ByteFormatUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DataUsageScreen(onBack: () -> Unit, viewModel: DataUsageViewModel = hiltViewModel()) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.load()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Data Usage") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    RangeFilterMenu(selected = uiState.selectedRange, onSelect = { viewModel.load(it) })
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                SecureScopeLoadingIndicator(label = "Loading apps...")
            }
            !uiState.hasPermission -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Usage access is required to show data usage per app.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = viewModel::requestPermission) { Text("Grant Access") }
                }
            }
            else -> LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(16.dp)
            ) {
                items(uiState.rows) { row -> DataUsageRowItem(row) }
            }
        }
    }
}

@Composable
private fun RangeFilterMenu(selected: DataUsageRange, onSelect: (DataUsageRange) -> Unit) {
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.clickable { expanded = true }.padding(horizontal = 12.dp)
        ) {
            Icon(Icons.Filled.FilterList, contentDescription = "Filter")
            Spacer(modifier = Modifier.width(4.dp))
            Text(selected.label, fontSize = 13.sp)
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            DataUsageRange.entries.forEach { range ->
                DropdownMenuItem(
                    text = { Text(range.label) },
                    onClick = { onSelect(range); expanded = false }
                )
            }
        }
    }
}

@Composable
private fun DataUsageRowItem(row: DataUsageRow) {
    val bitmap = remember(row.app.packageName) { row.app.icon.toBitmap().asImageBitmap() }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(bitmap = bitmap, contentDescription = row.app.appName, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(row.app.appName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Spacer(modifier = Modifier.height(2.dp))
            Row {
                Text("↑ ${ByteFormatUtil.formatBytes(row.sentBytes)}", fontSize = 12.sp)
                Spacer(modifier = Modifier.width(12.dp))
                Text("↓ ${ByteFormatUtil.formatBytes(row.receivedBytes)}", fontSize = 12.sp)
            }
        }
    }
}