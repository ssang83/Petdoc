package kr.co.petdoc.petdoc.adapter.pet_add

import android.graphics.Typeface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.adapter_pet_weight_footer_item.view.*
import kotlinx.android.synthetic.main.adapter_pet_weight_item.view.*
import kotlinx.android.synthetic.main.fragment_pet_age.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.petadd.PetBreedData
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: PetWeightAdapter
 * Created by kimjoonsung on 2020/04/08.
 *
 * Description : 반려동물 품종 모를 경우 사용하는 어댑터
 */
class PetWeightAdapter : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_FOOTER = 0
        private const val TYPE_ITEM = 1
    }

    private var mItems:MutableList<PetBreedData> = mutableListOf()
    private var mListener:AdapterListener? = null

    private var selectedPosition = -1
    private var petName = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) : RecyclerView.ViewHolder {
        when (viewType) {
            TYPE_ITEM -> {
                return PetWeighHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_pet_weight_item, parent, false))
            }

            else -> {
                return PetWeightFooterHolder(LayoutInflater.from(parent.context)
                    .inflate(R.layout.adapter_pet_weight_footer_item, parent, false))
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder.itemViewType) {
            TYPE_ITEM -> {
                mItems[position].let { item ->
                    if (selectedPosition == position) {
                        holder.itemView.imageViewCheck.visibility = View.VISIBLE
                        holder.itemView.textViewPetWeight.setTypeface(null, Typeface.BOLD)
                    } else {
                        holder.itemView.imageViewCheck.visibility = View.GONE
                        holder.itemView.textViewPetWeight.setTypeface(null, Typeface.NORMAL)
                    }

                    if (itemCount - 2 == position) {
                        holder.itemView.divider1.visibility = View.VISIBLE
                    } else {
                        holder.itemView.divider1.visibility = View.GONE
                    }

                    holder.itemView.textViewPetWeight.text = item.name

                    holder.itemView.setOnClickListener {
                        selectedPosition = position
                        mListener?.onItemClicked(item)
                        notifyDataSetChanged()
                    }
                }
            }

            TYPE_FOOTER -> {
                val str1 = Helper.getCompleteWordByJongsung(petName, "이의", "의")
                val str2 = Helper.getCompleteWordByJongsung(petName, "이가", "가")

                holder.itemView.textViewDesc.text = "${str1} 크기는 어느정도 인가요? ${str2} 아직 성장기라면 다 자란 후의 예상 크기를 선택해주세요."
            }
        }
    }

    override fun getItemCount() = mItems.size + 1

    override fun getItemViewType(position: Int): Int {
        if (position == mItems.size) {
            return TYPE_FOOTER
        } else {
            return TYPE_ITEM
        }
    }

    fun updateData(items: MutableList<PetBreedData>) {
        this.mItems = items
        notifyDataSetChanged()
    }

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    fun setPetName(name: String) {
        this.petName = name
    }

    fun setPosition(position: Int) {
        this.selectedPosition = position
        notifyItemChanged(position)
    }

    interface AdapterListener {
        fun onItemClicked(item:PetBreedData)
    }

    inner class PetWeighHolder(itemView:View) : RecyclerView.ViewHolder(itemView)
    inner class PetWeightFooterHolder(itemView: View) : RecyclerView.ViewHolder(itemView)
}