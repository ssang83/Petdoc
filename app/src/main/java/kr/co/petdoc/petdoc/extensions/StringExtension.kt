package kr.co.petdoc.petdoc.extensions

import android.text.Spannable
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan

fun String.highlight(color: Int, word: String): Spannable {
    var start = indexOf(word)
    if (start < 0) {
        return SpannableString(this)
    }

    val highlightedContent = SpannableString(this)
    while (start >= 0) {
        val spanStart = start.coerceAtMost(length)
        val spanEnd = (start + word.length).coerceAtMost(length)

        highlightedContent.setSpan(ForegroundColorSpan(color), spanStart, spanEnd, Spanned.SPAN_INCLUSIVE_EXCLUSIVE)
        start = indexOf(word, spanEnd)
    }
    return highlightedContent
}