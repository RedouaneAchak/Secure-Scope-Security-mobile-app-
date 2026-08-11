package pfa.redouaneachak.securescope.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.local.entity.ScanResultEntity

@Dao
interface ScanResultDao {

    @Query("""
        SELECT * FROM scan_results 
        WHERE id IN (
            SELECT MAX(id) FROM scan_results GROUP BY packageName
        )
        ORDER BY riskScore DESC
    """)
    fun getLatestScanResults(): Flow<List<ScanResultEntity>>

    @Query("SELECT * FROM scan_results WHERE packageName = :packageName ORDER BY scanTimestamp DESC")
    fun getScanHistoryForApp(packageName: String): Flow<List<ScanResultEntity>>

    @Insert
    suspend fun insertScanResult(result: ScanResultEntity): Long

    @Insert
    suspend fun insertScanResults(results: List<ScanResultEntity>)

    @Query("DELETE FROM scan_results WHERE packageName = :packageName")
    suspend fun deleteHistoryForApp(packageName: String)
}