package pfa.redouaneachak.securescope.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.local.entity.NetworkSessionEntity

@Dao
interface NetworkSessionDao {

    @Query("SELECT * FROM network_sessions WHERE packageName = :packageName ORDER BY sessionStartTimestamp DESC")
    fun getSessionsForApp(packageName: String): Flow<List<NetworkSessionEntity>>

    @Query("""
        SELECT packageName, SUM(bytesSent) AS totalSent, SUM(bytesReceived) AS totalReceived 
        FROM network_sessions 
        GROUP BY packageName
    """)
    fun getDataUsageSummary(): Flow<List<AppDataUsage>>

    @Query("SELECT DISTINCT remoteAddress FROM network_sessions WHERE packageName = :packageName")
    suspend fun getContactedServers(packageName: String): List<String>

    @Insert
    suspend fun insertSession(session: NetworkSessionEntity): Long

    @Update
    suspend fun updateSession(session: NetworkSessionEntity)

    @Query("DELETE FROM network_sessions WHERE sessionStartTimestamp < :beforeTimestamp")
    suspend fun deleteSessionsOlderThan(beforeTimestamp: Long)
}

data class AppDataUsage(
    val packageName: String,
    val totalSent: Long,
    val totalReceived: Long
)