package pfa.redouaneachak.securescope.data.model

data class NetworkSession(
    val remoteAddress: String,
    val remotePort: Int,
    val bytesSent: Long,
    val bytesReceived: Long,
    val sessionStartTimestamp: Long,
    val sessionEndTimestamp: Long?
)