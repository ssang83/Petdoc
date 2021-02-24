package kr.co.petdoc.petdoc.utils

import java.security.MessageDigest

fun sha256(str: String): String {
    val hexChars = "0123456789abcdef"
    val bytes = MessageDigest
        .getInstance("SHA-256")
        .digest(str.toByteArray())
    val result = StringBuilder(bytes.size * 2)
    bytes.forEach {
        val i = it.toInt()
        result.append(hexChars[i shr 4 and 0x0f])
        result.append(hexChars[i and 0x0f])
    }
    return result.toString()
}

