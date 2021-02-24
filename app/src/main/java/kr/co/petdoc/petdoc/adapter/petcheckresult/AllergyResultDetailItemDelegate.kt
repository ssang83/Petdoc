package kr.co.petdoc.petdoc.adapter.petcheckresult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.AdapterDelegate
import kr.co.petdoc.petdoc.base.BindingViewHolder
import kr.co.petdoc.petdoc.databinding.AllergyResultDelegateItemDetailBinding
import kr.co.petdoc.petdoc.model.healthcareresult.AllergyResultDetailItem
import kr.co.petdoc.petdoc.model.healthcareresult.PetCheckResultSection

class AllergyResultDetailItemDelegate : AdapterDelegate<PetCheckResultSection> {
    override fun isForViewType(items: List<PetCheckResultSection>, position: Int): Boolean {
        return items[position] is AllergyResultDetailItem
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return DetailItemViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.allergy_result_delegate_item_detail,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(items: List<PetCheckResultSection>, position: Int, holder: RecyclerView.ViewHolder, payloads: List<*>) {
        val item = items[position] as AllergyResultDetailItem
        (holder as DetailItemViewHolder).bind(item)
    }

    inner class DetailItemViewHolder(view: View): BindingViewHolder<AllergyResultDelegateItemDetailBinding>(view) {
        fun bind(item: AllergyResultDetailItem) {
            binding.item = item
        }
    }
}