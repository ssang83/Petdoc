package kr.co.petdoc.petdoc.adapter.care

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_blood_test_warning_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.response.submodel.BloodDangerList
import kr.co.petdoc.petdoc.network.response.submodel.BloodDangerListSubList
import kr.co.petdoc.petdoc.network.response.submodel.BloodDangerListSubListItem
import kr.co.petdoc.petdoc.network.response.submodel.BloodResultSubListItem

/**
 * Petdoc
 * Class: WarningItemAdapter
 * Created by kimjoonsung on 2020/09/11.
 *
 * Description :
 */
class WarningItemAdapter() : RecyclerView.Adapter<WarningItemAdapter.WarningItemHolder>() {

    private var mItems:BloodDangerList? = null
    private var mListener:AdapterListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        WarningItemHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.adapter_blood_test_warning_item, parent, false)
        )

    override fun onBindViewHolder(holder: WarningItemHolder, position: Int) {
        holder.itemView.divider.apply {
            when {
                position == itemCount - 1 -> {
                    visibility = View.GONE
                }

                else -> visibility = View.VISIBLE
            }
        }


        val count = mItems!![position].size
        val category = mItems!![position][0].smallCategory
        holder.setWarningCount(count.toString())
        holder.setWarningItem("${category} 수치 ${count} 항목")
        holder.setWarningDetail(mItems!![position])

        holder.itemView.btnGo.setOnClickListener {
            mListener?.onGoClicked(mItems!![position])
        }
    }

    override fun getItemCount() = mItems!!.size

    fun setItems(items: BloodDangerList?) {
        mItems = items
        notifyDataSetChanged()
    }

    fun setListener(listener: AdapterListener) {
        mListener = listener
    }

    class WarningItemHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setWarningCount(_count: String) {
            item.count.text = _count
        }

        fun setWarningItem(_item: String) {
            item.type.text = _item
        }

        fun setWarningDetail(subItems: BloodDangerListSubList) {
            item.detail.apply {
                for (i in 0 until subItems.size) {
                    append(subItems[i].itemNameEng)

                    if (i != subItems.size - 1) {
                        append(", ")
                    }
                }
            }
        }
    }

    interface AdapterListener {
        fun onGoClicked(dangerItems: BloodDangerListSubList)
    }
}