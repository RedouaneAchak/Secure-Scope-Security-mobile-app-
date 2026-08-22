package pfa.redouaneachak.securescope.ui.screens.scan

import pfa.redouaneachak.securescope.data.model.ScanResult

enum class ScanPhase { LOADING, IDLE, SCANNING, RESULTS }

data class ScanUiState(
    val phase: ScanPhase = ScanPhase.LOADING,
    val progressCurrent: Int = 0,
    val progressTotal: Int = 0,
    val currentAppName: String = "",
    val results: List<ScanResult> = emptyList(),
    val expandedPackageName: String? = null
)