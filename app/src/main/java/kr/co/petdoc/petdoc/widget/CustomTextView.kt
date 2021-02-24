package com.tripbtoz.component.ui.text

import android.content.Context
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Typeface
import android.text.SpannableStringBuilder
import android.util.AttributeSet
import androidx.appcompat.widget.AppCompatTextView

class CustomTextView(var mContext:Context, var attrs:AttributeSet?) : AppCompatTextView(mContext, attrs){

    constructor(mContext:Context) : this(mContext, null)
    constructor(mContext:Context, attrs:AttributeSet?, res:Int) : this(mContext, attrs)

    companion object {
        var nanumFont : Typeface? = null
        var nanumFontBold : Typeface? = null
    }

    init{
        if(nanumFont==null) nanumFont = Typeface.createFromAsset(mContext.assets, "notosan1.otf")
        if(nanumFontBold==null) nanumFontBold = Typeface.createFromAsset(mContext.assets, "notosan2.otf")

        val typearray = attrs?.getAttributeValue("http://tripbtoz.com/resource/id", "textStyle") ?: ""
        val keepPadding = attrs?.getAttributeBooleanValue("http://tripbtoz.com/resource/id", "keepPadding", false)

        typeface = when(typearray){
            "bold" -> nanumFontBold
            else -> nanumFont
        }


        includeFontPadding = false
        if(keepPadding == false) setPadding(0,0,0,0)
        setLineSpacing(0f, 0.96f)

    }

    fun changeBold(bold:Boolean){
        typeface = if(bold) nanumFontBold  else nanumFont
    }

    override fun setText(text: CharSequence?, type: BufferType?) {
        if(text !is SpannableStringBuilder){
            val textWithSpecial = text?.toString()?.replace(" ", "\u00A0")
            super.setText(textWithSpecial, type)
        }else super.setText(text, type)
    }


    var hasStroke = false
    var strokeSize = 0f
    var strokeColor = 0

    override fun onDraw(canvas: Canvas?) {

        if(hasStroke){
            val states = textColors
            paint.style = Paint.Style.STROKE
            paint.strokeWidth = strokeSize
            setTextColor(strokeColor)
            super.onDraw(canvas)

            paint.style = Paint.Style.FILL
            setTextColor(states)
        }

        super.onDraw(canvas)
    }

}