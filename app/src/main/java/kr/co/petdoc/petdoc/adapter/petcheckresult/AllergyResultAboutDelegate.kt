package kr.co.petdoc.petdoc.adapter.petcheckresult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.allergy_result_delegate_item_about.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.AllergeTestAboutActivity
import kr.co.petdoc.petdoc.adapter.AdapterDelegate
import kr.co.petdoc.petdoc.base.BindingViewHolder
import kr.co.petdoc.petdoc.databinding.AllergyResultDelegateItemAboutBinding
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.model.healthcareresult.AllergyResultAbout
import kr.co.petdoc.petdoc.model.healthcareresult.PetCheckResultSection

class AllergyResultAboutDelegate : AdapterDelegate<PetCheckResultSection> {
    override fun isForViewType(items: List<PetCheckResultSection>, position: Int): Boolean {
        return items[position] is AllergyResultAbout
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return AboutViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.allergy_result_delegate_item_about,
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

    inner class AboutViewHolder(view: View): BindingViewHolder<AllergyResultDelegateItemAboutBinding>(view) {
        init {
            itemView.btnAllergeTestDesc.setOnClickListener {
                it.context.startActivity<AllergeTestAboutActivity>()
            }
        }
    }
}