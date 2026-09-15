package pfa.redouaneachak.securescope.service

import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.net.VpnService
import android.system.OsConstants
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import pfa.redouaneachak.securescope.data.local.BlockStatsHolder
import pfa.redouaneachak.securescope.data.model.NetworkSession
import pfa.redouaneachak.securescope.data.repository.NetworkMonitorRepository
import pfa.redouaneachak.securescope.util.BlocklistProvider
import java.io.BufferedInputStream
import java.io.ByteArrayOutputStream
import java.io.InputStream
import java.net.InetAddress
import java.net.InetSocketAddress
import java.net.ServerSocket
import java.net.Socket

class HttpProxyServer(
    private val vpnService: VpnService,
    private val connectivityManager: ConnectivityManager,
    private val packageManager: PackageManager,
    private val blocklistProvider: BlocklistProvider,
    private val networkMonitorRepository: NetworkMonitorRepository,
    private val blockStatsHolder: BlockStatsHolder,
    private val port: Int
) {
    private val scope = CoroutineScope(Dispatchers.IO + Job())
    private var serverSocket: ServerSocket? = null

    fun start() {
        val socket = ServerSocket(port, 50, InetAddress.getByName("127.0.0.1"))
        serverSocket = socket
        scope.launch {
            while (true) {
                val client = try { socket.accept() } catch (_: Exception) { break }
                scope.launch { handleClient(client) }
            }
        }
    }

    fun stop() {
        serverSocket?.close()
        scope.coroutineContext[Job]?.cancel()
    }

    private fun readLineRaw(input: InputStream): String? {
        val buffer = ByteArrayOutputStream()
        var byteRead: Int
        var sawAny = false
        while (input.read().also { byteRead = it } != -1) {
            sawAny = true
            if (byteRead == '\n'.code) break
            if (byteRead != '\r'.code) buffer.write(byteRead)
        }
        return if (!sawAny) null else buffer.toString(Charsets.ISO_8859_1.name())
    }

    private fun handleClient(client: Socket) {
        try {
            val input = BufferedInputStream(client.getInputStream())
            val requestLine = readLineRaw(input) ?: run { client.close(); return }

            val target = parseTarget(requestLine, input) ?: run { client.close(); return }
            val (host, targetPort) = target
            android.util.Log.d("SecureScope", "checking host=$host isBlocked=${blocklistProvider.isBlocked(host)}")
            val packageName = resolveOwningPackage(client)

            if (blocklistProvider.isBlocked(host)) {
                client.close()
                val category = blocklistProvider.getBlockedCategory(host) ?: "unknown"
                blockStatsHolder.recordBlocked(host, category)
                logSession(host, packageName)
                return
            }

            logSession(host, packageName)

            if (requestLine.startsWith("CONNECT")) {
                client.getOutputStream().write("HTTP/1.1 200 Connection Established\r\n\r\n".toByteArray())
            }

            val remote = Socket()
            vpnService.protect(remote)
            remote.connect(InetSocketAddress(host, targetPort), 5000)

            relay(client, input, remote)
        } catch (_: Exception) {
            client.close()
        }
    }

    private fun parseTarget(requestLine: String, input: InputStream): Pair<String, Int>? {
        return if (requestLine.startsWith("CONNECT")) {
            val target = requestLine.removePrefix("CONNECT ").substringBefore(" ")
            val parts = target.split(":")
            val host = parts[0]
            val port = parts.getOrNull(1)?.toIntOrNull() ?: 443

            var line = readLineRaw(input)
            while (!line.isNullOrEmpty()) {
                line = readLineRaw(input)
            }

            host to port
        } else {
            var line = readLineRaw(input)
            var hostFromHeader: String? = null
            while (line != null && line.isNotBlank()) {
                if (hostFromHeader == null && line.startsWith("Host:", ignoreCase = true)) {
                    hostFromHeader = line.removePrefix("Host:").trim()
                }
                line = readLineRaw(input)
            }
            hostFromHeader?.let { it to 80 }
        }
    }

    private fun resolveOwningPackage(client: Socket): String? {
        return try {
            val appEndpoint = client.remoteSocketAddress as? InetSocketAddress ?: return null
            val proxyEndpoint = client.localSocketAddress as? InetSocketAddress ?: return null
            val uid = connectivityManager.getConnectionOwnerUid(OsConstants.IPPROTO_TCP, appEndpoint, proxyEndpoint)
            if (uid <= 0) return null
            packageManager.getPackagesForUid(uid)?.firstOrNull()
        } catch (_: Exception) {
            null
        }
    }

    private fun relay(client: Socket, clientInput: InputStream, remote: Socket) {
        val t1 = Thread { runCatching { clientInput.copyTo(remote.getOutputStream()) } }
        val t2 = Thread { runCatching { remote.getInputStream().copyTo(client.getOutputStream()) } }
        t1.start(); t2.start()
        t1.join(); t2.join()
        client.close(); remote.close()
    }

    private fun logSession(host: String, packageName: String?) {
        if (packageName == null) return
        scope.launch {
            networkMonitorRepository.recordSessionIfNew(
                NetworkSession(remoteAddress = host, timestamp = System.currentTimeMillis()),
                packageName = packageName
            )
        }
    }
}