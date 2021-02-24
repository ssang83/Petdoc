package kr.co.petdoc.petdoc.widget

import android.content.Context
import android.graphics.Typeface
import android.text.SpannableString
import android.text.Spanned
import android.text.style.BulletSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.domain.BloodComment

/**
 * Petdoc
 * Class: BloodCommentView
 * Created by kimjoonsung on 1/26/21.
 *
 * Description :
 */
class BloodCommentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0
) : LinearLayout(context, attrs, defStyleAttrs) {
    private lateinit var comment:List<BloodComment>

    init {
        orientation = VERTICAL
        val paddingStartEnd = context.resources.getDimensionPixelSize(R.dimen.padding_16dp)
        val paddingBottom = context.resources.getDimensionPixelSize(R.dimen.padding_34dp)
        setPadding(paddingStartEnd, 0, paddingStartEnd, paddingBottom)
    }

    fun bindComment(comment: List<BloodComment>) {
        this.comment = comment
        if (isNeedToShowHead()) {
            bindHeadTitle()
            bindHeadDescription()
            addSeparateLine()
            addView(getSubDetailTitle())
            bindDetailSection1()
        } else {
            bindDetailSection()
        }
    }

    private fun isNeedToShowHead(): Boolean {
        return comment[0].category == "백혈구" || comment[0].category == "적혈구"
    }

    private fun bindHeadTitle() {
        val headTitle = resources.getString(R.string.allergy_comment_head_title, "${comment[0].categoryName} ${comment[0].category}")
        addView(getTitleTextView(headTitle))
    }

    private fun bindHeadDescription() {
        if (comment[0].upComment?.isNotEmpty() == true) {
            addView(getBulletTextView("증가"))
            addView(getDescTextView(comment[0].upComment ?: ""))
        }

        if (comment[0].downComment?.isNotEmpty() == true) {
            addView(getBulletTextView("감소"))
            addView(getDescTextView(comment[0].downComment ?: ""))
        }
    }

    private fun bindDetailSection() {
        for (i in 0 until comment.size) {
            if(i == 0) {
                addView(getDetailSectionTitleView34("${comment[i].categoryName}\n${comment[i].categoryNameKor}" ?: ""))
            } else {
                addView(getDetailSectionTitleView("${comment[i].categoryName}\n${comment[i].categoryNameKor}" ?: ""))
            }

            if (comment[i].upComment?.isNotEmpty() == true) {
                addView(getBulletTextView("증가"))
                addView(getDescTextView(comment[i].upComment ?: ""))
            }

            if (comment[i].downComment?.isNotEmpty() == true) {
                addView(getBulletTextView("감소"))
                addView(getDescTextView(comment[i].downComment ?: ""))
            }
        }
    }

    private fun bindDetailSection1() {
        for (i in 1 until comment.size) {
            if (i == 0) {
                addView(getDetailSectionTitleView24("${comment[i].categoryName}\n${comment[i].categoryNameKor}" ?: ""))
            } else {
                addView(getDetailSectionTitleView("${comment[i].categoryName}\n${comment[i].categoryNameKor}" ?: ""))
            }

            if (comment[i].upComment?.isNotEmpty() == true) {
                addView(getBulletTextView("증가"))
                addView(getDescTextView(comment[i].upComment ?: ""))
            }

            if (comment[i].downComment?.isNotEmpty() == true) {
                addView(getBulletTextView("감소"))
                addView(getDescTextView(comment[i].downComment ?: ""))
            }
        }
    }

    private fun getDetailSectionTitleView(content: String): View {
        val sectionTitle = inflate(context, R.layout.lay_comment_section_title, null)
        sectionTitle.setPadding(0, resources.getDimensionPixelSize(R.dimen.padding_32dp), 0, 0)
        sectionTitle.findViewById<TextView>(R.id.tvTitle).setText(content)
        return sectionTitle
    }

    private fun getDetailSectionTitleView24(content: String): View {
        val sectionTitle = inflate(context, R.layout.lay_comment_section_title, null)
        sectionTitle.setPadding(0, resources.getDimensionPixelSize(R.dimen.margin_24dp), 0, 0)
        sectionTitle.findViewById<TextView>(R.id.tvTitle).setText(content)
        return sectionTitle
    }

    private fun getDetailSectionTitleView34(content: String): View {
        val sectionTitle = inflate(context, R.layout.lay_comment_section_title, null)
        sectionTitle.setPadding(0, resources.getDimensionPixelSize(R.dimen.padding_34dp), 0, 0)
        sectionTitle.findViewById<TextView>(R.id.tvTitle).setText(content)
        return sectionTitle
    }

    private fun getBulletTextView(content: String) =
        TextView(context).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 18f)
            setTextColor(ContextCompat.getColor(context, R.color.slateGrey))
            setTypeface(null, Typeface.BOLD)

            val bulletColor = ContextCompat.getColor(context, R.color.salmonPinkThree)
            val bulletGap = context.resources.getDimensionPixelOffset(R.dimen.textview_bullet_margin)
            val spannableString = SpannableString(content).apply {
                setSpan(BulletSpan(bulletGap, bulletColor), 0, content.length - 1,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
            }
            text = spannableString

            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, resources.getDimensionPixelSize(R.dimen.margin_20dp), 0, 0)
            }
        }

    private fun getTitleTextView(content: String) =
        TextView(context).apply {
            this.text = content
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
            setTextColor(ContextCompat.getColor(context, R.color.slateGrey))
            setTypeface(null, Typeface.BOLD)
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, resources.getDimensionPixelSize(R.dimen.padding_34dp), 0, 0)
            }
        }

    private fun getSubDetailTitle() =
        TextView(context).apply {
            this.text = "세부 항목"
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
            setTextColor(ContextCompat.getColor(context, R.color.slateGrey))
            setTypeface(null, Typeface.BOLD)
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, resources.getDimensionPixelSize(R.dimen.padding_32dp), 0, 0)
            }
        }

    private fun getDescTextView(desc: String) =
        TextView(context).apply {
            this.text = desc
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            setTextColor(ContextCompat.getColor(context, R.color.slateGrey))
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, resources.getDimensionPixelSize(R.dimen.margin_8dp), 0, 0)
            }
        }

    private fun addSeparateLine() {
        addView(TextView(context).apply {
            background = ContextCompat.getDrawable(context, R.drawable.dash_ligth_grey)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(
                    0,
                    resources.getDimensionPixelSize(R.dimen.margin_20dp),
                    0,
                    0)
            }
        })
    }
}