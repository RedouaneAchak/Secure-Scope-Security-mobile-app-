package pfa.redouaneachak.securescope.data.remote.dto

data class VirusTotalScanResponse(
    val malwareDetected: Boolean,
    val malwareNames: List<String>,
    val vulnerabilityCount: Int,
    val fileFound: Boolean
)