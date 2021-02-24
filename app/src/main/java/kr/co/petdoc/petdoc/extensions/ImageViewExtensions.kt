package kr.co.petdoc.petdoc.extensions

import android.view.View
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import android.widget.ImageView

fun ImageView.fadeOutAndHideImage() {
    val fadeOut: Animation = AlphaAnimation(1f, 0f).apply {
        interpolator = AccelerateInterpolator()
        duration = 1000
    }

    fadeOut.setAnimationListener(object : Animation.AnimationListener {
        override fun onAnimationEnd(animation: Animation?) {
            visibility = View.GONE
        }

        override fun onAnimationRepeat(animation: Animation?) {}
        override fun onAnimationStart(animation: Animation?) {}
    })

    startAnimation(fadeOut)
}