package pfa.redouaneachak.securescope.util

import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import pfa.redouaneachak.securescope.data.model.TrackerInfo
import javax.inject.Inject

private data class TrackerSignature(
    val name: String,
    val category: String,
    val packagePrefix: String
)

private object TrackerSignatureDatabase {
    val signatures = listOf(
        TrackerSignature("Google Firebase Analytics", "Analytics", "com.google.firebase.analytics"),
        TrackerSignature("Google Firebase Crashlytics", "Crash Reporting", "com.google.firebase.crashlytics"),
        TrackerSignature("Google AdMob", "Advertising", "com.google.android.gms.ads"),
        TrackerSignature("Facebook Ads", "Advertising", "com.facebook.ads"),
        TrackerSignature("Facebook Analytics", "Analytics", "com.facebook.appevents"),
        TrackerSignature("AppsFlyer", "Analytics", "com.appsflyer"),
        TrackerSignature("Adjust", "Analytics", "com.adjust.sdk"),
        TrackerSignature("Amplitude", "Analytics", "com.amplitude"),
        TrackerSignature("Mixpanel", "Analytics", "com.mixpanel"),
        TrackerSignature("Unity Ads", "Advertising", "com.unity3d.ads"),
        TrackerSignature("Flurry", "Analytics", "com.flurry"),
        TrackerSignature("OneSignal", "Push / Analytics", "com.onesignal"),
        TrackerSignature("Segment", "Analytics", "com.segment.analytics"),
        TrackerSignature("Braze", "Marketing", "com.braze")
    )
}

class TrackerDetector @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val packageManager = context.packageManager

    suspend fun detectTrackers(packageName: String): List<TrackerInfo> = withContext(Dispatchers.IO) {
        val componentClassNames = getDeclaredComponentClassNames(packageName)
        val now = System.currentTimeMillis()

        return@withContext TrackerSignatureDatabase.signatures
            .filter { signature ->
                componentClassNames.any { it.startsWith(signature.packagePrefix) }
            }
            .map { signature ->
                TrackerInfo(
                    name = signature.name,
                    category = signature.category,
                    detectedTimestamp = now
                )
            }
    }

    private fun getDeclaredComponentClassNames(packageName: String): List<String> {
        val flags = PackageManager.GET_SERVICES or
                PackageManager.GET_RECEIVERS or
                PackageManager.GET_PROVIDERS or
                PackageManager.GET_ACTIVITIES

        val packageInfo = try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                packageManager.getPackageInfo(packageName, PackageManager.PackageInfoFlags.of(flags.toLong()))
            } else {
                @Suppress("DEPRECATION")
                packageManager.getPackageInfo(packageName, flags)
            }
        } catch (_: PackageManager.NameNotFoundException) {
            return emptyList()
        }

        val names = mutableListOf<String>()
        packageInfo.services?.forEach { names.add(it.name) }
        packageInfo.receivers?.forEach { names.add(it.name) }
        packageInfo.providers?.forEach { names.add(it.name) }
        packageInfo.activities?.forEach { names.add(it.name) }
        return names
    }
}