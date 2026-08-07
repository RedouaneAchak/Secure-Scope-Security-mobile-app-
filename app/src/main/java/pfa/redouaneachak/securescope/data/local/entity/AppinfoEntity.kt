package pfa.redouaneachak.securescope.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "app_info")
data class AppInfoEntity(
    @PrimaryKey
    val packageName: String,

    val appName: String,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val installedTimestamp: Long,
    val lastUpdatedTimestamp: Long
)