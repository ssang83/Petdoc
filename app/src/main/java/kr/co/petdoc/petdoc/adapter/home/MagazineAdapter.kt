package kr.co.petdoc.petdoc.adapter.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_magazine_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.response.submodel.MagazineItem
import org.json.JSONObject

/**
 * Petdoc
 * Class: PetHomeAdapter
 * Created by kimjoonsung on 2020/04/17.
 *
 * Description : 펫 홈 어댑터
 */
class MagazineAdapter : RecyclerView.Adapter<MagazineAdapter.MagazineHolder>() {

    private var mItems:MutableList<MagazineItem> = mutableListOf()
    private var mListener: AdapterListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        MagazineHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_magazine_item, parent, false))

    override fun onBindViewHolder(holder: MagazineHolder, position: Int) {
        mItems[position].let { item ->
            holder.itemView.likeCnt.text = item.likeCount.toString()
            holder.itemView.category.text = item.categoryNm
            holder.itemView.magazineTitle.text = item.title

            val imageUrl:String
            if (item.titleImage.startsWith("https://")
                || item.titleImage.startsWith("http://")) {
                imageUrl = item.titleImage
            } else {
                imageUrl = "${AppConstants.IMAGE_URL}${item.titleImage}"
            }

            Glide.with(holder.itemView.context)
                .load(imageUrl)
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                .into(holder.itemView.magazineImg)


            holder.itemView.setOnClickListener {
                mListener?.onMagazineItemClicked(item)
            }
        }
    }

    override fun getItemCount() = mItems.size

    fun setData(items: MutableList<MagazineItem>) {
        this.mItems.clear()
        this.mItems.addAll(items)
        notifyDataSetChanged()
    }

    fun addData(items: MutableList<MagazineItem>) {
        if(items.isNotEmpty()) {
            for(i in 0 until items.size) {
                this.mItems.add(items[i])
            }

            notifyItemInserted(mItems.size - items.size)
        }
    }

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    inner class MagazineHolder(itemView:View) : RecyclerView.ViewHolder(itemView)

    interface AdapterListener {
        fun onMagazineItemClicked(item:MagazineItem)
    }
}