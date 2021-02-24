package kr.co.petdoc.petdoc.adapter.pet_add

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_breed_search_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.response.submodel.PetSpeciesData

/**
 * Petdoc
 * Class: BreedKnowAdapter
 * Created by kimjoonsung on 2020/04/08.
 *
 * Description : 반려동물 품종 검색 어댑터
 */
class BreedSearchAdapter : RecyclerView.Adapter<BreedSearchAdapter.BreedSearchHolder>(){

    private var mItems:MutableList<PetSpeciesData> = mutableListOf()
    private var mListener:AdapterListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BreedSearchHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_breed_search_item, parent, false))

    override fun onBindViewHolder(holder: BreedSearchHolder, position: Int) {
        mItems[position].let { item ->
            if (itemCount - 1 == position) {
                holder.itemView.divider1.visibility = View.VISIBLE
            } else {
                holder.itemView.divider1.visibility = View.GONE
            }

            holder.itemView.textViewPetBreed.text = item.name

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item)
            }
        }
    }

    override fun getItemCount() = mItems.size

    fun updateData(items: MutableList<PetSpeciesData>?) {
        if (items != null) {
            this.mItems.clear()
            this.mItems = items
        } else {
            this.mItems.clear()
        }

        notifyDataSetChanged()
    }

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    interface AdapterListener {
        fun onItemClicked(item:PetSpeciesData)
    }

    inner class BreedSearchHolder(itemView:View) : RecyclerView.ViewHolder(itemView)
}