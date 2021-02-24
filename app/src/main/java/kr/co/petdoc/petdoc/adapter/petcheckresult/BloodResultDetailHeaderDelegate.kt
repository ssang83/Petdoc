package kr.co.petdoc.petdoc.adapter.petcheckresult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BloodCommentActivity
import kr.co.petdoc.petdoc.activity.EXTRA_BLOOD_COMMENT_CATEGORY
import kr.co.petdoc.petdoc.adapter.AdapterDelegate
import kr.co.petdoc.petdoc.base.BindingViewHolder
import kr.co.petdoc.petdoc.databinding.BloodResultDelegateItemDetailHeaderBinding
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.model.healthcareresult.BloodResultDetailHeader
import kr.co.petdoc.petdoc.model.healthcareresult.PetCheckResultSection

class BloodResultDetailHeaderDelegate : AdapterDelegate<PetCheckResultSection> {
    override fun isForViewType(items: List<PetCheckResultSection>, position: Int): Boolean {
        return items[position] is BloodResultDetailHeader
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return DetailHeaderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.blood_result_delegate_item_detail_header,
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
        val item = items[position] as BloodResultDetailHeader
        (holder as DetailHeaderViewHolder).bind(item)
    }

    inner class DetailHeaderViewHolder(view: View): BindingViewHolder<BloodResultDelegateItemDetailHeaderBinding>(view) {
        fun bind(item: BloodResultDetailHeader) {
            binding.detailHeader = item
            binding.tvComment.setOnClickListener {
                itemView.context.startActivity<BloodCommentActivity> {
                    putExtra(EXTRA_BLOOD_COMMENT_CATEGORY, item.smallCategory)
                }
            }
        }
    }
}