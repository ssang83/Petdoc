package kr.co.petdoc.petdoc.scanner

import java.util.regex.Pattern

fun String.compareScannerVersion(version: String): Int {
    val s1 = normalisedVersion(this)
    val s2 = normalisedVersion(version)
    return s1.compareTo(s2)
}

private fun normalisedVersion(version: String): String {
    return normalisedVersion(version, ".", 4)
}

private fun normalisedVersion(version: String, seperator: String, maxWidth: Int): String {
    val split = Pattern.compile(seperator, Pattern.LITERAL).split((version))
    return with (StringBuilder()) {
        split.forEach {
            append(String.format("%" + maxWidth + 's', it))
        }
        toString()
    }
}
