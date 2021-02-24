package kr.co.petdoc.petdoc.widget

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatImageView
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.screenRectPx
import kotlin.math.roundToInt

class BannerImageView : AppCompatImageView {
    constructor(context: Context): super(context)

    constructor(context: Context, attrs: AttributeSet): super(context, attrs) {
        initAttributeSet(attrs, 0)
    }

    constructor(context: Context, attrs: AttributeSet, defStyle: Int): super(context, attrs, defStyle) {
        initAttributeSet(attrs, defStyle)
    }

    private var heightFactor = 0.4f

    private val desiredWidth by lazy {
        screenRectPx.width() -
            (resources.getDimensionPixelSize(R.dimen.banner_image_view_side_margin) * 2)
    }

    private val desiredHeight by lazy {
        (desiredWidth * heightFactor).roundToInt()
    }

    private fun initAttributeSet(attrs: AttributeSet, defStyleAttr: Int) {
        val typedArray = context.obtainStyledAttributes(attrs, R.styleable.BannerImageView, defStyleAttr, 0)
        heightFactor = typedArray.getFloat(R.styleable.BannerImageView_heightFactor, 0f)

        typedArray.recycle()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        setMeasuredDimension(desiredWidth, desiredHeight)
    }
}