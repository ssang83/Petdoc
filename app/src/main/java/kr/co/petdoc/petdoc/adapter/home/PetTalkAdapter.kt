package kr.co.petdoc.petdoc.adapter.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_home_pettalk_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.response.submodel.HomePetTalkData

/**
 * Petdoc
 * Class: PetTalkAdapter
 * Created by kimjoonsung on 2020/07/20.
 *
 * Description :
 */
class PetTalkAdapter : RecyclerView.Adapter<PetTalkAdapter.PetTalkHolder>() {

    private val petTalkItems: MutableList<HomePetTalkData> = mutableListOf()
    private var mListener:AdapterListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        PetTalkHolder(LayoutInflater.from(parent.context).inflate(R.layout.adapter_home_pettalk_item, parent, false))

    override fun onBindViewHolder(holder: PetTalkHolder, position: Int) {
        holder.setTitle(petTalkItems[position].title)
        holder.setNickname(petTalkItems[position].nickName)
        holder.setImage(petTalkItems[position].imageURL)

        holder.itemView.setOnClickListener {
            mListener?.onPetTalkItemClicked(petTalkItems[position])
        }
    }

    override fun getItemCount() = petTalkItems.size

    fun setData(items: MutableList<HomePetTalkData>) {
        this.petTalkItems.clear()
        this.petTalkItems.addAll(items)
        notifyDataSetChanged()
    }

    fun setListener(listener: AdapterListener) {
        this.mListener = listener
    }

    inner class PetTalkHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setNickname(_name: String) {
            item.nickName.text = _name
        }

        fun setTitle(_title: String) {
            item.petTalkTitle.text = _title
        }

        fun setImage(url: String) {
            Glide.with(item.context)
                .load(if(url.startsWith("http")) url else "${AppConstants.IMAGE_URL}$url")
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                .into(item.petTalkImg)
        }
    }

    interface AdapterListener {
        fun onPetTalkItemClicked(item:HomePetTalkData)
    }
}