package kr.co.petdoc.petdoc.dialog

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import kr.co.petdoc.petdoc.R

/**
 * Petdoc
 * Class: FindCareCodeDialg
 * Created by kimjoonsung on 12/11/20.
 *
 * Description :
 */
class FindCareCodeDialog(context:Context) : AppCompatDialog(context, true, null) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialgo_find_care_code)

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT

        window!!.setLayout(width, height)
        window!!.setGravity(Gravity.BOTTOM)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }
}