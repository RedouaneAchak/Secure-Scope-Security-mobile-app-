package pfa.redouaneachak.securescope.ui.screens.applist

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import pfa.redouaneachak.securescope.ui.components.SecureScopeLoadingIndicator

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppListScreen(
    onBack: () -> Unit,
    onAppClick: (String) -> Unit,
    viewModel: AppListViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Installed Apps (${uiState.displayedApps.size})") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    Box {
                        IconButton(onClick = { sortMenuExpanded = true }) {
                            Icon(Icons.Filled.Sort, contentDescription = "Sort")
                        }
                        DropdownMenu(expanded = sortMenuExpanded, onDismissRequest = { sortMenuExpanded = false }) {
                            AppSortOption.entries.forEach { option ->
                                DropdownMenuItem(
                                    text = { Text(option.label) },
                                    onClick = { viewModel.onSortOptionChange(option); sortMenuExpanded = false }
                                )
                            }
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = uiState.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                placeholder = { Text("Search apps") },
                leadingIcon = { Icon(Icons.Filled.Search, contentDescription = null) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth().padding(16.dp)
            )
            SingleChoiceSegmentedButtonRow(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp)) {
                AppTypeFilter.entries.forEachIndexed { index, filter ->
                    SegmentedButton(
                        selected = uiState.typeFilter == filter,
                        onClick = { viewModel.onTypeFilterChange(filter) },
                        shape = SegmentedButtonDefaults.itemShape(index = index, count = AppTypeFilter.entries.size)
                    ) {
                        Text(filter.label, fontSize = 12.sp)
                    }
                }
            }
            Spacer(modifier = Modifier.height(8.dp))

            if (uiState.isLoading) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    SecureScopeLoadingIndicator(label = "Loading apps...")
                }
            } else {
                LazyColumn(contentPadding = PaddingValues(horizontal = 16.dp, vertical = 4.dp)) {
                    items(uiState.displayedApps, key = { it.app.packageName }) { row ->
                        AppListItem(row = row, onClick = { onAppClick(row.app.packageName) })
                    }
                }
            }
        }
    }
}

@Composable
private fun AppListItem(row: AppListRow, onClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick).padding(vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Image(bitmap = row.icon, contentDescription = row.app.appName, modifier = Modifier.size(40.dp))
        Spacer(modifier = Modifier.width(14.dp))
        Column {
            Text(row.app.appName, fontWeight = FontWeight.Bold, fontSize = 15.sp)
            Text(row.app.packageName, fontSize = 11.sp, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}