package kr.co.petdoc.petdoc.adapter.pet_add

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_vaccine_status_item.view.*
import kr.co.petdoc.petdoc.R

/**
 * Petdoc
 * Class: VaccineStatusAdapter
 * Created by kimjoonsung on 2020/04/08.
 *
 * Description : 반려동물 등록 시 예방접종 화면 어댑터
 */
class VaccineStatusAdapter : RecyclerView.Adapter<VaccineStatusAdapter.VaccineStatusHolder>() {

    private var mItems:MutableList<String> = mutableListOf()
    private var mListener:AdapterListener? = null

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        VaccineStatusHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_vaccine_status_item, parent, false))

    override fun onBindViewHolder(holder: VaccineStatusHolder, position: Int) {
        mItems[position].let { item ->
            if(selectedPosition == position) {
                holder.itemView.imageViewCheck.visibility = View.VISIBLE
                holder.itemView.textViewVaccineStatus.setTypeface(null, Typeface.BOLD)
            } else {
                holder.itemView.imageViewCheck.visibility = View.GONE
                holder.itemView.textViewVaccineStatus.setTypeface(null, Typeface.NORMAL)
            }

            if (itemCount - 1 == position) {
                holder.itemView.divider1.visibility = View.VISIBLE
            } else {
                holder.itemView.divider1. visibility = View.GONE
            }

            holder.itemView.textViewVaccineStatus.text = item

            holder.itemView.setOnClickListener {
                selectedPosition = position
                mListener?.onItemClicked(item)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount() = mItems.size

    fun updateData(items: MutableList<String>) {
        this.mItems = items
        notifyDataSetChanged()
    }

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    fun setSelectPosition(position: Int) {
        this.selectedPosition = position
        notifyDataSetChanged()
    }

    interface AdapterListener {
        fun onItemClicked(status:String)
    }

    inner class VaccineStatusHolder(itemView:View) : RecyclerView.ViewHolder(itemView)
}