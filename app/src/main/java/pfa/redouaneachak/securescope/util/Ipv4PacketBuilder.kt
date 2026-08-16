package pfa.redouaneachak.securescope.util

import java.net.InetAddress
import java.nio.ByteBuffer

object Ipv4PacketBuilder {

    fun buildUdpResponsePacket(
        sourceAddress: InetAddress,
        sourcePort: Int,
        destAddress: InetAddress,
        destPort: Int,
        payload: ByteArray
    ): ByteArray {
        val udpLength = 8 + payload.size
        val totalLength = 20 + udpLength
        val packet = ByteBuffer.allocate(totalLength)

        packet.put(0x45.toByte())
        packet.put(0x00.toByte())
        packet.putShort(totalLength.toShort())
        packet.putShort(0)
        packet.putShort(0x4000.toShort())
        packet.put(64.toByte())
        packet.put(17.toByte())
        packet.putShort(0)
        packet.put(sourceAddress.address)
        packet.put(destAddress.address)

        packet.putShort(sourcePort.toShort())
        packet.putShort(destPort.toShort())
        packet.putShort(udpLength.toShort())
        packet.putShort(0)
        packet.put(payload)

        val bytes = packet.array()
        val checksum = computeIpv4Checksum(bytes, 0, 20)
        bytes[10] = (checksum shr 8).toByte()
        bytes[11] = (checksum and 0xFF).toByte()

        return bytes
    }

    private fun computeIpv4Checksum(data: ByteArray, offset: Int, length: Int): Int {
        var sum = 0
        var i = offset
        while (i < offset + length) {
            val word = ((data[i].toInt() and 0xFF) shl 8) or (data[i + 1].toInt() and 0xFF)
            sum += word
            i += 2
        }
        while (sum shr 16 != 0) {
            sum = (sum and 0xFFFF) + (sum shr 16)
        }
        return sum.inv() and 0xFFFF
    }
}