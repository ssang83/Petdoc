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
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.domain.AllergyComment

class AllergeCommentView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0
) : LinearLayout(context, attrs, defStyleAttrs) {
    private lateinit var comment: AllergyComment
    init {
        orientation = VERTICAL
        val paddingStartEnd = context.resources.getDimensionPixelSize(R.dimen.padding_15dp)
        val paddingTopBottom = context.resources.getDimensionPixelSize(R.dimen.padding_32dp)
        setPadding(paddingStartEnd, paddingTopBottom, paddingStartEnd, paddingTopBottom)
    }

    fun bindComment(comment: AllergyComment) {
        this.comment = comment
        if (isNeedToShowHead()) {
            bindHead()
            addSeparateLine()
        }
        bindDetail()
    }

    private fun isNeedToShowHead(): Boolean {
        return comment.categoryName != "기타"
    }

    private fun bindHead() {
        bindHeadTitle()
        bindHeadDescription()
    }

    private fun bindHeadTitle() {
        val headTitle = resources.getString(R.string.allergy_comment_head_title, comment.categoryName)
        addView(getTitleTextView(headTitle))
    }

    private fun bindHeadDescription() {
        if (comment.feature?.isNotEmpty() == true) {
            addView(getBulletTextView("특징"))
            addView(getDescTextView(comment.feature?:""))
        }
        if (comment.managementMethod?.isNotEmpty() == true) {
            addView(getBulletTextView("관리법"))
            addView(getDescTextView(comment.managementMethod?:""))
        }
    }

    private fun bindDetail() {
        bindDetailTitle()
        bindDetailSections()
    }

    private fun bindDetailTitle() {
        if (comment.categoryName == "기타") {
            addView(getTitleTextView("기타 항목"))
        } else {
            addView(getTitleTextView("세부 항목"))
        }
    }

    private fun bindDetailSections() {
        comment.commentDetails?.forEach {
            addView(getDetailSectionTitleView(it.itemTitle?:""))
            if (it.habitat?.isNotEmpty() == true) {
                addView(getBulletTextView("서식지"))
                addView(getDescTextView(it.habitat?:""))
            }
            if (it.itemFeature?.isNotEmpty() == true) {
                addView(getBulletTextView("특징"))
                addView(getDescTextView(it.itemFeature?:""))
            }
            if (it.itemManagementMethod?.isNotEmpty() == true) {
                addView(getBulletTextView("관리법"))
                addView(getDescTextView(it.itemManagementMethod?:""))
            }
            if (it.itemNote?.isNotEmpty() == true) {
                addView(getBulletTextView("참고 사항"))
                addView(getDescTextView(it.itemNote?:""))
            }
        }
    }

    private fun getTitleTextView(content: String): TextView {
        return TextView(context).apply {
            this.text = content
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 22f)
            setTextColor(ContextCompat.getColor(context, R.color.slateGrey))
            setTypeface(null, Typeface.BOLD)
            layoutParams = ViewGroup.LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        }
    }

    private fun getDetailSectionTitleView(content: String): View {
        val sectionTitle = inflate(context, R.layout.lay_comment_section_title, null)
        sectionTitle.setPadding(0, resources.getDimensionPixelSize(R.dimen.margin_20dp), 0, 0)
        sectionTitle.findViewById<TextView>(R.id.tvTitle).setText(content)
        return sectionTitle
    }

    private fun getDescTextView(desc: String): TextView {
        return TextView(context).apply {
            this.text = desc
            setTextSize(TypedValue.COMPLEX_UNIT_DIP, 16f)
            setTextColor(ContextCompat.getColor(context, R.color.slateGrey))
            layoutParams = LayoutParams(LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(0, resources.getDimensionPixelSize(R.dimen.margin_10dp), 0, 0)
            }
        }
    }

    private fun getBulletTextView(content: String): TextView {
        return TextView(context).apply {
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
    }

    private fun addSeparateLine() {
        addView(TextView(context).apply {
            background = ContextCompat.getDrawable(context, R.drawable.dash_ligth_grey)
            layoutParams = LayoutParams(LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT).apply {
                setMargins(
                    0,
                    resources.getDimensionPixelSize(R.dimen.margin_20dp),
                    0,
                    resources.getDimensionPixelSize(R.dimen.margin_20dp))
            }
        })
    }

}