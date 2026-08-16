package pfa.redouaneachak.securescope.ui.screens.home

import pfa.redouaneachak.securescope.data.model.AppInfo

data class HomeUiState(
    val isLoading: Boolean = true,
    val installedAppsPreview: List<AppInfo> = emptyList(),
    val installedAppsCount: Int = 0,
    val totalDataSentBytes: Long = 0,
    val totalDataReceivedBytes: Long = 0,
    val hasUsageAccessPermission: Boolean = false
)