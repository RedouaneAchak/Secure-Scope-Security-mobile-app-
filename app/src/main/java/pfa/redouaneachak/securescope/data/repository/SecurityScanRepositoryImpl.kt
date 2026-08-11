package pfa.redouaneachak.securescope.data.repository

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import pfa.redouaneachak.securescope.data.local.dao.ScanResultDao
import pfa.redouaneachak.securescope.data.local.dao.TrackerDao
import pfa.redouaneachak.securescope.data.local.entity.ScanResultEntity
import pfa.redouaneachak.securescope.data.local.entity.TrackerEntity
import pfa.redouaneachak.securescope.data.model.AppInfo
import pfa.redouaneachak.securescope.data.model.RiskScore
import pfa.redouaneachak.securescope.data.model.ScanResult
import pfa.redouaneachak.securescope.data.model.TrackerInfo
import pfa.redouaneachak.securescope.data.remote.VirusTotalApiService
import pfa.redouaneachak.securescope.util.TrackerDetector
import javax.inject.Inject

class SecurityScanRepositoryImpl @Inject constructor(
    private val appRepository: AppRepository,
    private val scanResultDao: ScanResultDao,
    private val trackerDao: TrackerDao,
    private val trackerDetector: TrackerDetector,
    private val virusTotalApi: VirusTotalApiService
) : SecurityScanRepository {

    private val cloudCallMutex = Mutex()
    private var lastCloudCallTime = 0L

    override fun getLatestScanResults(): Flow<List<ScanResult>> {
        return scanResultDao.getLatestScanResults().map { scanEntities ->
            scanEntities.mapNotNull { scanEntity ->
                val app = appRepository.getAppByPackageName(scanEntity.packageName) ?: return@mapNotNull null
                val trackers = trackerDao.getTrackersForScan(scanEntity.id)
                scanEntity.toDomainModel(app, trackers)
            }
        }
    }

    override suspend fun scanAllApps(): List<ScanResult> {
        val apps = appRepository.getInstalledApps()
        return apps.map { scanSingleApp(it.packageName) }
    }

    override suspend fun scanSingleApp(packageName: String): ScanResult {
        val app = appRepository.getAppByPackageName(packageName) ?: error("App $packageName not found")

        val detectedTrackers = trackerDetector.detectTrackers(packageName)
        val dangerousPermissionCount = appRepository.getPermissionsForApp(packageName)
            .count { it.isDangerous && it.isGranted }
        val isSideloaded = appRepository.isSideloaded(packageName)

        val localScore = calculateLocalRiskScore(
            trackerCount = detectedTrackers.size,
            dangerousPermissionCount = dangerousPermissionCount,
            isSideloaded = isSideloaded
        )

        val shouldEscalate = localScore >= CLOUD_ESCALATION_THRESHOLD

        val cloudResult = if (shouldEscalate) {
            callVirusTotalRateLimited(packageName)
        } else {
            null
        }

        val riskScore = if (cloudResult != null) {
            calculateFinalRiskScore(
                trackerCount = detectedTrackers.size,
                malwareDetected = cloudResult.malwareDetected,
                vulnerabilitiesFound = cloudResult.vulnerabilityCount
            )
        } else {
            // below escalation threshold — local signal alone caps at MEDIUM,
            // since nothing here has been cloud-confirmed as malicious
            if (localScore >= LOCAL_MEDIUM_THRESHOLD) RiskScore.MEDIUM else RiskScore.LOW
        }

        val scanEntity = ScanResultEntity(
            packageName = packageName,
            trackerCount = detectedTrackers.size,
            malwareDetected = cloudResult?.malwareDetected ?: false,
            malwareNames = cloudResult?.malwareNames ?: emptyList(),
            vulnerabilitiesFound = cloudResult?.vulnerabilityCount ?: 0,
            riskScore = riskScore,
            cloudVerified = shouldEscalate,
            scanTimestamp = System.currentTimeMillis()
        )
        val scanResultId = scanResultDao.insertScanResult(scanEntity)

        val trackerEntities = detectedTrackers.map { tracker ->
            TrackerEntity(
                scanResultId = scanResultId,
                packageName = packageName,
                trackerName = tracker.name,
                trackerCategory = tracker.category,
                detectedTimestamp = System.currentTimeMillis()
            )
        }
        trackerDao.insertTrackers(trackerEntities)

        return ScanResult(
            app = app,
            trackerCount = detectedTrackers.size,
            detectedTrackers = detectedTrackers,
            malwareDetected = cloudResult?.malwareDetected ?: false,
            malwareNames = cloudResult?.malwareNames ?: emptyList(),
            vulnerabilitiesFound = cloudResult?.vulnerabilityCount ?: 0,
            riskScore = riskScore,
            cloudVerified = shouldEscalate,
            scanTimestamp = scanEntity.scanTimestamp
        )
    }

    private fun calculateLocalRiskScore(
        trackerCount: Int,
        dangerousPermissionCount: Int,
        isSideloaded: Boolean
    ): Int {
        return (trackerCount * 1) +
                (dangerousPermissionCount * 2) +
                (if (isSideloaded) 5 else 0)
    }

    private fun calculateFinalRiskScore(
        trackerCount: Int,
        malwareDetected: Boolean,
        vulnerabilitiesFound: Int
    ): RiskScore {
        if (malwareDetected) return RiskScore.CRITICAL
        val score = (trackerCount * 1) + (vulnerabilitiesFound * 4)
        return when {
            score >= 15 -> RiskScore.HIGH
            score >= 5 -> RiskScore.MEDIUM
            else -> RiskScore.LOW
        }
    }

    private suspend fun callVirusTotalRateLimited(packageName: String): CloudScanOutcome {
        cloudCallMutex.withLock {
            val now = System.currentTimeMillis()
            val elapsed = now - lastCloudCallTime
            if (elapsed < MIN_CLOUD_CALL_INTERVAL_MS) {
                delay(MIN_CLOUD_CALL_INTERVAL_MS - elapsed)
            }
            lastCloudCallTime = System.currentTimeMillis()
        }
        val response = virusTotalApi.analyzeApp(packageName)
        return CloudScanOutcome(
            malwareDetected = response.malwareDetected,
            malwareNames = response.malwareNames,
            vulnerabilityCount = response.vulnerabilityCount
        )
    }

    private fun ScanResultEntity.toDomainModel(app: AppInfo, trackers: List<TrackerEntity>): ScanResult {
        return ScanResult(
            app = app,
            trackerCount = trackerCount,
            detectedTrackers = trackers.map { TrackerInfo(it.trackerName, it.trackerCategory, it.detectedTimestamp) },
            malwareDetected = malwareDetected,
            malwareNames = malwareNames,
            vulnerabilitiesFound = vulnerabilitiesFound,
            riskScore = riskScore,
            cloudVerified = cloudVerified,
            scanTimestamp = scanTimestamp
        )
    }

    companion object {
        private const val CLOUD_ESCALATION_THRESHOLD = 5
        private const val LOCAL_MEDIUM_THRESHOLD = 3
        private const val MIN_CLOUD_CALL_INTERVAL_MS = 15_000L // 4 requests/minute
    }
}

private data class CloudScanOutcome(
    val malwareDetected: Boolean,
    val malwareNames: List<String>,
    val vulnerabilityCount: Int
)