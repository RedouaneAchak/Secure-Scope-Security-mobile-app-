package pfa.redouaneachak.securescope.data.model

data class ScanResult(
    val app: AppInfo,
    val trackerCount: Int,
    val detectedTrackers: List<TrackerInfo>,
    val malwareDetected: Boolean,
    val malwareNames: List<String>,
    val vulnerabilitiesFound: Int,
    val riskScore: RiskScore,
    val scanTimestamp: Long,
    val cloudVerified: Boolean
)