package pfa.redouaneachak.securescope.ui.screens.applist

import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pfa.redouaneachak.securescope.data.repository.AppRepository
import javax.inject.Inject

@HiltViewModel
class AppListViewModel @Inject constructor(
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(AppListUiState())
    val uiState: StateFlow<AppListUiState> = _uiState.asStateFlow()

    private var allRows: List<AppListRow> = emptyList()

    init {
        load()
    }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }
            val apps = appRepository.getInstalledApps()
            allRows = withContext(Dispatchers.Default) {
                apps.map { AppListRow(it, it.icon.toBitmap().asImageBitmap()) }
            }
            applyFilters()
            _uiState.update { it.copy(isLoading = false) }
        }
    }

    fun onSearchQueryChange(query: String) {
        _uiState.update { it.copy(searchQuery = query) }
        applyFilters()
    }

    fun onSortOptionChange(option: AppSortOption) {
        _uiState.update { it.copy(sortOption = option) }
        applyFilters()
    }

    fun onTypeFilterChange(filter: AppTypeFilter) {
        _uiState.update { it.copy(typeFilter = filter) }
        applyFilters()
    }

    private fun applyFilters() {
        val query = _uiState.value.searchQuery.trim().lowercase()
        val sort = _uiState.value.sortOption
        val typeFilter = _uiState.value.typeFilter

        var result = if (query.isEmpty()) allRows else allRows.filter {
            it.app.appName.lowercase().contains(query)
        }

        result = when (typeFilter) {
            AppTypeFilter.ALL -> result
            AppTypeFilter.USER -> result.filterNot { it.app.isSystemApp }
            AppTypeFilter.SYSTEM -> result.filter { it.app.isSystemApp }
        }

        result = when (sort) {
            AppSortOption.NAME_AZ -> result.sortedBy { it.app.appName.lowercase() }
            AppSortOption.INSTALL_DATE_NEWEST -> result.sortedByDescending { it.app.installedTimestamp }
        }

        _uiState.update { it.copy(displayedApps = result) }
    }
}