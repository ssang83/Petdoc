package kr.co.petdoc.petdoc.extensions

import android.widget.EditText

fun EditText.setReadOnly(value: Boolean) {
    isFocusable = !value
    isFocusableInTouchMode = !value
}