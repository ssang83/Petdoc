package kr.co.petdoc.petdoc.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.util.AttributeSet
import android.view.View
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper

class CircleLineProgressView(_context:Context, _attr:AttributeSet?=null, resId:Int =0) : View(_context, _attr, resId){

    constructor(_context:Context) : this(_context, null, 0)
    constructor(_context:Context, _attr:AttributeSet) : this(_context, _attr, 0)


    var backgroundWidth = 9f
    var progressWidth = 9f

    var bgColor : Int = Helper.readColorRes(R.color.pale)
    var progressColor : Int = Helper.readColorRes(R.color.salmon)
    lateinit var backgroundPaint : Paint
    lateinit var progressPaint : Paint

    var centerX = 0f
    var centerY = 0f
    var radius = 0f
    var oval = RectF()

    var progress = 0f


    init{
        backgroundPaint = Paint().apply{
            color = bgColor
            style = Paint.Style.STROKE
            strokeWidth = backgroundWidth
            isAntiAlias = true
        }

        progressPaint = Paint().apply{
            color = progressColor
            style = Paint.Style.STROKE
            strokeWidth = progressWidth
            isAntiAlias = true
        }

    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {

        centerX = w/2f
        centerY = h/2f

        val fix = progressWidth/2f
        oval.set(0f+fix,0f+fix,w.toFloat()-fix,h.toFloat()-fix)
        radius = w/2f

        super.onSizeChanged(w, h, oldw, oldh)
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)

        //canvas?.drawCircle(centerX, centerY, radius, backgroundPaint)
        canvas?.drawArc(oval, 0f, 360f, false, backgroundPaint )
        canvas?.drawArc(oval, -90f, progress, false, progressPaint )
    }

}