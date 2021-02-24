package kr.co.petdoc.petdoc.widget

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.os.Handler
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper

class ScrollRulerView(_context:Context, _attributset:AttributeSet?, resId:Int=0) : HorizontalScrollView(_context, _attributset, resId) {

    constructor(_context:Context) : this(_context, null, 0)
    constructor(_context:Context, _attributset: AttributeSet?) : this(_context, _attributset, 0)

    private var layoutWidth = 0
    private var layoutHeight = 0

    private var current_position = 0f
    var max_range = 10

    private var textUnit = 5f

    private var unitWidth = 0
    private var unitHeight = 0
    private var unitMin = 0f

    private var viewContext : Context? = null

    private var insideLayout : LinearLayout? = null

    var callback : PositionChangeCallback? = null

    init{
        viewContext = _context

        insideLayout = LinearLayout(viewContext).apply{
            orientation = LinearLayout.HORIZONTAL
            clipChildren = false
        }
        addView(insideLayout,  ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.MATCH_PARENT))
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        layoutWidth = w
        layoutHeight = h

        unitHeight = h / 5 * 2
        // 111 : 26 = ? : h
        unitWidth = 177 * unitHeight / 38

        unitMin = unitWidth / 10f

        reset()
    }


    private fun reset(){

        insideLayout?.post{

            insideLayout?.removeAllViews()

            var marginViewStart = View(viewContext).apply{
                setBackgroundColor(Color.WHITE)
                layoutParams = LinearLayout.LayoutParams(layoutWidth/2,layoutHeight)
                clipChildren = false
            }

            insideLayout?.addView(marginViewStart)

            //-------------------------------------------------------------------------------------
            val textUnitSize = Helper.convertDpToPx(viewContext!!, textUnit)
            for(count in 0..max_range){
                var nodearea = LinearLayout(viewContext).apply{
                    layoutParams = LinearLayout.LayoutParams( unitWidth, layoutHeight )
                    orientation = LinearLayout.VERTICAL
                    setBackgroundColor(Color.WHITE)
                    clipChildren = false
                }
                    val nodeUnit = ImageView(viewContext).apply{
                        layoutParams = LinearLayout.LayoutParams( unitWidth, unitHeight )
                        setBackgroundResource(R.drawable.ruler_image)
                        clipChildren = false
                    }
                    nodearea.addView(nodeUnit)

                    val nodeText = TextView(viewContext).apply{
                        if(count < 10) {
                            layoutParams = LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT ).apply{
                                setMargins(-30, unitHeight/5, 0, 0)
                            }
                        } else {
                            layoutParams = LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT ).apply{
                                setMargins(-55, unitHeight/5, 0, 0)
                            }
                        }

                        textSize = textUnitSize.toFloat()
                        setTextColor(Helper.readColorRes(R.color.dark_grey))
                        text = "$count"
                        textAlignment = View.TEXT_ALIGNMENT_GRAVITY
                        gravity = Gravity.CENTER
                        setPadding(0,0,0,0)
                        setTypeface(null, Typeface.BOLD)
                        clipChildren = false
                    }
                    nodearea.addView(nodeText)

                insideLayout?.addView(nodearea)
            }
            //-------------------------------------------------------------------------------------

            val marginViewEnd = FrameLayout(viewContext!!).apply{
                layoutParams = LinearLayout.LayoutParams(layoutWidth/2, layoutHeight)
                setBackgroundColor(Color.WHITE)
                clipChildren = false
            }

                //-- last unit node ----------------------------------------------------------------
                var nodearea = LinearLayout(viewContext).apply{
                    layoutParams = LinearLayout.LayoutParams( unitWidth, layoutHeight )
                    orientation = LinearLayout.VERTICAL
                    setBackgroundColor(Color.WHITE)
                    clipChildren = false
                }
                val nodeUnit = ImageView(viewContext).apply{
                    layoutParams = LinearLayout.LayoutParams( unitWidth, unitHeight )
                    setBackgroundResource(R.drawable.ruler_image)
                    clipChildren = false
                }
                nodearea.addView(nodeUnit)

                val nodeText = TextView(viewContext).apply{
                    layoutParams = LinearLayout.LayoutParams( LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT ).apply{
                        setMargins(-70, unitHeight/5, 0, 0)
                    }
                    textSize = textUnitSize.toFloat()
                    setTextColor(Helper.readColorRes(R.color.dark_grey))
                    text = "${max_range+1}"
                    textAlignment = View.TEXT_ALIGNMENT_GRAVITY
                    gravity = Gravity.CENTER
                    setPadding(0,0,0,0)
                    setTypeface(null, Typeface.BOLD)
                    clipChildren = false
                }
                nodearea.addView(nodeText)

                marginViewEnd.addView(nodearea)

                val cover = View(viewContext!!).apply{
                    layoutParams = FrameLayout.LayoutParams((layoutWidth/2)-10, unitHeight).apply{
                        gravity = Gravity.END
                    }
                    setBackgroundColor(Color.WHITE)
                }

                marginViewEnd.addView(cover)
                // ---------------------------------------------------------------------------------

            insideLayout?.addView(marginViewEnd)

            insideLayout?.invalidate()
        }
    }


    override fun onScrollChanged(l: Int, t: Int, oldl: Int, oldt: Int) {
        super.onScrollChanged(l, t, oldl, oldt)

        current_position = l.toFloat() / unitMin

        callback?.positionCallback(current_position/10f)
    }

    var animator : ObjectAnimator? = null
    fun moveToWeight(weight:Float){

        Handler().postDelayed({
            animator = ObjectAnimator.ofInt(this, "scrollX", (weight * 10 * unitMin).toInt())
            animator?.duration = (weight * 120).toLong()
            animator?.start()
        },150)
    }

    fun ready(weight:Int){
        reset()
    }

    interface PositionChangeCallback{
        fun positionCallback(value:Float)
    }
}

