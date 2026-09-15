package pfa.redouaneachak.securescope.ui.screens.networkscan

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import pfa.redouaneachak.securescope.data.model.BlockedDomain
import pfa.redouaneachak.securescope.data.repository.NetworkMonitorRepository
import javax.inject.Inject

@HiltViewModel
class NetworkScanViewModel @Inject constructor(
    networkMonitorRepository: NetworkMonitorRepository
) : ViewModel() {

    val uiState: StateFlow<NetworkScanUiState> = combine(
        networkMonitorRepository.isVpnActive(),
        networkMonitorRepository.observeRecentSessions()
    ) { active, sessions ->
        NetworkScanUiState(isVpnActive = active, recentSessions = sessions)
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), NetworkScanUiState())

    val blockedDomains: StateFlow<List<BlockedDomain>> = networkMonitorRepository.observeBlockedDomains()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())
}