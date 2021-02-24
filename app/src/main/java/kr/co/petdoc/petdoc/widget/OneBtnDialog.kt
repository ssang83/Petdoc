package kr.co.petdoc.petdoc.widget

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.appcompat.app.AppCompatDialog
import kotlinx.android.synthetic.main.dialog_one_button.*
import kr.co.petdoc.petdoc.R

/**
 * Petdoc
 * Class: OneBtnDialog
 * Created by kimjoonsung on 2020/05/21.
 *
 * Description :
 */
class OneBtnDialog(context: Context) : AppCompatDialog(context, true, null) {

    init {
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        setContentView(R.layout.dialog_one_button)

        val width = ViewGroup.LayoutParams.MATCH_PARENT
        val height = ViewGroup.LayoutParams.WRAP_CONTENT

        window!!.setLayout(width, height)
        window!!.setGravity(Gravity.BOTTOM)
        window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        setCanceledOnTouchOutside(true)
        setCancelable(true)
    }

    fun setTitle(_title: String) {
        title.apply {
            when {
                _title.isEmpty() -> {
                    visibility = View.GONE
                }

                else -> {
                    visibility = View.VISIBLE
                    text = _title
                }
            }
        }
    }

    fun setMessage(_msg: String) {
        message.text = _msg
    }

    fun setConfirmButton(buttonText: String, clickListener: View.OnClickListener) {
        btnConfirm.text = buttonText
        btnConfirm.setOnClickListener(clickListener)
    }

}