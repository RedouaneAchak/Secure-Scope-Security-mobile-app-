package pfa.redouaneachak.securescope.service

import android.content.Intent
import android.net.ConnectivityManager
import android.net.VpnService
import android.os.ParcelFileDescriptor
import android.system.OsConstants
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import pfa.redouaneachak.securescope.data.model.NetworkSession
import pfa.redouaneachak.securescope.data.repository.NetworkMonitorRepository
import pfa.redouaneachak.securescope.util.DnsMessageParser
import pfa.redouaneachak.securescope.util.Ipv4PacketBuilder
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress
import java.net.InetSocketAddress
import javax.inject.Inject

@AndroidEntryPoint
class NetworkVpnService : VpnService() {

    @Inject lateinit var networkMonitorRepository: NetworkMonitorRepository

    private val serviceScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private var packetLoopJob: Job? = null
    private var tunInterface: ParcelFileDescriptor? = null

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

        val dnsServers = getConfiguredDnsServers().ifEmpty {
            listOf(InetAddress.getByName(FALLBACK_DNS))
        }

        val builder = Builder()
            .addAddress(TUNNEL_ADDRESS, 32)
            .setSession("Secure Scope DNS Monitor")
            .setMtu(1500)

        dnsServers.forEach { dns ->
            builder.addRoute(dns.hostAddress!!, 32)
            builder.addDnsServer(dns)
        }

        tunInterface = builder.establish()

        val currentInterface = tunInterface ?: return
        packetLoopJob = serviceScope.launch { runPacketLoop(currentInterface) }
    }

    private fun getConfiguredDnsServers(): List<InetAddress> {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val activeNetwork = connectivityManager.activeNetwork ?: return emptyList()
        val linkProperties = connectivityManager.getLinkProperties(activeNetwork) ?: return emptyList()
        return linkProperties.dnsServers.filter { it.address.size == 4 }
    }

    private fun stopVpn() {
        packetLoopJob?.cancel()
        packetLoopJob = null
        tunInterface?.close()
        tunInterface = null
        stopSelf()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun runPacketLoop(vpnInterface: ParcelFileDescriptor) {
        val input = FileInputStream(vpnInterface.fileDescriptor)
        val output = FileOutputStream(vpnInterface.fileDescriptor)
        val buffer = ByteArray(32767)

        while (true) {
            val length = input.read(buffer)
            if (length <= 0) continue
            handlePacket(buffer, length, output)
        }
    }

    private fun handlePacket(packet: ByteArray, length: Int, output: FileOutputStream) {
        if (length < 28) return
        val version = (packet[0].toInt() and 0xF0) shr 4
        if (version != 4) return

        val ihl = (packet[0].toInt() and 0x0F) * 4
        val protocol = packet[9].toInt() and 0xFF
        if (protocol != 17) return

        val sourceIp = InetAddress.getByAddress(packet.copyOfRange(12, 16))
        val destIp = InetAddress.getByAddress(packet.copyOfRange(16, 20))

        val sourcePort = ((packet[ihl].toInt() and 0xFF) shl 8) or (packet[ihl + 1].toInt() and 0xFF)
        val destPort = ((packet[ihl + 2].toInt() and 0xFF) shl 8) or (packet[ihl + 3].toInt() and 0xFF)

        if (destPort != 53) return

        val dnsPayload = packet.copyOfRange(ihl + 8, length)
        val domains = DnsMessageParser.extractQueriedDomains(dnsPayload)
        if (domains.isEmpty()) return

        val packageName = resolveOwningPackage(sourcePort, sourceIp, destIp, destPort)

        serviceScope.launch {
            forwardDnsQuery(dnsPayload, sourceIp, sourcePort, destIp, destPort, output)
            if (packageName != null) {
                domains.forEach { domain ->
                    networkMonitorRepository.recordSession(
                        session = NetworkSession(
                            remoteAddress = domain,
                            timestamp = System.currentTimeMillis()
                        ),
                        packageName = packageName
                    )
                }
            }
        }
    }

    private fun resolveOwningPackage(
        sourcePort: Int,
        sourceIp: InetAddress,
        destIp: InetAddress,
        destPort: Int
    ): String? {
        val connectivityManager = getSystemService(ConnectivityManager::class.java)
        val uid = try {
            connectivityManager.getConnectionOwnerUid(
                OsConstants.IPPROTO_UDP,
                InetSocketAddress(sourceIp, sourcePort),
                InetSocketAddress(destIp, destPort)
            )
        } catch (_: Exception) {
            -1
        }
        if (uid <= 0) return null
        return packageManager.getPackagesForUid(uid)?.firstOrNull()
    }

    @Suppress("BlockingMethodInNonBlockingContext")
    private fun forwardDnsQuery(
        query: ByteArray,
        clientAddress: InetAddress,
        clientPort: Int,
        realDnsServer: InetAddress,
        realDnsPort: Int,
        output: FileOutputStream
    ) {
        try {
            val socket = DatagramSocket()
            protect(socket)

            socket.send(DatagramPacket(query, query.size, realDnsServer, realDnsPort))

            val responseBuffer = ByteArray(512)
            val responsePacket = DatagramPacket(responseBuffer, responseBuffer.size)
            socket.soTimeout = 5000
            socket.receive(responsePacket)
            socket.close()

            val responseBytes = responsePacket.data.copyOfRange(0, responsePacket.length)
            val ipPacket = Ipv4PacketBuilder.buildUdpResponsePacket(
                sourceAddress = realDnsServer,
                sourcePort = realDnsPort,
                destAddress = clientAddress,
                destPort = clientPort,
                payload = responseBytes
            )
            output.write(ipPacket)
        } catch (_: Exception) {
            // query timed out or failed — app's own DNS retry logic handles it
        }
    }

    override fun onRevoke() {
        stopVpn()
        super.onRevoke()
    }

    override fun onDestroy() {
        stopVpn()
        serviceScope.cancel()
        super.onDestroy()
    }

    companion object {
        const val ACTION_STOP = "pfa.redouaneachak.securescope.action.STOP_VPN"
        private const val TUNNEL_ADDRESS = "10.0.0.2"
        private const val FALLBACK_DNS = "8.8.8.8"
    }
}