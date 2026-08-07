package pfa.redouaneachak.securescope.data.model

import android.graphics.drawable.Drawable

data class AppInfo(
    val packageName: String,
    val appName: String,
    val icon: Drawable,
    val versionName: String,
    val versionCode: Long,
    val isSystemApp: Boolean,
    val installedTimestamp: Long,
    val lastUpdatedTimestamp: Long
)