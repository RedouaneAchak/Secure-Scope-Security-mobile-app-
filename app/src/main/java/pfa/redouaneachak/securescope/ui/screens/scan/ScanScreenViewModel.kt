package pfa.redouaneachak.securescope.ui.screens.scan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkInfo
import androidx.work.WorkManager
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import pfa.redouaneachak.securescope.data.local.ScanControlHolder
import pfa.redouaneachak.securescope.data.repository.AppRepository
import pfa.redouaneachak.securescope.data.repository.SecurityScanRepository
import javax.inject.Inject

@HiltViewModel
class ScanViewModel @Inject constructor(
    private val securityScanRepository: SecurityScanRepository,
    private val appRepository: AppRepository,
    private val workManager: WorkManager,
    private val scanControlHolder: ScanControlHolder
) : ViewModel() {

    private val _uiState = MutableStateFlow(ScanUiState())
    val uiState: StateFlow<ScanUiState> = _uiState.asStateFlow()

    init {
        loadLastResults()
        observeExistingWork()
        observePauseState()
    }

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

    private fun observeExistingWork() {
        viewModelScope.launch {
            workManager.getWorkInfosForUniqueWorkFlow(pfa.redouaneachak.securescope.service.ScanWorker.WORK_NAME).collect { infos ->
                val info = infos.firstOrNull() ?: return@collect
                handleWorkInfo(info)
            }
        }
    }

    private fun observePauseState() {
        viewModelScope.launch {
            scanControlHolder.isPaused.collect { paused ->
                _uiState.update { it.copy(isPaused = paused) }
            }
        }
    }

    fun startScan() {
        _uiState.update { it.copy(phase = ScanPhase.SCANNING, progressCurrent = 0, progressTotal = 0, isPaused = false) }
        val request = OneTimeWorkRequestBuilder<pfa.redouaneachak.securescope.service.ScanWorker>().build()
        workManager.enqueueUniqueWork(
            pfa.redouaneachak.securescope.service.ScanWorker.WORK_NAME,
            ExistingWorkPolicy.KEEP,
            request
        )
    }

    fun togglePause() {
        scanControlHolder.togglePause()
    }

    fun cancelScan() {
        workManager.cancelUniqueWork(pfa.redouaneachak.securescope.service.ScanWorker.WORK_NAME)
        scanControlHolder.reset()
        loadLastResults()
    }

    private fun handleWorkInfo(info: WorkInfo) {
        when (info.state) {
            WorkInfo.State.RUNNING -> {
                val current = info.progress.getInt(pfa.redouaneachak.securescope.service.ScanWorker.KEY_CURRENT, 0)
                val total = info.progress.getInt(pfa.redouaneachak.securescope.service.ScanWorker.KEY_TOTAL, 0)
                _uiState.update { it.copy(phase = ScanPhase.SCANNING, progressCurrent = current, progressTotal = total) }
            }
            WorkInfo.State.SUCCEEDED -> loadLastResults()
            WorkInfo.State.FAILED, WorkInfo.State.CANCELLED -> loadLastResults()
            else -> Unit
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