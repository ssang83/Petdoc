package kr.co.petdoc.petdoc.extensions

import android.os.Handler
import androidx.viewpager2.widget.ViewPager2

fun ViewPager2.autoScroll(interval: Long) {
    val scrollHandler = Handler()
    val autoScrolling = Runnable {
        setCurrentItem(currentItem + 1, true)
        autoScroll(interval)
    }
    scrollHandler.postDelayed(autoScrolling, interval)
}