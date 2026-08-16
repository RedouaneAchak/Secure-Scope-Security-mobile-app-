package pfa.redouaneachak.securescope.data.model

data class AppDataUsage(
    val packageName: String,
    val totalSentBytes: Long,
    val totalReceivedBytes: Long
)