package pfa.redouaneachak.securescope.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "network_sessions")
data class NetworkSessionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(index = true)
    val packageName: String,

    val remoteAddress: String,
    val timestamp: Long
)