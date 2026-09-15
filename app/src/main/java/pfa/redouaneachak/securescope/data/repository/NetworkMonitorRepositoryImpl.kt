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
import pfa.redouaneachak.securescope.data.local.BlockStatsHolder
import pfa.redouaneachak.securescope.data.local.VpnStateHolder
import pfa.redouaneachak.securescope.data.local.dao.NetworkSessionDao
import pfa.redouaneachak.securescope.data.local.entity.NetworkSessionEntity
import pfa.redouaneachak.securescope.data.model.AppDataUsage
import pfa.redouaneachak.securescope.data.model.NetworkSession
import pfa.redouaneachak.securescope.data.model.NetworkSessionWithApp
import pfa.redouaneachak.securescope.data.model.BlockedDomain
import javax.inject.Inject

class NetworkMonitorRepositoryImpl @Inject constructor(
    private val networkSessionDao: NetworkSessionDao,
    private val appRepository: AppRepository,
    private val vpnStateHolder: VpnStateHolder,
    private val blockStatsHolder: BlockStatsHolder,
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

    override suspend fun recordSessionIfNew(session: NetworkSession, packageName: String) {
        val exists = networkSessionDao.sessionExists(packageName, session.remoteAddress)
        if (!exists) {
            recordSession(session, packageName)
        }
    }

    @Suppress("DEPRECATION")
    override suspend fun getDataUsageForAllApps(sinceMillis: Long?): List<AppDataUsage> = withContext(Dispatchers.IO) {
        val networkStatsManager = context.getSystemService(Context.NETWORK_STATS_SERVICE) as NetworkStatsManager
        val packageManager = context.packageManager
        val usageByUid = mutableMapOf<Int, Pair<Long, Long>>()

        val endTime = System.currentTimeMillis()
        val startTime = if (sinceMillis != null) endTime - sinceMillis else 0L

        for (networkType in listOf(ConnectivityManager.TYPE_WIFI, ConnectivityManager.TYPE_MOBILE)) {
            try {
                val stats = networkStatsManager.querySummary(networkType, null, startTime, endTime)
                val bucket = NetworkStats.Bucket()
                while (stats.hasNextBucket()) {
                    stats.getNextBucket(bucket)
                    val previous = usageByUid[bucket.uid] ?: (0L to 0L)
                    usageByUid[bucket.uid] = (previous.first + bucket.rxBytes) to (previous.second + bucket.txBytes)
                }
                stats.close()
            } catch (_: Exception) { }
        }

        usageByUid.mapNotNull { (uid, usage) ->
            val packageName = packageManager.getPackagesForUid(uid)?.firstOrNull() ?: return@mapNotNull null
            AppDataUsage(packageName, totalSentBytes = usage.second, totalReceivedBytes = usage.first)
        }
    }

    override fun isVpnActive(): Flow<Boolean> = vpnStateHolder.isActive

    override fun observeBlockedDomains(): Flow<List<BlockedDomain>> = blockStatsHolder.blockedDomains

    override fun observeRecentSessions(limit: Int): Flow<List<NetworkSessionWithApp>> {
        return networkSessionDao.getRecentSessions(limit).map { entities ->
            entities.map { entity ->
                val appName = appRepository.getAppByPackageName(entity.packageName)?.appName ?: entity.packageName
                NetworkSessionWithApp(
                    packageName = entity.packageName,
                    appName = appName,
                    remoteAddress = entity.remoteAddress,
                    timestamp = entity.timestamp,
                    isSuspicious = false
                )
            }
        }
    }

    private fun NetworkSessionEntity.toDomainModel(): NetworkSession {
        return NetworkSession(remoteAddress = remoteAddress, timestamp = timestamp)
    }
}