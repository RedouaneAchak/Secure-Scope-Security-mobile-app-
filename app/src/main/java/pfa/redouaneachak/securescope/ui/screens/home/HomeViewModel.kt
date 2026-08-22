package pfa.redouaneachak.securescope.ui.screens.home

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
import kotlinx.coroutines.flow.first
import pfa.redouaneachak.securescope.data.repository.SecurityScanRepository

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val activeAppsRepository: ActiveAppsRepository,
    private val securityScanRepository: SecurityScanRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init { loadDashboard() }

    fun loadDashboard() {
        loadApps()
        loadDataUsage()
        loadLastScan()
    }

    private fun loadApps() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingApps = true) }
            val apps = appRepository.getInstalledApps()
                .filterNot { it.isSystemApp }
                .sortedByDescending { it.installedTimestamp }
            _uiState.update {
                it.copy(
                    isLoadingApps = false,
                    installedAppsPreview = apps.take(PREVIEW_COUNT),
                    installedAppsCount = apps.size
                )
            }
        }
    }

    private fun loadDataUsage() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoadingDataUsage = true) }
            val hasPermission = activeAppsRepository.hasUsageAccessPermission()
            val dataUsage = if (hasPermission) {
                networkMonitorRepository.getDataUsageForAllApps(sinceMillis = ONE_DAY_MS)
            } else emptyList()
            _uiState.update {
                it.copy(
                    isLoadingDataUsage = false,
                    hasUsageAccessPermission = hasPermission,
                    totalDataSentBytes = dataUsage.sumOf { u -> u.totalSentBytes },
                    totalDataReceivedBytes = dataUsage.sumOf { u -> u.totalReceivedBytes }
                )
            }
        }
    }

    private fun loadLastScan() {
        viewModelScope.launch {
            val latestScans = securityScanRepository.getLatestScanResults().first()
            _uiState.update { it.copy(lastScanTimestamp = latestScans.maxOfOrNull { s -> s.scanTimestamp }) }
        }
    }

    companion object {
        private const val PREVIEW_COUNT = 14
        private const val ONE_DAY_MS = 24 * 60 * 60 * 1000L
    }
    fun requestUsageAccess() {
        activeAppsRepository.requestUsageAccessPermission()
    }

}