package kr.co.petdoc.petdoc.widget

import android.content.Context
import android.util.AttributeSet
import android.view.MotionEvent
import androidx.viewpager.widget.ViewPager

/**
 * petdoc-android
 * Class: NonSwipeableViewPager
 * Created by kimjoonsung on 2020/04/02.
 *
 * Description : Swipe 활성 / 비 활성 가능하도록 확장된 ViewPager
 */
class NonSwipeableViewPager : ViewPager {

    private var swipeEnable: Boolean = false

    constructor(context: Context) : super(context)
    constructor(context: Context, attributes: AttributeSet) : super(context, attributes)

    override fun onInterceptTouchEvent(ev: MotionEvent?): Boolean {
        return if (swipeEnable) {
            super.onInterceptTouchEvent(ev)
        } else {
            if (ev?.actionMasked == MotionEvent.ACTION_MOVE) {
                // ignore move action
            } else {
                if (super.onInterceptTouchEvent(ev)) {
                    super.onTouchEvent(ev)
                }
            }
            false
        }
    }

    override fun onTouchEvent(ev: MotionEvent?): Boolean {
        return if (swipeEnable) {
            super.onTouchEvent(ev)
        } else {
            ev?.actionMasked != MotionEvent.ACTION_MOVE && super.onTouchEvent(ev)
        }
    }

    /**
     * Swipe 가능 설정
     * @param enable
     */
    fun setSwipeEnable(enable: Boolean) {
        swipeEnable = enable
    }

    fun isSwipeEnable(): Boolean = swipeEnable
}