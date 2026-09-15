package pfa.redouaneachak.securescope.service

import android.app.Notification
import android.app.PendingIntent
import android.content.Intent
import android.net.ConnectivityManager
import android.net.ProxyInfo
import android.net.VpnService
import android.os.ParcelFileDescriptor
import androidx.core.app.NotificationCompat
import dagger.hilt.android.AndroidEntryPoint
import pfa.redouaneachak.securescope.MainActivity
import pfa.redouaneachak.securescope.R
import pfa.redouaneachak.securescope.SecureScopeApp
import pfa.redouaneachak.securescope.data.local.BlockStatsHolder
import pfa.redouaneachak.securescope.data.local.VpnStateHolder
import pfa.redouaneachak.securescope.data.repository.NetworkMonitorRepository
import pfa.redouaneachak.securescope.util.BlocklistProvider
import javax.inject.Inject

@AndroidEntryPoint
class NetworkVpnService : VpnService() {

    @Inject lateinit var networkMonitorRepository: NetworkMonitorRepository
    @Inject lateinit var blocklistProvider: BlocklistProvider
    @Inject lateinit var vpnStateHolder: VpnStateHolder
    @Inject lateinit var blockStatsHolder: BlockStatsHolder

    private var tunInterface: ParcelFileDescriptor? = null
    private var httpProxyServer: HttpProxyServer? = null

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_STOP) {
            stopVpn()
            return START_NOT_STICKY
        }
        startVpn()
        return START_STICKY
    }

    private fun startVpn() {
        if (tunInterface != null) return

        startForeground(NOTIFICATION_ID, buildNotification())

        tunInterface = Builder()
            .addAddress(TUNNEL_ADDRESS, 32)
            .setSession("Secure Scope Network Protection")
            .setMtu(1500)
            .setHttpProxy(ProxyInfo.buildDirectProxy("127.0.0.1", PROXY_PORT))
            .establish()

        if (tunInterface == null) {
            stopForeground(STOP_FOREGROUND_REMOVE)
            return
        }

        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val proxy = HttpProxyServer(
            vpnService = this,
            connectivityManager = connectivityManager,
            packageManager = packageManager,
            blocklistProvider = blocklistProvider,
            networkMonitorRepository = networkMonitorRepository,
            blockStatsHolder = blockStatsHolder,
            port = PROXY_PORT
        )
        proxy.start()
        httpProxyServer = proxy
        vpnStateHolder.setActive(true)
    }

    private fun stopVpn() {
        httpProxyServer?.stop()
        httpProxyServer = null
        tunInterface?.close()
        tunInterface = null
        vpnStateHolder.setActive(false)
        stopForeground(STOP_FOREGROUND_REMOVE)
        stopSelf()
    }

    private fun buildNotification(): Notification {
        val stopIntent = Intent(this, NetworkVpnService::class.java).apply { action = ACTION_STOP }
        val stopPendingIntent = PendingIntent.getService(this, 0, stopIntent, PendingIntent.FLAG_IMMUTABLE)

        val openAppIntent = Intent(this, MainActivity::class.java).apply {
            putExtra(MainActivity.EXTRA_DESTINATION, "network_scan")
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val openAppPendingIntent = PendingIntent.getActivity(
            this, 0, openAppIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        return NotificationCompat.Builder(this, SecureScopeApp.CHANNEL_ID)
            .setContentTitle("Proxy is running")
            .setContentText("Secure Scope is protecting your network traffic")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentIntent(openAppPendingIntent)
            .setOngoing(true)
            .addAction(0, "Stop", stopPendingIntent)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()
    }

    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopVpn()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "pfa.redouaneachak.securescope.action.STOP_VPN"
        private const val TUNNEL_ADDRESS = "10.0.0.2"
        private const val PROXY_PORT = 8877
        private const val NOTIFICATION_ID = 42
    }
}