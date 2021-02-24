package kr.co.petdoc.petdoc.adapter.pet_add

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_breed_search_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.response.submodel.PetSpeciesData

/**
 * Petdoc
 * Class: SpeciesAdapter
 * Created by kimjoonsung on 2020/08/24.
 *
 * Description :
 */
class SpeciesAdapter : RecyclerView.Adapter<SpeciesAdapter.SpeciesHolder>() {

    private var mItems:MutableList<PetSpeciesData> = mutableListOf()
    private var mListener: AdapterListener? = null

    var selectedPosition = -1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        SpeciesHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_breed_search_item, parent, false))

    override fun onBindViewHolder(holder: SpeciesHolder, position: Int) {
        mItems[position].let { item ->
            if (selectedPosition == position) {
                holder.itemView.imageViewCheck.visibility = View.VISIBLE
                holder.itemView.textViewPetBreed.setTypeface(null, Typeface.BOLD)
            } else {
                holder.itemView.imageViewCheck.visibility = View.GONE
                holder.itemView.textViewPetBreed.setTypeface(null, Typeface.NORMAL)
            }

            if (itemCount - 1 == position) {
                holder.itemView.divider1.visibility = View.VISIBLE
            } else {
                holder.itemView.divider1.visibility = View.GONE
            }

            holder.itemView.textViewPetBreed.text = item.name

            holder.itemView.setOnClickListener {
                selectedPosition = position
                mListener?.onItemClicked(item)
                notifyDataSetChanged()
            }
        }
    }

    override fun getItemCount() = mItems.size

    fun updateData(items: MutableList<PetSpeciesData>) {
        this.mItems.clear()
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
        fun onItemClicked(item: PetSpeciesData)
    }

    class SpeciesHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}