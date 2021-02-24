package kr.co.petdoc.petdoc.extensions

import android.view.View
import kr.co.petdoc.petdoc.widget.OnSingleClickListener

/**
 * Petdoc
 * Class: ViewExtension
 * Created by kimjoonsung on 1/4/21.
 *
 * Description :
 */
fun View.setOnSingleClickListener(l: View.OnClickListener) {
    setOnClickListener(OnSingleClickListener(l))
}

fun View.setOnSingleClickListener(l: (View) -> Unit) {
    setOnClickListener(OnSingleClickListener(l))
}