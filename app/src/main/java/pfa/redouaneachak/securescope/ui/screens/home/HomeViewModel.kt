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

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val appRepository: AppRepository,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val activeAppsRepository: ActiveAppsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        loadDashboard()
    }

    fun loadDashboard() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val hasPermission = activeAppsRepository.hasUsageAccessPermission()
            val apps = appRepository.getInstalledApps().filterNot { it.isSystemApp }
            val dataUsage = if (hasPermission) networkMonitorRepository.getDataUsageForAllApps() else emptyList()

            _uiState.update {
                it.copy(
                    isLoading = false,
                    installedAppsPreview = apps.take(PREVIEW_COUNT),
                    installedAppsCount = apps.size,
                    totalDataSentBytes = dataUsage.sumOf { usage -> usage.totalSentBytes },
                    totalDataReceivedBytes = dataUsage.sumOf { usage -> usage.totalReceivedBytes },
                    hasUsageAccessPermission = hasPermission
                )
            }
        }
    }

    fun requestUsageAccess() {
        activeAppsRepository.requestUsageAccessPermission()
    }

    companion object {
        private const val PREVIEW_COUNT = 6
    }
}