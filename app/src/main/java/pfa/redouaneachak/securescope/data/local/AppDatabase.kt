package pfa.redouaneachak.securescope.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import pfa.redouaneachak.securescope.data.local.dao.NetworkSessionDao
import pfa.redouaneachak.securescope.data.local.dao.ScanResultDao
import pfa.redouaneachak.securescope.data.local.dao.TrackerDao
import pfa.redouaneachak.securescope.data.local.entity.NetworkSessionEntity
import pfa.redouaneachak.securescope.data.local.entity.ScanResultEntity
import pfa.redouaneachak.securescope.data.local.entity.TrackerEntity

@Database(
    entities = [
        ScanResultEntity::class,
        NetworkSessionEntity::class,
        TrackerEntity::class
    ],
    version = 1,
    exportSchema = true
)
@TypeConverters(Converters::class)
abstract class AppDatabase : RoomDatabase() {
    abstract fun scanResultDao(): ScanResultDao
    abstract fun networkSessionDao(): NetworkSessionDao
    abstract fun trackerDao(): TrackerDao
}