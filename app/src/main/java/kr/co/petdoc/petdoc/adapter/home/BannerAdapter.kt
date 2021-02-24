package kr.co.petdoc.petdoc.adapter.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.banner_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.response.submodel.BannerItem
import kr.co.petdoc.petdoc.viewmodel.Interaction

/**
 * Petdoc
 * Class: BannerAdapter
 * Created by kimjoonsung on 2020/04/17.
 *
 * Description : 홈 배너 어댑터
 */
class BannerAdapter(private val interaction: Interaction) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private var bannerItemList:MutableList<BannerItem> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BannerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.banner_item, parent, false),
            interaction
        )

    override fun getItemCount() = Integer.MAX_VALUE

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        bannerItemList.let {
            if (it.size > 0) {
                (holder as BannerViewHolder).bind(it[position % it.size])
            }
        }
    }

    fun setItems(items: List<BannerItem>) {
        bannerItemList.clear()
        bannerItemList.addAll(items)
        notifyDataSetChanged()
    }

    class BannerViewHolder(itemView: View, private val interaction: Interaction) :
        RecyclerView.ViewHolder(itemView) {

        fun bind(bannerItem: BannerItem) {
            itemView.setOnClickListener {
                interaction.onBannerItemClicked(bannerItem)
            }

            Glide.with(itemView.context)
                 .load(if(bannerItem.bannerImageUrl.startsWith("http")) bannerItem.bannerImageUrl else "${AppConstants.IMAGE_URL}${bannerItem.bannerImageUrl}")
                 .apply(RequestOptions().transform(RoundedCorners(30)))
                 .into(itemView.bannerImg)
        }
    }
}