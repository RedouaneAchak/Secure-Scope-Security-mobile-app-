package pfa.redouaneachak.securescope.ui.screens.datausage

import pfa.redouaneachak.securescope.data.model.AppInfo

enum class DataUsageRange(val label: String, val millis: Long?) {
    TODAY("Today", 24 * 60 * 60 * 1000L),
    LAST_WEEK("Last Week", 7 * 24 * 60 * 60 * 1000L),
    LAST_MONTH("Last Month", 30L * 24 * 60 * 60 * 1000L),
    ALWAYS("Always", null)
}

data class DataUsageRow(
    val app: AppInfo,
    val sentBytes: Long,
    val receivedBytes: Long
)

data class DataUsageUiState(
    val isLoading: Boolean = true,
    val hasPermission: Boolean = false,
    val selectedRange: DataUsageRange = DataUsageRange.LAST_WEEK,
    val rows: List<DataUsageRow> = emptyList()
)