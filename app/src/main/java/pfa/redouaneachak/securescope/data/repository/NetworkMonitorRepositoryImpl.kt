package pfa.redouaneachak.securescope.data.repository

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import pfa.redouaneachak.securescope.data.local.dao.NetworkSessionDao
import pfa.redouaneachak.securescope.data.local.entity.NetworkSessionEntity
import pfa.redouaneachak.securescope.data.model.NetworkSession
import javax.inject.Inject

class NetworkMonitorRepositoryImpl @Inject constructor(
    private val networkSessionDao: NetworkSessionDao
) : NetworkMonitorRepository {

    override fun getSessionsForApp(packageName: String): Flow<List<NetworkSession>> {
        return networkSessionDao.getSessionsForApp(packageName).map { entities ->
            entities.map { it.toDomainModel() }
        }
    }

    override fun getDataUsageSummary(): Flow<Map<String, Pair<Long, Long>>> {
        return networkSessionDao.getDataUsageSummary().map { usageList ->
            usageList.associate { it.packageName to (it.totalSent to it.totalReceived) }
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
                remotePort = session.remotePort,
                bytesSent = session.bytesSent,
                bytesReceived = session.bytesReceived,
                sessionStartTimestamp = session.sessionStartTimestamp,
                sessionEndTimestamp = session.sessionEndTimestamp
            )
        )
    }

    private fun NetworkSessionEntity.toDomainModel(): NetworkSession {
        return NetworkSession(
            remoteAddress = remoteAddress,
            remotePort = remotePort,
            bytesSent = bytesSent,
            bytesReceived = bytesReceived,
            sessionStartTimestamp = sessionStartTimestamp,
            sessionEndTimestamp = sessionEndTimestamp
        )
    }
}