package pfa.redouaneachak.securescope.data.repository

import android.app.ActivityManager
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.BatteryManager
import android.os.Environment
import android.os.StatFs
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import pfa.redouaneachak.securescope.data.model.HardwareStats
import javax.inject.Inject
import android.app.usage.StorageStatsManager
import android.os.storage.StorageManager
import android.provider.MediaStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pfa.redouaneachak.securescope.data.model.AppStorageUsage
import pfa.redouaneachak.securescope.data.model.StorageBreakdown
import pfa.redouaneachak.securescope.data.model.StorageCategoryUsage

class HardwareMonitorRepositoryImpl @Inject constructor(
    @ApplicationContext private val context: Context
) : HardwareMonitorRepository {

    override fun observeHardwareStats(): Flow<HardwareStats> = flow {
        while (true) {
            emit(readCurrentStats())
            delay(2000)
        }
    }

    private fun readCurrentStats(): HardwareStats {
        val activityManager = context.getSystemService(Context.ACTIVITY_SERVICE) as ActivityManager
        val memoryInfo = ActivityManager.MemoryInfo()
        activityManager.getMemoryInfo(memoryInfo)

        val batteryStatus = context.registerReceiver(null, IntentFilter(Intent.ACTION_BATTERY_CHANGED))
        val batteryLevel = batteryStatus?.getIntExtra(BatteryManager.EXTRA_LEVEL, -1) ?: -1
        val batteryScale = batteryStatus?.getIntExtra(BatteryManager.EXTRA_SCALE, -1) ?: -1
        val batteryPercent = if (batteryLevel >= 0 && batteryScale > 0) (batteryLevel * 100) / batteryScale else 0
        val batteryTemp = (batteryStatus?.getIntExtra(BatteryManager.EXTRA_TEMPERATURE, 0) ?: 0) / 10f

        val storageStat = StatFs(Environment.getDataDirectory().path)
        val totalBytes = storageStat.totalBytes
        val availableBytes = storageStat.availableBytes
        val usedBytes = totalBytes - availableBytes

        return HardwareStats(
            ramUsedMb = (memoryInfo.totalMem - memoryInfo.availMem) / (1024 * 1024),
            ramTotalMb = memoryInfo.totalMem / (1024 * 1024),
            batteryLevelPercent = batteryPercent,
            batteryTemperatureCelsius = batteryTemp,
            storageUsedGb = usedBytes / (1024f * 1024f * 1024f),
            storageTotalGb = totalBytes / (1024f * 1024f * 1024f)
        )
    }
    @Suppress("DEPRECATION")
    override suspend fun getStorageBreakdown(): StorageBreakdown = withContext(Dispatchers.IO) {
        val storageStatsManager = context.getSystemService(StorageStatsManager::class.java)
        val storageManager = context.getSystemService(StorageManager::class.java)
        val uuid = StorageManager.UUID_DEFAULT

        val externalStats = storageStatsManager.queryExternalStatsForUser(uuid, android.os.Process.myUserHandle())

        val apps = context.packageManager.getInstalledApplications(0)
        val appUsages = mutableListOf<AppStorageUsage>()
        var appsTotalBytes = 0L

        for (appInfo in apps) {
            try {
                val stats = storageStatsManager.queryStatsForUid(uuid, appInfo.uid)
                val size = stats.appBytes + stats.dataBytes + stats.cacheBytes
                appsTotalBytes += size
                appUsages.add(
                    AppStorageUsage(
                        packageName = appInfo.packageName,
                        appName = context.packageManager.getApplicationLabel(appInfo).toString(),
                        bytes = size
                    )
                )
            } catch (_: Exception) { }
        }

        StorageBreakdown(
            categories = listOf(
                StorageCategoryUsage("Apps", appsTotalBytes),
                StorageCategoryUsage("Images", externalStats.imageBytes),
                StorageCategoryUsage("Audio", externalStats.audioBytes),
                StorageCategoryUsage("Video", externalStats.videoBytes),
                StorageCategoryUsage("Other", externalStats.totalBytes - externalStats.imageBytes - externalStats.audioBytes - externalStats.videoBytes)
            ),
            appUsages = appUsages.sortedByDescending { it.bytes }
        )
    }

    private fun queryMediaStoreSize(uri: android.net.Uri): Long {
        var total = 0L
        context.contentResolver.query(uri, arrayOf(MediaStore.MediaColumns.SIZE), null, null, null)?.use { cursor ->
            val sizeIndex = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
            while (cursor.moveToNext()) total += cursor.getLong(sizeIndex)
        }
        return total
    }
}