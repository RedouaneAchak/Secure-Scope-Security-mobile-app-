package pfa.redouaneachak.securescope.data.repository

import android.app.AppOpsManager
import android.app.usage.UsageStatsManager
import android.content.Context
import android.content.Intent
import android.os.Process
import android.provider.Settings
import dagger.hilt.android.qualifiers.ApplicationContext
import pfa.redouaneachak.securescope.data.model.RecentAppInfo
import javax.inject.Inject

class ActiveAppsRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : ActiveAppsRepository {

    private val usageStatsManager = context.getSystemService(Context.USAGE_STATS_SERVICE) as UsageStatsManager
    private val appOpsManager = context.getSystemService(Context.APP_OPS_SERVICE) as AppOpsManager

    override fun hasUsageAccessPermission(): Boolean {
        val mode = appOpsManager.checkOpNoThrow(
            AppOpsManager.OPSTR_GET_USAGE_STATS,
            Process.myUid(),
            context.packageName
        )
        return mode == AppOpsManager.MODE_ALLOWED
    }

    override fun requestUsageAccessPermission() {
        val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override suspend fun getRecentlyUsedApps(limit: Int): List<RecentAppInfo> {
        if (!hasUsageAccessPermission()) return emptyList()

        val endTime = System.currentTimeMillis()
        val startTime = endTime - ONE_WEEK_MS

        val usageStatsList = usageStatsManager.queryUsageStats(
            UsageStatsManager.INTERVAL_BEST,
            startTime,
            endTime
        )

        return usageStatsList
            .filter { it.lastTimeUsed > 0 }
            .sortedByDescending { it.lastTimeUsed }
            .distinctBy { it.packageName }
            .take(limit)
            .map { RecentAppInfo(it.packageName, it.lastTimeUsed) }
    }

    companion object {
        private const val ONE_WEEK_MS = 7 * 24 * 60 * 60 * 1000L
    }
}