package pfa.redouaneachak.securescope.ui.screens.appdetail

import androidx.compose.ui.graphics.ImageBitmap
import pfa.redouaneachak.securescope.data.model.AppInfo
import pfa.redouaneachak.securescope.data.model.PermissionInfo
import pfa.redouaneachak.securescope.data.model.ScanResult

data class AppDetailUiState(
    val isLoading: Boolean = true,
    val app: AppInfo? = null,
    val icon: ImageBitmap? = null,
    val installSource: String? = null,
    val permissions: List<PermissionInfo> = emptyList(),
    val lastScanResult: ScanResult? = null,
    val contactedServers: List<String> = emptyList(),
    val isScanning: Boolean = false
)