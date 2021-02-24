package kr.co.petdoc.petdoc.adapter.petcheckresult

import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.AdapterDelegate
import kr.co.petdoc.petdoc.base.BindingViewHolder
import kr.co.petdoc.petdoc.databinding.BloodResultDelegateItemHeaderBinding
import kr.co.petdoc.petdoc.model.healthcareresult.BloodResultHeader
import kr.co.petdoc.petdoc.model.healthcareresult.PetCheckResultSection

class BloodResultHeaderDelegate : AdapterDelegate<PetCheckResultSection> {
    override fun isForViewType(items: List<PetCheckResultSection>, position: Int): Boolean {
        return items[position] is BloodResultHeader
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return HeaderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.blood_result_delegate_item_header,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        items: List<PetCheckResultSection>,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: List<*>
    ) {
        val item = items[position] as BloodResultHeader
        (holder as HeaderViewHolder).bind(item)
    }

    inner class HeaderViewHolder(view: View): BindingViewHolder<BloodResultDelegateItemHeaderBinding>(view) {
        fun bind(item: BloodResultHeader) {
            val builder = SpannableStringBuilder(itemView.context.getString(R.string.blood_test_warning_count, item.warningCnt))
            builder.setSpan(ForegroundColorSpan(ContextCompat.getColor(itemView.context, R.color.orange)), 20, 23, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            binding.warningCount.text = builder
        }
    }
}