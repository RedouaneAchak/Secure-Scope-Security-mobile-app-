package pfa.redouaneachak.securescope.ui.screens.applist

import androidx.compose.ui.graphics.ImageBitmap
import pfa.redouaneachak.securescope.data.model.AppInfo

enum class AppSortOption(val label: String) {
    NAME_AZ("Name (A-Z)"),
    INSTALL_DATE_NEWEST("Recently Installed")
}

data class AppListRow(val app: AppInfo, val icon: ImageBitmap)

data class AppListUiState(
    val isLoading: Boolean = true,
    val searchQuery: String = "",
    val sortOption: AppSortOption = AppSortOption.NAME_AZ,
    val displayedApps: List<AppListRow> = emptyList(),
    val typeFilter: AppTypeFilter = AppTypeFilter.ALL
)
enum class AppTypeFilter(val label: String) {
    ALL("All Apps"),
    USER("User Apps"),
    SYSTEM("System Apps")
}