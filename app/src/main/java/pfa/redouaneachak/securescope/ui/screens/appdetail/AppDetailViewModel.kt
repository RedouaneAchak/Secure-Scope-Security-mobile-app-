package pfa.redouaneachak.securescope.ui.screens.appdetail

import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import pfa.redouaneachak.securescope.data.repository.AppRepository
import pfa.redouaneachak.securescope.data.repository.NetworkMonitorRepository
import pfa.redouaneachak.securescope.data.repository.SecurityScanRepository
import javax.inject.Inject

@HiltViewModel
class AppDetailViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val appRepository: AppRepository,
    private val securityScanRepository: SecurityScanRepository,
    private val networkMonitorRepository: NetworkMonitorRepository
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle["packageName"])

    private val _uiState = MutableStateFlow(AppDetailUiState())
    val uiState: StateFlow<AppDetailUiState> = _uiState.asStateFlow()

    init { load() }

    fun load() {
        viewModelScope.launch {
            _uiState.update { it.copy(isLoading = true) }

            val app = appRepository.getAppByPackageName(packageName)
            val icon = app?.let { withContext(Dispatchers.Default) { it.icon.toBitmap().asImageBitmap() } }
            val installSource = appRepository.getInstallSource(packageName)
            val permissions = appRepository.getPermissionsForApp(packageName)
            val latestScans = securityScanRepository.getLatestScanResults().first()
            val scanResult = latestScans.find { it.app.packageName == packageName }
            val servers = networkMonitorRepository.getContactedServers(packageName)

            _uiState.update {
                it.copy(
                    isLoading = false,
                    app = app,
                    icon = icon,
                    installSource = installSource,
                    permissions = permissions,
                    lastScanResult = scanResult,
                    contactedServers = servers
                )
            }
        }
    }

    fun scanNow() {
        val app = _uiState.value.app ?: return
        if (app.isSystemApp) return

        viewModelScope.launch {
            _uiState.update { it.copy(isScanning = true) }
            securityScanRepository.scanSingleApp(app.packageName)
            load()
            _uiState.update { it.copy(isScanning = false) }
        }
    }

    fun uninstall() {
        viewModelScope.launch { appRepository.uninstallApp(packageName) }
    }

    fun forceStop() {
        viewModelScope.launch { appRepository.forceStopApp(packageName) }
    }
}