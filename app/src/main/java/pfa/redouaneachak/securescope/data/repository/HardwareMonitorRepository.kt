package pfa.redouaneachak.securescope.data.repository

import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.model.HardwareStats

interface HardwareMonitorRepository {
    fun observeHardwareStats(): Flow<HardwareStats>
}