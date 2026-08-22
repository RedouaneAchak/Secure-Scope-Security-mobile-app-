package pfa.redouaneachak.securescope.data.repository

import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.model.HardwareStats
import pfa.redouaneachak.securescope.data.model.StorageBreakdown

interface HardwareMonitorRepository {
    fun observeHardwareStats(): Flow<HardwareStats>
    suspend fun getStorageBreakdown(): StorageBreakdown
}