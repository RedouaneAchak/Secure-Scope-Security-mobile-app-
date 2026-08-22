package pfa.redouaneachak.securescope.data.model

sealed interface ScanProgress {
    data class InProgress(val current: Int, val total: Int, val currentAppName: String) : ScanProgress
    data class Completed(val results: List<ScanResult>) : ScanProgress
}