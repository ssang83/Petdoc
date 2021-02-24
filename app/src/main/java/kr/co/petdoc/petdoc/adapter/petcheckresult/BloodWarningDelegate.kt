package kr.co.petdoc.petdoc.adapter.petcheckresult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.AdapterDelegate
import kr.co.petdoc.petdoc.base.BindingViewHolder
import kr.co.petdoc.petdoc.databinding.BloodResultDelegateItemWarningBinding
import kr.co.petdoc.petdoc.model.healthcareresult.BloodWarning
import kr.co.petdoc.petdoc.model.healthcareresult.PetCheckResultSection
import kr.co.petdoc.petdoc.viewmodel.MyHealthCareResultViewModel

class BloodWarningDelegate(
    private val viewModel: MyHealthCareResultViewModel
) : AdapterDelegate<PetCheckResultSection> {
    override fun isForViewType(items: List<PetCheckResultSection>, position: Int): Boolean {
        return items[position] is BloodWarning
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return BloodWarningViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.blood_result_delegate_item_warning,
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
        val item = items[position] as BloodWarning
        (holder as BloodWarningViewHolder).bind(item)
    }

    inner class BloodWarningViewHolder(view: View): BindingViewHolder<BloodResultDelegateItemWarningBinding>(view) {
        fun bind(item: BloodWarning) {
            binding.item ?: run {
                binding.viewModel = viewModel
                binding.item = item
            }
        }
    }
}