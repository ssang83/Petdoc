package kr.co.petdoc.petdoc.adapter.petcheckresult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.AdapterDelegate
import kr.co.petdoc.petdoc.base.BindingViewHolder
import kr.co.petdoc.petdoc.databinding.AllergyResultDelegateItemClassBinding
import kr.co.petdoc.petdoc.model.healthcareresult.AllergyClass
import kr.co.petdoc.petdoc.model.healthcareresult.PetCheckResultSection

class AllergyClassDelegate : AdapterDelegate<PetCheckResultSection> {
    override fun isForViewType(items: List<PetCheckResultSection>, position: Int): Boolean {
        return items[position] is AllergyClass
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return ClassViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.allergy_result_delegate_item_class,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(items: List<PetCheckResultSection>, position: Int, holder: RecyclerView.ViewHolder, payloads: List<*>) {
        val item = items[position] as AllergyClass
        (holder as ClassViewHolder).bind(item)
    }

    inner class ClassViewHolder(view: View): BindingViewHolder<AllergyResultDelegateItemClassBinding>(view) {
        fun bind(item: AllergyClass) {
            binding.classData = item
        }
    }
}