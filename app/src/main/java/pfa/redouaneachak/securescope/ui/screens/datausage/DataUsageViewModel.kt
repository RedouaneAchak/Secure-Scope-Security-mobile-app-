package pfa.redouaneachak.securescope.ui.screens.datausage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pfa.redouaneachak.securescope.data.repository.ActiveAppsRepository
import pfa.redouaneachak.securescope.data.repository.AppRepository
import pfa.redouaneachak.securescope.data.repository.NetworkMonitorRepository
import javax.inject.Inject

@HiltViewModel
class DataUsageViewModel @Inject constructor(
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val appRepository: AppRepository,
    private val activeAppsRepository: ActiveAppsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(DataUsageUiState())
    val uiState: StateFlow<DataUsageUiState> = _uiState.asStateFlow()

    init { load() }

    fun load(range: DataUsageRange = _uiState.value.selectedRange) {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true, selectedRange = range) }

            val hasPermission = activeAppsRepository.hasUsageAccessPermission()
            val usage = if (hasPermission) {
                networkMonitorRepository.getDataUsageForAllApps(sinceMillis = range.millis)
            } else emptyList()

            val rows = usage.mapNotNull { entry ->
                val app = appRepository.getAppByPackageName(entry.packageName) ?: return@mapNotNull null
                DataUsageRow(app, entry.totalSentBytes, entry.totalReceivedBytes)
            }.sortedByDescending { it.sentBytes + it.receivedBytes }

            _uiState.update { it.copy(isLoading = false, hasPermission = hasPermission, rows = rows) }
        }
    }

    fun requestPermission() = activeAppsRepository.requestUsageAccessPermission()
}