package pfa.redouaneachak.securescope.data.local

import androidx.room.TypeConverter
import pfa.redouaneachak.securescope.data.model.RiskScore

class Converters {

    @TypeConverter
    fun fromRiskScore(riskScore: RiskScore): String {
        return riskScore.name
    }

    @TypeConverter
    fun toRiskScore(value: String): RiskScore {
        return RiskScore.valueOf(value)
    }

    @TypeConverter
    fun fromStringList(list: List<String>): String {
        return list.joinToString(separator = "|")
    }

    @TypeConverter
    fun toStringList(value: String): List<String> {
        return if (value.isEmpty()) emptyList() else value.split("|")
    }
}