package pfa.redouaneachak.securescope.data.local.dao
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.local.entity.AppInfoEntity

@Dao
interface AppInfoDao {

    @Query("SELECT * FROM app_info ORDER BY appName ASC")
    fun getAllApps(): Flow<List<AppInfoEntity>>

    @Query("SELECT * FROM app_info WHERE packageName = :packageName")
    suspend fun getAppByPackageName(packageName: String): AppInfoEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApp(app: AppInfoEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertApps(apps: List<AppInfoEntity>)

    @Delete
    suspend fun deleteApp(app: AppInfoEntity)

    @Query("DELETE FROM app_info WHERE packageName = :packageName")
    suspend fun deleteByPackageName(packageName: String)
}