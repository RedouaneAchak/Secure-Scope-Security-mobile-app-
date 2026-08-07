package pfa.redouaneachak.securescope.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.local.entity.TrackerEntity

@Dao
interface TrackerDao {

    @Query("SELECT * FROM trackers WHERE scanResultId = :scanResultId")
    suspend fun getTrackersForScan(scanResultId: Long): List<TrackerEntity>

    @Query("SELECT * FROM trackers WHERE packageName = :packageName")
    fun getAllTrackersForApp(packageName: String): Flow<List<TrackerEntity>>

    @Query("SELECT COUNT(*) FROM trackers WHERE scanResultId = :scanResultId")
    suspend fun getTrackerCountForScan(scanResultId: Long): Int

    @Insert
    suspend fun insertTracker(tracker: TrackerEntity)

    @Insert
    suspend fun insertTrackers(trackers: List<TrackerEntity>)
}