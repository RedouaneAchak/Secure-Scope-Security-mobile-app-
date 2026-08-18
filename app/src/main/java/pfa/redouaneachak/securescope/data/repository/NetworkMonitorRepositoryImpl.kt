package pfa.redouaneachak.securescope.data.repository

import android.app.usage.NetworkStats
import android.app.usage.NetworkStatsManager
import android.content.Context
import android.net.ConnectivityManager
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import pfa.redouaneachak.securescope.data.local.dao.NetworkSessionDao
import pfa.redouaneachak.securescope.data.local.entity.NetworkSessionEntity
import pfa.redouaneachak.securescope.data.model.AppDataUsage
import pfa.redouaneachak.securescope.data.model.NetworkSession
import javax.inject.Inject

class NetworkMonitorRepositoryImpl @Inject constructor(
    private val networkSessionDao: NetworkSessionDao,
    @ApplicationContext private val context: Context
) : NetworkMonitorRepository {

    override fun getSessionsForApp(packageName: String): Flow<List<NetworkSession>> {
        return networkSessionDao.getSessionsForApp(packageName).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override suspend fun getContactedServers(packageName: String): List<String> {
        return networkSessionDao.getContactedServers(packageName)
    }

    override suspend fun recordSession(session: NetworkSession, packageName: String) {
        networkSessionDao.insertSession(
            NetworkSessionEntity(
                packageName = packageName,
                remoteAddress = session.remoteAddress,
                timestamp = session.timestamp
            )
        )
    }

    @Suppress("DEPRECATION")
    override suspend fun getDataUsageForAllApps(): List<AppDataUsage> = withContext(Dispatchers.IO) {
        val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val packageManager = context.packageManager
        val usageByUid = mutableMapOf<Int, Pair<Long, Long>>()

        for (networkType in listOf(ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE)) {
            try {
                val stats = networkStatsManager.querySummary(networkType, null, 0L, System.currentTimeMillis())
                val bucket = NetworkStats.Bucket()
                while (stats.hasNextBucket()) {
                    stats.getNextBucket(bucket)
                    val previous = usageByUid[bucket.uid] ?: (0L to 0L)
                    usageByUid[bucket.uid] = (previous.first + bucket.rxBytes) to (previous.second + bucket.txBytes)
                }
                stats.close()
            } catch (_: Exception) {
                // this network type unavailable on this device, or usage access not yet granted
            }
        }

        return@withContext usageByUid.mapNotNull { (uid, usage) ->
            val packageName = packageManager.getPackagesForUid(uid)?.firstOrNull() ?: return@mapNotNull null
            AppDataUsage(packageName, totalSentBytes = usage.second, totalReceivedBytes = usage.first)
        }
    }

    override fun isPrivateDnsActive(): Boolean {
        val connectivityManager = context.getSystemService(ConnectivityManager::class.java)
        val activeNetwork = connectivityManager.activeNetwork ?: return false
        val linkProperties = connectivityManager.getLinkProperties(activeNetwork) ?: return false
        return linkProperties.isPrivateDnsActive
    }

    private fun NetworkSessionEntity.toDomainModel(): NetworkSession {
        return NetworkSession(
            remoteAddress = remoteAddress,
            timestamp = timestamp
        )
    }
}