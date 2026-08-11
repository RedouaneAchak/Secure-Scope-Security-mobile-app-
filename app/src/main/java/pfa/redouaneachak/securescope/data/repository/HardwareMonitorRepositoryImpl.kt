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
}