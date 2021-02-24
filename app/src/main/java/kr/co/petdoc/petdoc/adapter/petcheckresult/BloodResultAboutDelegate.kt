package kr.co.petdoc.petdoc.adapter.petcheckresult

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.blood_result_delegate_item_about.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BloodTestAboutActivity
import kr.co.petdoc.petdoc.adapter.AdapterDelegate
import kr.co.petdoc.petdoc.base.BindingViewHolder
import kr.co.petdoc.petdoc.databinding.BloodResultDelegateItemAboutBinding
import kr.co.petdoc.petdoc.extensions.startActivity
import kr.co.petdoc.petdoc.model.healthcareresult.BloodResultAbout
import kr.co.petdoc.petdoc.model.healthcareresult.PetCheckResultSection

/**
 * Petdoc
 * Class: BloodResultAboutDelegate
 * Created by kimjoonsung on 1/26/21.
 *
 * Description :
 */
class BloodResultAboutDelegate : AdapterDelegate<PetCheckResultSection> {
    override fun isForViewType(items: List<PetCheckResultSection>, position: Int): Boolean {
        return items[position] is BloodResultAbout
    }

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return AboutViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.blood_result_delegate_item_about,
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

    inner class AboutViewHolder(view: View) : BindingViewHolder<BloodResultDelegateItemAboutBinding>(view) {
        init {
            itemView.btnBloodTestDesc.setOnClickListener {
                it.context.startActivity<BloodTestAboutActivity>()
            }
        }
    }
}