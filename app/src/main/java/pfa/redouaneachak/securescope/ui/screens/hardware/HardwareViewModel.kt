package pfa.redouaneachak.securescope.ui.screens.hardware

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.*
import kotlinx.coroutines.launch
import pfa.redouaneachak.securescope.data.model.HardwareStats
import pfa.redouaneachak.securescope.data.model.StorageBreakdown
import pfa.redouaneachak.securescope.data.repository.HardwareMonitorRepository
import javax.inject.Inject

@HiltViewModel
class HardwareViewModel @Inject constructor(
    private val hardwareMonitorRepository: HardwareMonitorRepository
) : ViewModel() {

    val stats: StateFlow<HardwareStats?> = hardwareMonitorRepository.observeHardwareStats()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _storageBreakdown = MutableStateFlow<StorageBreakdown?>(null)
    val storageBreakdown: StateFlow<StorageBreakdown?> = _storageBreakdown.asStateFlow()

    private val _appsExpanded = MutableStateFlow(false)
    val appsExpanded: StateFlow<Boolean> = _appsExpanded.asStateFlow()

    fun loadStorageBreakdown() {
        viewModelScope.launch {
            _storageBreakdown.value = hardwareMonitorRepository.getStorageBreakdown()
        }
    }

    fun toggleAppsExpanded() {
        _appsExpanded.update { !it }
    }
}