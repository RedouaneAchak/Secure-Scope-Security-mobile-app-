package pfa.redouaneachak.securescope.data.repository

import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.model.NetworkSession

interface NetworkMonitorRepository {
    fun getSessionsForApp(packageName: String): Flow<List<NetworkSession>>
    fun getDataUsageSummary(): Flow<Map<String, Pair<Long, Long>>>
    suspend fun getContactedServers(packageName: String): List<String>
    suspend fun recordSession(session: NetworkSession, packageName: String)
}