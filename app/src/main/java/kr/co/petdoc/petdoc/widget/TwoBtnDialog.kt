package kr.co.petdoc.petdoc.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import kotlinx.android.synthetic.main.dialog_two_button.*
import kr.co.petdoc.petdoc.R

/**
 * Petdoc
 * Class: TwoBtnDialog
 * Created by kimjoonsung on 2020/04/29.
 *
 * Description :
 */
class TwoBtnDialog(context: Context) : AppCompatDialog(context, false, null) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_two_button)

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT

        window!!.setLayout(width, height)
        window!!.setGravity(Gravity.BOTTOM)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
    }

    fun setTitle(_title: String) {
        title.text = _title
    }

    fun setMessage(_msg: String) {
        message.text = _msg
    }

    fun setConfirmButton(buttonText: String, clickListener: View.OnClickListener) {
        buttonConfirm.text = buttonText
        buttonConfirm.setOnClickListener(clickListener)
    }

    fun setCancelButton(buttonText: String, clickListener: View.OnClickListener) {
        buttonCancel.text = buttonText
        buttonCancel.setOnClickListener(clickListener)
    }
}