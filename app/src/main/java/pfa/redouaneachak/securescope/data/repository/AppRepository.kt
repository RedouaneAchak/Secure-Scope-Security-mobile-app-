package pfa.redouaneachak.securescope.data.repository

import pfa.redouaneachak.securescope.data.model.AppInfo
import pfa.redouaneachak.securescope.data.model.PermissionInfo

interface AppRepository {
    suspend fun getInstalledApps(): List<AppInfo>
    suspend fun getAppByPackageName(packageName: String): AppInfo?
    suspend fun getPermissionsForApp(packageName: String): List<PermissionInfo>
    suspend fun uninstallApp(packageName: String)
    suspend fun forceStopApp(packageName: String)
    suspend fun revokePermission(packageName: String, permissionName: String)
    suspend fun isSideloaded(packageName: String): Boolean
}