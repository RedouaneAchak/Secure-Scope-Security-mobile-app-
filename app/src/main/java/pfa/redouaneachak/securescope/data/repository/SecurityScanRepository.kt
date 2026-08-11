package pfa.redouaneachak.securescope.data.repository

import kotlinx.coroutines.flow.Flow
import pfa.redouaneachak.securescope.data.model.ScanResult

interface SecurityScanRepository {
    fun getLatestScanResults(): Flow<List<ScanResult>>
    suspend fun scanAllApps(): List<ScanResult>
    suspend fun scanSingleApp(packageName: String): ScanResult
}