package pfa.redouaneachak.securescope.data.model

data class StorageCategoryUsage(val category: String, val bytes: Long)
data class AppStorageUsage(val packageName: String, val appName: String, val bytes: Long)

data class StorageBreakdown(
    val categories: List<StorageCategoryUsage>,
    val appUsages: List<AppStorageUsage>
)