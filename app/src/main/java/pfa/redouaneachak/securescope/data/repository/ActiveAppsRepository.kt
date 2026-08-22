package pfa.redouaneachak.securescope.data.repository

import pfa.redouaneachak.securescope.data.model.RecentAppInfo
import kotlinx.coroutines.flow.Flow

interface ActiveAppsRepository {
    fun hasUsageAccessPermission(): Boolean
    fun requestUsageAccessPermission()
    suspend fun getRecentlyUsedApps(limit: Int = 20): List<RecentAppInfo>
    fun observeRecentlyUsedApps(limit: Int = 20): Flow<List<RecentAppInfo>>
}