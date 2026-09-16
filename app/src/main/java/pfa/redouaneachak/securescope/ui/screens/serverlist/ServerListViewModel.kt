package pfa.redouaneachak.securescope.ui.screens.serverlist

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import pfa.redouaneachak.securescope.data.repository.NetworkMonitorRepository
import javax.inject.Inject

@HiltViewModel
class ServerListViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val networkMonitorRepository: NetworkMonitorRepository
) : ViewModel() {

    private val packageName: String = checkNotNull(savedStateHandle["packageName"])

    private val _servers = MutableStateFlow<List<String>>(emptyList())
    val servers: StateFlow<List<String>> = _servers.asStateFlow()

    init {
        viewModelScope.launch {
            _servers.value = networkMonitorRepository.getContactedServers(packageName)
        }
    }
}