package kr.co.petdoc.petdoc.adapter.petcheckresult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.AdapterDelegate
import kr.co.petdoc.petdoc.base.BindingViewHolder
import kr.co.petdoc.petdoc.databinding.BloodResultDelegateItemFooterBinding
import kr.co.petdoc.petdoc.model.healthcareresult.BloodResultFooter
import kr.co.petdoc.petdoc.model.healthcareresult.PetCheckResultSection

class BloodResultFooterDelegate : AdapterDelegate<PetCheckResultSection> {
    override fun isForViewType(items: List<PetCheckResultSection>, position: Int): Boolean {
        return items[position] is BloodResultFooter
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return FooterViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.blood_result_delegate_item_footer,
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
    ) {}

    inner class FooterViewHolder(view: View): BindingViewHolder<BloodResultDelegateItemFooterBinding>(view)
}