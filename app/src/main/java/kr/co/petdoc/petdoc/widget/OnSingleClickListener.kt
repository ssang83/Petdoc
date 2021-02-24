package kr.co.petdoc.petdoc.widget

import android.view.View

/**
 * Petdoc
 * Class: OnSingleClickListener
 * Created by kimjoonsung on 11/23/20.
 *
 * Description : 연타 방지용 ClickListener
 */
class OnSingleClickListener : View.OnClickListener {

    private val onClickListener:View.OnClickListener

    constructor(listener: View.OnClickListener) {
        onClickListener = listener
    }

    constructor(listener: (View) -> Unit) {
        onClickListener = View.OnClickListener { listener.invoke(it) }
    }

    override fun onClick(v: View?) {
        val currentTimeMillsec = System.currentTimeMillis()

        if (currentTimeMillsec >= previousClickTimeMillisec + DELAY_MILLISEC) {
            onClickListener.onClick(v)
        }

        previousClickTimeMillisec = currentTimeMillsec
    }

    companion object {
        private const val DELAY_MILLISEC = 600L
        private var previousClickTimeMillisec = 0L
    }
}