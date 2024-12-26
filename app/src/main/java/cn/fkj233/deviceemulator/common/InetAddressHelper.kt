package cn.fkj233.deviceemulator.common

import java.net.InetAddress

object InetAddressHelper {
    private fun ipv4AddressVerify(ip: String): Boolean {
        val p = "^((?:(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d)\\.){3}(?:25[0-5]|2[0-4]\\d|[01]?\\d?\\d))\$".toRegex()
        return p.matches(ip)
    }

    private fun ipv6AddressVerify(ip: String): Boolean {
        val p = "^([\\da-fA-F]{1,4}:){6}((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\$|^::([\\da-fA-F]{1,4}:){0,4}((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\$|^([\\da-fA-F]{1,4}:):([\\da-fA-F]{1,4}:){0,3}((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\$|^([\\da-fA-F]{1,4}:){2}:([\\da-fA-F]{1,4}:){0,2}((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\$|^([\\da-fA-F]{1,4}:){3}:([\\da-fA-F]{1,4}:){0,1}((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\$|^([\\da-fA-F]{1,4}:){4}:((25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\\.){3}(25[0-5]|2[0-4]\\d|[01]?\\d\\d?)\$|^([\\da-fA-F]{1,4}:){7}[\\da-fA-F]{1,4}\$|^:((:[\\da-fA-F]{1,4}){1,6}|:)\$|^[\\da-fA-F]{1,4}:((:[\\da-fA-F]{1,4}){1,5}|:)\$|^([\\da-fA-F]{1,4}:){2}((:[\\da-fA-F]{1,4}){1,4}|:)\$|^([\\da-fA-F]{1,4}:){3}((:[\\da-fA-F]{1,4}){1,3}|:)\$|^([\\da-fA-F]{1,4}:){4}((:[\\da-fA-F]{1,4}){1,2}|:)\$|^([\\da-fA-F]{1,4}:){5}:([\\da-fA-F]{1,4})?\$|^([\\da-fA-F]{1,4}:){6}:\$".toRegex()
        return p.matches(ip)
    }

    // 将16位整数转换为字节数组
    private fun Int.toByteArray(): ByteArray {
        return byteArrayOf((this shr 8 and 0xFF).toByte(), (this and 0xFF).toByte())
    }

    private fun ipv6AddressToByteArray(ipv6: String): ByteArray {
        // 初始化一个大小为16的字节数组来存储IPv6地址
        val bytes = ByteArray(16)

        // 将IPv6地址分成段
        val segments = ipv6.replace("::", ":0:".repeat(8 - ipv6.count { it == ':' })).split(":")

        // 填充每一段
        var index = 0
        for (segment in segments) {
            if (segment.isEmpty()) continue // 处理空段 (即 "::" 部分)
            val segmentBytes = segment.toInt(16).toByteArray()
            segmentBytes.copyInto(bytes, index)
            index += 2
        }

        return bytes
    }

    fun getIpv4InetAddress(ip: String): InetAddress {
        if (!ipv4AddressVerify(ip)) {
            throw IllegalArgumentException("Invalid IPv4 address")
        }
        val address = ip.split(".").map { it.toInt().toByte() }.toByteArray()
        return InetAddress.getByAddress(address)
    }

    fun getIpv6InetAddress(ip: String): InetAddress {
        if (!ipv6AddressVerify(ip)) {
            throw IllegalArgumentException("Invalid IPv6 address")
        }
        return InetAddress.getByAddress(ipv6AddressToByteArray(ip))
    }
}