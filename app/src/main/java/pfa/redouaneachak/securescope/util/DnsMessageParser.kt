package pfa.redouaneachak.securescope.util

object DnsMessageParser {

    fun extractQueriedDomains(dnsPayload: ByteArray): List<String> {
        if (dnsPayload.size < 12) return emptyList()

        return try {
            val questionCount = ((dnsPayload[4].toInt() and 0xFF) shl 8) or (dnsPayload[5].toInt() and 0xFF)
            val domains = mutableListOf<String>()
            var offset = 12

            repeat(questionCount) {
                val domain = StringBuilder()
                while (offset < dnsPayload.size) {
                    val labelLength = dnsPayload[offset].toInt() and 0xFF
                    if (labelLength == 0) {
                        offset++
                        break
                    }
                    offset++
                    if (offset + labelLength > dnsPayload.size) return domains
                    if (domain.isNotEmpty()) domain.append('.')
                    domain.append(String(dnsPayload, offset, labelLength, Charsets.US_ASCII))
                    offset += labelLength
                }
                if (domain.isNotBlank()) domains.add(domain.toString())
                offset += 4 // skip QTYPE(2) + QCLASS(2) before the next question
            }

            domains
        } catch (_: Exception) {
            emptyList()
        }
    }
}