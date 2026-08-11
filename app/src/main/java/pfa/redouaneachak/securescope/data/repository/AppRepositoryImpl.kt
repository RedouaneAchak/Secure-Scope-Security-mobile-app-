package pfa.redouaneachak.securescope.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import dagger.hilt.android.qualifiers.ApplicationContext
import pfa.redouaneachak.securescope.data.model.AppInfo
import pfa.redouaneachak.securescope.data.model.PermissionInfo
import javax.inject.Inject

class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppRepository {

    private val packageManager: PackageManager = context.packageManager

    override suspend fun getInstalledApps(): List<AppInfo> {
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }
        return packages.mapNotNull { it.toDomainModel() }
    }

    override suspend fun getAppByPackageName(packageName: String): AppInfo? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            appInfo.toDomainModel()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    override suspend fun getPermissionsForApp(packageName: String): List<PermissionInfo> {
        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(
                    packageName,
                    PackageManager.PackageInfoFlags.of(PackageManager.GET_PERMISSIONS.toLong())
                )
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, PackageManager.GET_PERMISSIONS)
            }
        } catch (_: PackageManager.NameNotFoundException) {
            return emptyList()
        }

        val requestedPermissions = packageInfo.requestedPermissions ?: return emptyList()
        val grantedFlags = packageInfo.requestedPermissionsFlags

        return requestedPermissions.mapIndexedNotNull { index, permissionName ->
            val flagValue = grantedFlags?.getOrNull(index) ?: 0
            val isGranted = (flagValue and PackageInfo.REQUESTED_PERMISSION_GRANTED) != 0

            try {
                val permInfo = packageManager.getPermissionInfo(permissionName, 0)
                PermissionInfo(
                    name = permissionName,
                    label = permInfo.loadLabel(packageManager).toString(),
                    isGranted = isGranted,
                    isDangerous = permInfo.protection == android.content.pm.PermissionInfo.PROTECTION_DANGEROUS
                )
            } catch (_: PackageManager.NameNotFoundException) {
                null
            }
        }
    }

    override suspend fun uninstallApp(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = "package:$packageName".toUri()
        }
        context.startActivity(intent)
    }

    override suspend fun forceStopApp(packageName: String) {
        openAppSettings(packageName)
    }

    override suspend fun revokePermission(packageName: String, permissionName: String) {
        openAppSettings(packageName)
    }

    private fun openAppSettings(packageName: String) {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
            data = "package:$packageName".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        }
        context.startActivity(intent)
    }

    override suspend fun isSideloaded(packageName: String): Boolean {
        val installerPackage = try {
            packageManager.getInstallSourceInfo(packageName).installingPackageName
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
        return installerPackage != "com.android.vending"
    }

    private fun ApplicationInfo.toDomainModel(): AppInfo? {
        return try {
            val packageInfo = packageManager.getPackageInfo(packageName, 0)
            AppInfo(
                packageName = packageName,
                appName = packageManager.getApplicationLabel(this).toString(),
                icon = packageManager.getApplicationIcon(packageName),
                versionName = packageInfo.versionName ?: "unknown",
                versionCode = packageInfo.longVersionCode,
                isSystemApp = (flags and ApplicationInfo.FLAG_SYSTEM) != 0,
                installedTimestamp = packageInfo.firstInstallTime,
                lastUpdatedTimestamp = packageInfo.lastUpdateTime
            )
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }
}