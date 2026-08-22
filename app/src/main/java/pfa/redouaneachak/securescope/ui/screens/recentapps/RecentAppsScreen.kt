package pfa.redouaneachak.securescope.ui.screens.recentapps

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pfa.redouaneachak.securescope.ui.components.SecureScopeLoadingIndicator
import pfa.redouaneachak.securescope.ui.theme.SecureScopeColors
import pfa.redouaneachak.securescope.util.TimeFormatUtil

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecentAppsScreen(
    onBack: () -> Unit,
    onAppClick: (String) -> Unit,
    viewModel: RecentAppsViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    val lifecycleOwner = LocalLifecycleOwner.current
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) viewModel.recheckPermission()
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recent Apps") },
                navigationIcon = {
                    IconButton(onClick = onBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Back") }
                }
            )
        }
    ) { padding ->
        when {
            uiState.isLoading -> Box(modifier = Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                SecureScopeLoadingIndicator(label = "Loading recent apps...")
            }
            !uiState.hasPermission -> Box(
                modifier = Modifier.fillMaxSize().padding(padding).padding(24.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Usage access is required to show recent apps.")
                    Spacer(modifier = Modifier.height(12.dp))
                    Button(onClick = viewModel::requestPermission) { Text("Grant Access") }
                }
            }
            else -> LazyColumn(modifier = Modifier.fillMaxSize().padding(padding), contentPadding = PaddingValues(16.dp)) {
                items(uiState.rows, key = { it.info.packageName }) { row ->
                    RecentAppRowItem(row, onClick = { onAppClick(row.info.packageName) })
                }
            }
        }
    }
}

@Composable
private fun RecentAppRowItem(row: RecentAppRow, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(bitmap = row.icon, contentDescription = row.appName, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(12.dp))
        Column {
            Text(row.appName, fontWeight = FontWeight.Bold, fontSize = 14.sp)
            Text(
                TimeFormatUtil.formatRelativeTime(row.info.lastUsedTimestamp),
                fontSize = 12.sp,
                color = SecureScopeColors.Gray
            )
        }
    }
}