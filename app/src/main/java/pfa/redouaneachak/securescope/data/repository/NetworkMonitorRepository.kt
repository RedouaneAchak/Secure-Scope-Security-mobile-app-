package pfa.redouaneachak.securescope.data.repository

import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.model.AppDataUsage
import pfa.redouaneachak.securescope.data.model.BlockedDomain
import pfa.redouaneachak.securescope.data.model.NetworkSession
import pfa.redouaneachak.securescope.data.model.NetworkSessionWithApp

interface NetworkMonitorRepository {
    fun getSessionsForApp(packageName: String): Flow<List<NetworkSession>>
    suspend fun getContactedServers(packageName: String): List<String>
    suspend fun recordSession(session: NetworkSession, packageName: String)
    suspend fun recordSessionIfNew(session: NetworkSession, packageName: String)
    suspend fun getDataUsageForAllApps(sinceMillis: Long?): List<AppDataUsage>
    fun isVpnActive(): Flow<Boolean>
    fun observeBlockedDomains(): Flow<List<BlockedDomain>>
    fun observeRecentSessions(limit: Int = 50): Flow<List<NetworkSessionWithApp>>
}