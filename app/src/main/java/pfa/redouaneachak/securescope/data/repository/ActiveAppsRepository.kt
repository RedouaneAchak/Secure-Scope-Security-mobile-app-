package pfa.redouaneachak.securescope.data.repository

import pfa.redouaneachak.securescope.data.model.RecentAppInfo

interface ActiveAppsRepository {
    fun hasUsageAccessPermission(): Boolean
    fun requestUsageAccessPermission()
    suspend fun getRecentlyUsedApps(limit: Int = 20): List<RecentAppInfo>
}