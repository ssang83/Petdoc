package kr.co.petdoc.petdoc.adapter.petcheckresult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.allergy_result_delegate_item_detail_header.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.AllergyCommentActivity
import kr.co.petdoc.petdoc.activity.EXTRA_ALLERGY_COMMENT_CATEGORY
import kr.co.petdoc.petdoc.adapter.AdapterDelegate
import kr.co.petdoc.petdoc.base.BindingViewHolder
import kr.co.petdoc.petdoc.databinding.AllergyResultDelegateItemDetailHeaderBinding
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.model.healthcareresult.AllergyResultDetailHeader
import kr.co.petdoc.petdoc.model.healthcareresult.PetCheckResultSection

class AllergyResultDetailHeaderDelegate : AdapterDelegate<PetCheckResultSection> {
    override fun isForViewType(items: List<PetCheckResultSection>, position: Int): Boolean {
        return items[position] is AllergyResultDetailHeader
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return DetailHeaderViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.allergy_result_delegate_item_detail_header,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(items: List<PetCheckResultSection>, position: Int, holder: RecyclerView.ViewHolder, payloads: List<*>) {
        val item = items[position] as AllergyResultDetailHeader
        (holder as DetailHeaderViewHolder).bind(item)
    }

    inner class DetailHeaderViewHolder(view: View): BindingViewHolder<AllergyResultDelegateItemDetailHeaderBinding>(view) {
        fun bind(item: AllergyResultDetailHeader) {
            binding.detailHeader = item
            binding.tvComment.setOnClickListener {
                itemView.context.startActivity<AllergyCommentActivity> {
                    putExtra(EXTRA_ALLERGY_COMMENT_CATEGORY, item.smallCategory)
                }
            }
        }
    }
}