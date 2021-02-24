package kr.co.petdoc.petdoc.adapter.mypage.bookmark

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.fragment_bookmark.view.*
import kotlinx.android.synthetic.main.view_hospital_type.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.response.submodel.MagazineBookmarkData

/**
 * Petdoc
 * Class: PetGuideAdapter
 * Created by kimjoonsung on 2020/04/13.
 *
 * Description : 반려백과 어댑터
 */
class PetGuideAdapter : RecyclerView.Adapter<PetGuideAdapter.PetGuideHolder>() {

    private var mItems:MutableList<MagazineBookmarkData> = mutableListOf()
    private var mListener:AdapterListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PetGuideHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_bookmark_pet_guide_item, parent, false))

    override fun onBindViewHolder(holder: PetGuideHolder, position: Int) {
        mItems[position].let { item ->
            holder.itemView.title.text = item.title
            holder.itemView.type.text = item.categoryName

            holder.itemView.setOnClickListener {
                mListener?.onItemClicked(item)
            }
        }
    }

    override fun getItemCount() = mItems.size

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    fun updateData(items: List<MagazineBookmarkData>) {
        this.mItems.clear()
        this.mItems.addAll(items)
        notifyDataSetChanged()
    }

    fun addItems(items: List<MagazineBookmarkData>) {
        if(items.isNotEmpty()) {
            for(i in 0 until items.size) {
                this.mItems.add(items[i])
            }

            notifyItemInserted(mItems.size - items.size)
        }
    }

    inner class PetGuideHolder(view:View) : RecyclerView.ViewHolder(view)

    interface AdapterListener{
        fun onItemClicked(item:MagazineBookmarkData)
    }
}