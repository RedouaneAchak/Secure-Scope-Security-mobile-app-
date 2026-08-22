package pfa.redouaneachak.securescope.ui.screens.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pfa.redouaneachak.securescope.data.model.ScanProgress
import pfa.redouaneachak.securescope.data.repository.AppRepository
import pfa.redouaneachak.securescope.data.repository.SecurityScanRepository
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val securityScanRepository: SecurityScanRepository,
    private val appRepository: AppRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    init { loadLastResults() }

    fun loadLastResults() {
        viewModelScope.launch {
            _uiState.update { it.copy(phase = ScanPhase.LOADING) }
            val results = securityScanRepository.getLatestScanResults().first()
                .sortedByDescending { it.riskScore.ordinal }
            _uiState.update {
                it.copy(phase = if (results.isEmpty()) ScanPhase.IDLE else ScanPhase.RESULTS, results = results)
            }
        }
    }

    fun startScan() {
        viewModelScope.launch {
            _uiState.update { it.copy(phase = ScanPhase.SCANNING, progressCurrent = 0, progressTotal = 0) }
            securityScanRepository.scanAllApps().collect { progress ->
                when (progress) {
                    is ScanProgress.InProgress -> _uiState.update {
                        it.copy(
                            progressCurrent = progress.current,
                            progressTotal = progress.total,
                            currentAppName = progress.currentAppName
                        )
                    }
                    is ScanProgress.Completed -> _uiState.update {
                        it.copy(
                            phase = ScanPhase.RESULTS,
                            results = progress.results.sortedByDescending { r -> r.riskScore.ordinal }
                        )
                    }
                }
            }
        }
    }

    fun toggleExpanded(packageName: String) {
        _uiState.update {
            it.copy(expandedPackageName = if (it.expandedPackageName == packageName) null else packageName)
        }
    }

    fun uninstall(packageName: String) {
        viewModelScope.launch { appRepository.uninstallApp(packageName) }
    }

    fun forceStop(packageName: String) {
        viewModelScope.launch { appRepository.forceStopApp(packageName) }
    }
}