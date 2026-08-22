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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
class AppRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : AppRepository {

    private val packageManager: PackageManager = context.packageManager

    override suspend fun getInstalledApps(): List<AppInfo> = withContext(Dispatchers.IO) {
        val packages = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            packageManager.getInstalledApplications(
                PackageManager.ApplicationInfoFlags.of(PackageManager.GET_META_DATA.toLong())
            )
        } else {
            @Suppress("DEPRECATION")
            packageManager.getInstalledApplications(PackageManager.GET_META_DATA)
        }
        packages.mapNotNull { it.toDomainModel() }.sortedBy { it.appName.lowercase() }
    }

    override suspend fun getAppByPackageName(packageName: String): AppInfo? = withContext(Dispatchers.IO) {
        try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            appInfo.toDomainModel()
        } catch (_: PackageManager.NameNotFoundException) {
            null
        }
    }

    override suspend fun getPermissionsForApp(packageName: String): List<PermissionInfo> = withContext(Dispatchers.IO) {
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
            return@withContext emptyList()
        }

        val requestedPermissions = packageInfo.requestedPermissions ?: return@withContext emptyList()
        val grantedFlags = packageInfo.requestedPermissionsFlags

        return@withContext requestedPermissions.mapIndexedNotNull { index, permissionName ->
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

    override suspend fun getInstallSource(packageName: String): String = withContext(Dispatchers.IO) {
        val installerPackage = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                packageManager.getInstallSourceInfo(packageName).installingPackageName
            } else {
                @Suppress("DEPRECATION")
                packageManager.getInstallerPackageName(packageName)
            }
        } catch (_: Exception) {
            null
        }

        when (installerPackage) {
            null -> "Unknown / Sideloaded"
            "com.android.vending" -> "Google Play Store"
            else -> try {
                val installerAppInfo = packageManager.getApplicationInfo(installerPackage, 0)
                packageManager.getApplicationLabel(installerAppInfo).toString()
            } catch (_: Exception) {
                installerPackage
            }
        }
    }

    override suspend fun uninstallApp(packageName: String) {
        val intent = Intent(Intent.ACTION_DELETE).apply {
            data = "package:$packageName".toUri()
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
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