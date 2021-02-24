package kr.co.petdoc.petdoc.widget

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.domain.BloodResult
import kr.co.petdoc.petdoc.model.healthcareresult.BloodWarning

class BloodResultWarningView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttrs: Int = 0
) : LinearLayout(context, attrs, defStyleAttrs) {
    private var listener: OnQuickSearchButtonListener? = null
    var lastClickedCategory: String? = null

    init {
        orientation = VERTICAL
        background = ContextCompat.getDrawable(context, R.drawable.light_pink_solid_round_14)
    }

    fun setWarnings(model: BloodWarning) {
        model.warningList.forEachIndexed { i, list ->
            addWarningItems(
                list,
                isLastItem = ((i == model.warningList.size - 1))
            )
        }
    }

    private fun addWarningItems(bloodResultList: List<BloodResult>, isLastItem: Boolean) {
        if (bloodResultList.isEmpty()) {
            return
        }
        val warningItemView = inflate(context, R.layout.adapter_blood_test_warning_item, null)
        warningItemView.findViewById<View>(R.id.divider).visibility = if (isLastItem) View.GONE else View.VISIBLE
        warningItemView.findViewById<TextView>(R.id.count).text = bloodResultList.size.toString()
        warningItemView.findViewById<TextView>(R.id.type).text = "${bloodResultList.first().smallCategory} 수치 ${bloodResultList.size} 항목"
        warningItemView.findViewById<TextView>(R.id.detail).text = bloodResultList.joinToString(separator = ",") { it.itemNameEng }
        warningItemView.findViewById<View>(R.id.divider).visibility = if (isLastItem) View.GONE else View.VISIBLE

        warningItemView.findViewById<View>(R.id.btnGo).setOnClickListener {
            lastClickedCategory = bloodResultList.first().smallCategory
            listener?.onQuickSearchButtonClicked()
        }

        addView(warningItemView)
    }

    fun setBloodResultWarningViewListener(listener: OnQuickSearchButtonListener) {
        this.listener = listener
    }

    interface OnQuickSearchButtonListener {
        fun onQuickSearchButtonClicked()
    }

}