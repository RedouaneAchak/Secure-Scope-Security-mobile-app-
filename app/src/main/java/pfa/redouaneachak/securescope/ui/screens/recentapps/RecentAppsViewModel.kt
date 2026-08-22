package pfa.redouaneachak.securescope.ui.screens.recentapps

import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pfa.redouaneachak.securescope.data.model.RecentAppInfo
import pfa.redouaneachak.securescope.data.repository.ActiveAppsRepository
import pfa.redouaneachak.securescope.data.repository.AppRepository
import javax.inject.Inject

data class RecentAppRow(val info: RecentAppInfo, val appName: String, val icon: ImageBitmap)

data class RecentAppsUiState(
    val isLoading: Boolean = true,
    val hasPermission: Boolean = false,
    val rows: List<RecentAppRow> = emptyList()
)

@HiltViewModel
class RecentAppsViewModel @Inject constructor(
    private val activeAppsRepository: ActiveAppsRepository,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(RecentAppsUiState())
    val uiState: StateFlow<RecentAppsUiState> = _uiState.asStateFlow()

    init { observe() }

    private fun observe() {
        val hasPermission = activeAppsRepository.hasUsageAccessPermission()
        _uiState.update { it.copy(hasPermission = hasPermission) }
        if (!hasPermission) {
            _uiState.update { it.copy(isLoading = false) }
            return
        }

        viewModelScope.launch {
            activeAppsRepository.observeRecentlyUsedApps().collect { infos ->
                val rows = withContext(Dispatchers.Default) {
                    infos.mapNotNull { info ->
                        val app = appRepository.getAppByPackageName(info.packageName) ?: return@mapNotNull null
                        RecentAppRow(info, app.appName, app.icon.toBitmap().asImageBitmap())
                    }
                }
                _uiState.update { it.copy(isLoading = false, rows = rows) }
            }
        }
    }

    fun requestPermission() = activeAppsRepository.requestUsageAccessPermission()

    fun recheckPermission() = observe()
}