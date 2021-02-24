package kr.co.petdoc.petdoc.adapter.pet_add

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_pet_kind_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.response.submodel.PetKindData

/**
 * Petdoc
 * Class: PetKindAdapter
 * Created by kimjoonsung on 2020/04/07.
 *
 * Description :
 */
class PetKindAdapter : RecyclerView.Adapter<PetKindAdapter.PetKindHolder>() {

    private var mItems:MutableList<PetKindData> = mutableListOf()
    private var mListener: AdapterListener? = null

    private var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PetKindHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_pet_kind_item, parent, false))

    override fun onBindViewHolder(holder: PetKindHolder, position: Int) {
        mItems[position].let { item->
            if(selectedPosition == position) {
                holder.itemView.imageViewCheck.visibility = View.VISIBLE
                holder.itemView.textViewPetKind.setTypeface(null, Typeface.BOLD)
            } else {
                holder.itemView.imageViewCheck.visibility = View.GONE
                holder.itemView.textViewPetKind.setTypeface(null, Typeface.NORMAL)
            }

            if (itemCount - 1 == position) {
                holder.itemView.divider1.visibility = View.VISIBLE
            } else {
                holder.itemView.divider1. visibility = View.GONE
            }

            holder.itemView.textViewPetKind.text = item.name

            holder.itemView.setOnClickListener {
                selectedPosition = position
                mListener?.onItemClicked(item)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount() = mItems.size

    fun updateData(items: MutableList<PetKindData>) {
        this.mItems = items
        notifyDataSetChanged()
    }

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    fun setPosition(position: Int) {
        this.selectedPosition = position
        notifyItemChanged(position)
    }

    interface AdapterListener {
        fun onItemClicked(item:PetKindData)
    }

    inner class PetKindHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}