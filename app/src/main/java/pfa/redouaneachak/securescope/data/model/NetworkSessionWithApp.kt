package pfa.redouaneachak.securescope.data.model

data class NetworkSessionWithApp(
    val packageName: String,
    val appName: String,
    val remoteAddress: String,
    val timestamp: Long,
    val isSuspicious: Boolean
)