package kr.co.petdoc.petdoc.utils

import android.content.res.Resources
import android.graphics.Rect
import android.util.DisplayMetrics

private val displayMetrics: DisplayMetrics by lazy { Resources.getSystem().displayMetrics }

val screenRectPx: Rect
    get() = displayMetrics.run { Rect(0, 0, widthPixels, heightPixels) }