package pfa.redouaneachak.securescope.data.remote

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import org.json.JSONObject
import pfa.redouaneachak.securescope.BuildConfig
import pfa.redouaneachak.securescope.data.remote.dto.VirusTotalScanResponse
import pfa.redouaneachak.securescope.util.ApkHashUtil
import javax.inject.Inject

class VirusTotalApiService @Inject constructor(
    private val okHttpClient: OkHttpClient,
    @ApplicationContext private val context: Context
) {

    suspend fun analyzeApp(packageName: String): VirusTotalScanResponse = withContext(Dispatchers.IO) {
        val hash = ApkHashUtil.computeSha256(context.packageManager, packageName)
            ?: return@withContext emptyResponse()

        val request = Request.Builder()
            .url("$BASE_URL/files/$hash")
            .addHeader("x-apikey", BuildConfig.VIRUSTOTAL_API_KEY)
            .get()
            .build()

        okHttpClient.newCall(request).execute().use { response ->
            when {
                response.code == 404 -> emptyResponse(fileFound = false)
                !response.isSuccessful -> emptyResponse()
                else -> parseResponse(response.body.string())
            }
        }
    }

    private fun parseResponse(body: String?): VirusTotalScanResponse {
        if (body.isNullOrBlank()) return emptyResponse()

        return try {
            val json = JSONObject(body)
            val attributes = json.getJSONObject("data").getJSONObject("attributes")
            val stats = attributes.getJSONObject("last_analysis_stats")
            val results = attributes.getJSONObject("last_analysis_results")

            val maliciousCount = stats.optInt("malicious", 0)
            val suspiciousCount = stats.optInt("suspicious", 0)

            val malwareNames = mutableListOf<String>()
            val engineNames = results.keys()
            while (engineNames.hasNext()) {
                val engine = engineNames.next()
                val engineResult = results.getJSONObject(engine)
                if (engineResult.optString("category") == "malicious") {
                    val threatName = engineResult.optString("result", "")
                    if (threatName.isNotBlank()) {
                        malwareNames.add(threatName)
                    }
                }
            }

            VirusTotalScanResponse(
                malwareDetected = maliciousCount > 0,
                malwareNames = malwareNames.distinct(),
                vulnerabilityCount = suspiciousCount,
                fileFound = true
            )
        } catch (_: Exception) {
            emptyResponse()
        }
    }

    private fun emptyResponse(fileFound: Boolean = true): VirusTotalScanResponse {
        return VirusTotalScanResponse(
            malwareDetected = false,
            malwareNames = emptyList(),
            vulnerabilityCount = 0,
            fileFound = fileFound
        )
    }

    companion object {
        private const val BASE_URL = "https://www.virustotal.com/api/v3"
    }
}