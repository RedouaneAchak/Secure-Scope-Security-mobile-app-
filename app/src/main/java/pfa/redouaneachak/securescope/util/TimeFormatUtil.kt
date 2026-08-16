package pfa.redouaneachak.securescope.util

import java.text.SimpleDateFormat
import java.util.Locale
import java.util.concurrent.TimeUnit

object TimeFormatUtil {

    fun formatRelativeTime(timestamp: Long): String {
        val now = System.currentTimeMillis()
        val diffMs = now - timestamp

        return when {
            diffMs < TimeUnit.MINUTES.toMillis(1) -> "Just now"
            diffMs < TimeUnit.HOURS.toMillis(1) -> {
                val minutes = TimeUnit.MILLISECONDS.toMinutes(diffMs)
                "$minutes min ago"
            }
            diffMs < TimeUnit.DAYS.toMillis(1) -> {
                val hours = TimeUnit.MILLISECONDS.toHours(diffMs)
                "$hours h ago"
            }
            diffMs < TimeUnit.DAYS.toMillis(2) -> "Yesterday"
            else -> {
                val formatter = SimpleDateFormat("MMM d, HH:mm", Locale.getDefault())
                formatter.format(timestamp)
            }
        }
    }
}