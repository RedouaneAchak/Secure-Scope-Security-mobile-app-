package pfa.redouaneachak.securescope.ui.screens.networkscan

import pfa.redouaneachak.securescope.data.model.NetworkSessionWithApp

data class NetworkScanUiState(
    val isVpnActive: Boolean = false,
    val recentSessions: List<NetworkSessionWithApp> = emptyList()
)