package pfa.redouaneachak.securescope.util

import android.content.pm.PackageManager
import java.io.File
import java.security.MessageDigest

object ApkHashUtil {

    fun computeSha256(packageManager: PackageManager, packageName: String): String? {
        return try {
            val appInfo = packageManager.getApplicationInfo(packageName, 0)
            val apkFile = File(appInfo.sourceDir)
            val digest = MessageDigest.getInstance("SHA-256")

            apkFile.inputStream().use { input ->
                val buffer = ByteArray(8192)
                var bytesRead: Int
                while (input.read(buffer).also { bytesRead = it } != -1) {
                    digest.update(buffer, 0, bytesRead)
                }
            }

            digest.digest().joinToString("") { "%02x".format(it) }
        } catch (e: Exception) {
            null
        }
    }
}