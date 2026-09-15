package pfa.redouaneachak.securescope.util

import android.content.Context
import dagger.hilt.android.qualifiers.ApplicationContext
import java.io.BufferedReader
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class BlocklistProvider @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val blockedDomains: Map<String, String> by lazy { loadBlocklist() }

    fun isBlocked(domain: String): Boolean {
        val lower = domain.lowercase()
        return blockedDomains.keys.any { blocked -> lower == blocked || lower.endsWith(".$blocked") }
    }

    fun getBlockedCategory(domain: String): String? {
        val lower = domain.lowercase()
        return blockedDomains.entries.firstOrNull { (blocked, _) -> lower == blocked || lower.endsWith(".$blocked") }?.value
    }

    private fun loadBlocklist(): Map<String, String> {
        val map = mutableMapOf<String, String>()
        try {
            context.assets.open("blocklist.txt").bufferedReader().use { reader: BufferedReader ->
                reader.forEachLine { line ->
                    val trimmed = line.trim()
                    if (trimmed.isEmpty() || trimmed.startsWith("#")) return@forEachLine
                    val parts = trimmed.split(",")
                    if (parts.size == 2) map[parts[1].trim().lowercase()] = parts[0].trim()
                }
            }
        } catch (e: Exception) {
            android.util.Log.d("SecureScope", "blocklist load failed: ${e.message}")
        }
        return map
    }
}