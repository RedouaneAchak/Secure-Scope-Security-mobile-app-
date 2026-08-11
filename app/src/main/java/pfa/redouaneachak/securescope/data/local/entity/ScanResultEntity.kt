package pfa.redouaneachak.securescope.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import pfa.redouaneachak.securescope.data.model.RiskScore

@Entity(tableName = "scan_results")
data class ScanResultEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(index = true)
    val packageName: String,

    val trackerCount: Int,
    val malwareDetected: Boolean,
    val malwareNames: List<String>,
    val vulnerabilitiesFound: Int,
    val riskScore: RiskScore,
    val scanTimestamp: Long,
    val cloudVerified: Boolean
)