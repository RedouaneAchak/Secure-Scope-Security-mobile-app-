package pfa.redouaneachak.securescope.data.repository

import pfa.redouaneachak.securescope.data.model.ForegroundAppInfo

interface ActiveAppsRepository {
    fun hasUsageAccessPermission(): Boolean
    fun requestUsageAccessPermission()
    suspend fun getCurrentForegroundApp(): ForegroundAppInfo?
}