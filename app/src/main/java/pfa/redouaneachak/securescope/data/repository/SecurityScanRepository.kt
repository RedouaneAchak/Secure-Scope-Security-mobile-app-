package pfa.redouaneachak.securescope.data.repository

import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.model.ScanProgress
import pfa.redouaneachak.securescope.data.model.ScanResult


interface SecurityScanRepository {
    fun getLatestScanResults(): Flow<List<ScanResult>>
    fun scanAllApps(): Flow<ScanProgress>
    suspend fun scanSingleApp(packageName: String): ScanResult
}