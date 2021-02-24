package kr.co.petdoc.petdoc.extensions

import java.text.NumberFormat

fun Int.numberWithComma(): String {
    return NumberFormat.getInstance().format(this)
}