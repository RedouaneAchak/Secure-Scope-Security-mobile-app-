package pfa.redouaneachak.securescope.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "trackers",
    foreignKeys = [
        ForeignKey(
            entity = ScanResultEntity::class,
            parentColumns = ["id"],
            childColumns = ["scanResultId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class TrackerEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(index = true)
    val scanResultId: Long,

    @ColumnInfo(index = true)
    val packageName: String,

    val trackerName: String,
    val trackerCategory: String,
    val detectedTimestamp: Long
)