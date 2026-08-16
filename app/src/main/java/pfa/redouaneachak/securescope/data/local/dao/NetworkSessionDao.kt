package pfa.redouaneachak.securescope.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.local.entity.NetworkSessionEntity

@Dao
interface NetworkSessionDao {

    @Query("SELECT * FROM network_sessions WHERE packageName = :packageName ORDER BY timestamp DESC")
    fun getSessionsForApp(packageName: String): Flow<List<NetworkSessionEntity>>

    @Query("SELECT DISTINCT remoteAddress FROM network_sessions WHERE packageName = :packageName")
    suspend fun getContactedServers(packageName: String): List<String>

    @Insert
    suspend fun insertSession(session: NetworkSessionEntity)

    @Query("DELETE FROM network_sessions WHERE timestamp < :beforeTimestamp")
    suspend fun deleteSessionsOlderThan(beforeTimestamp: Long)
}