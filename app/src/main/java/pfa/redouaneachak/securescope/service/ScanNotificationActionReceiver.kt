package pfa.redouaneachak.securescope.service

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.work.WorkManager
import dagger.hilt.android.AndroidEntryPoint
import pfa.redouaneachak.securescope.data.local.ScanControlHolder
import javax.inject.Inject

@AndroidEntryPoint
class ScanNotificationActionReceiver : BroadcastReceiver() {

    @Inject lateinit var scanControlHolder: ScanControlHolder
    @Inject lateinit var workManager: WorkManager

    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            ACTION_TOGGLE_PAUSE -> scanControlHolder.togglePause()
            ACTION_CANCEL -> workManager.cancelUniqueWork(ScanWorker.WORK_NAME)
        }
    }

    companion object {
        const val ACTION_TOGGLE_PAUSE = "pfa.redouaneachak.securescope.action.SCAN_TOGGLE_PAUSE"
        const val ACTION_CANCEL = "pfa.redouaneachak.securescope.action.SCAN_CANCEL"
    }
}