package pfa.redouaneachak.securescope.service

import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.hilt.work.HiltWorker
import androidx.work.CoroutineWorker
import androidx.work.ForegroundInfo
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import dagger.assisted.Assisted
import dagger.assisted.AssistedInject
import kotlinx.coroutines.flow.first
import pfa.redouaneachak.securescope.MainActivity
import pfa.redouaneachak.securescope.R
import pfa.redouaneachak.securescope.SecureScopeApp
import pfa.redouaneachak.securescope.data.local.ScanControlHolder
import pfa.redouaneachak.securescope.data.model.ScanProgress
import pfa.redouaneachak.securescope.data.repository.SecurityScanRepository

@HiltWorker
class ScanWorker @AssistedInject constructor(
    @Assisted context: Context,
    @Assisted params: WorkerParameters,
    private val securityScanRepository: SecurityScanRepository,
    private val scanControlHolder: ScanControlHolder
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        scanControlHolder.reset()

        securityScanRepository.scanAllApps().collect { progress ->
            when (progress) {
                is ScanProgress.InProgress -> {
                    setProgress(workDataOf(KEY_CURRENT to progress.current, KEY_TOTAL to progress.total))
                    setForeground(buildForegroundInfo(progress.current, progress.total, progress.currentAppName, isPaused = false))

                    if (scanControlHolder.isPaused.value) {
                        setForeground(buildForegroundInfo(progress.current, progress.total, progress.currentAppName, isPaused = true))
                        scanControlHolder.isPaused.first { paused -> !paused }
                        setForeground(buildForegroundInfo(progress.current, progress.total, progress.currentAppName, isPaused = false))
                    }
                }
                is ScanProgress.Completed -> Unit
            }
        }

        scanControlHolder.reset()
        return Result.success()
    }

    private fun buildForegroundInfo(current: Int, total: Int, currentAppName: String, isPaused: Boolean): ForegroundInfo {
        val openAppIntent = Intent(applicationContext, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_DESTINATION, "scan")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val contentPendingIntent = PendingIntent.getActivity(
            applicationContext, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val toggleIntent = Intent(applicationContext, ScanNotificationActionReceiver::class.java).apply {
            action = ScanNotificationActionReceiver.ACTION_TOGGLE_PAUSE
        }
        val togglePendingIntent = PendingIntent.getBroadcast(
            applicationContext, 1, toggleIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val cancelIntent = Intent(applicationContext, ScanNotificationActionReceiver::class.java).apply {
            action = ScanNotificationActionReceiver.ACTION_CANCEL
        }
        val cancelPendingIntent = PendingIntent.getBroadcast(
            applicationContext, 2, cancelIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val statusText = if (isPaused) "Paused — $current of $total" else "$current of $total — $currentAppName"

        val notification = NotificationCompat.Builder(applicationContext, SecureScopeApp.SCAN_CHANNEL_ID)
            .setContentTitle(if (isPaused) "Scan paused" else "Scanning apps")
            .setContentText(statusText)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(contentPendingIntent)
            .setProgress(total, current, false)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .addAction(0, if (isPaused) "Resume" else "Pause", togglePendingIntent)
            .addAction(0, "Cancel", cancelPendingIntent)
            .build()

        return ForegroundInfo(NOTIFICATION_ID, notification)
    }

    companion object {
        const val KEY_CURRENT = "current"
        const val KEY_TOTAL = "total"
        const val WORK_NAME = "scan_all_apps"
        private const val NOTIFICATION_ID = 77
    }
}