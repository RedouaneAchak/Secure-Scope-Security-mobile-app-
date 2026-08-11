package pfa.redouaneachak.securescope.data.model

data class HardwareStats(
    val ramUsedMb: Long,
    val ramTotalMb: Long,
    val batteryLevelPercent: Int,
    val batteryTemperatureCelsius: Float,
    val storageUsedGb: Float,
    val storageTotalGb: Float
)