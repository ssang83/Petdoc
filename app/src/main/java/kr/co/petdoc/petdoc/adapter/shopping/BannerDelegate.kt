package kr.co.petdoc.petdoc.adapter.shopping

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.shop_list_delegate_item_banner.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.PagingAdapterDelegate
import kr.co.petdoc.petdoc.extensions.autoScroll
import kr.co.petdoc.petdoc.model.PagingItem
import kr.co.petdoc.petdoc.model.ShopBannerUIModel
import kr.co.petdoc.petdoc.network.response.submodel.BannerItem
import kr.co.petdoc.petdoc.utils.Helper

class BannerDelegate : PagingAdapterDelegate<PagingItem> {
    override fun isForViewType(item: PagingItem) =
        item is ShopBannerUIModel

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return BannerContainerViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.shop_list_delegate_item_banner, parent, false
            )
        )
    }

    override fun onBindViewHolder(
        item: PagingItem,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: List<*>
    ) {
        val banners = (item as ShopBannerUIModel).items
        (holder as BannerContainerViewHolder).bind(banners)
    }

    inner class BannerContainerViewHolder(view: View): RecyclerView.ViewHolder(view) {
        fun bind(items: List<BannerItem>) {
            val context = itemView.context
            if (itemView.shopBannerList.adapter == null) {
                itemView.shopBannerList.apply {
                    var pageOffset = Helper.convertDPResourceToPx(context, R.dimen.life_top_col_side).toFloat()
                    var pageMargin = Helper.convertDPResourceToPx(context, R.dimen.life_top_col_advertisement_gap).toFloat()

                    adapter = ShoppingBannerAdapter(items)

                    clipToPadding = false
                    clipChildren = false
                    setPadding(Helper.convertDPResourceToPx(context, R.dimen.life_top_col_side),0,Helper.convertDPResourceToPx(context, R.dimen.life_top_col_side),0)

                    setPageTransformer { page, position ->
                        when {
                            position >= 1 -> page.translationX = pageOffset - pageMargin
                            position <= -1 -> page.translationX = pageMargin - pageOffset
                            position >=0 && position<1 -> page.translationX = (pageOffset - pageMargin) * position
                            else -> page.translationX = (pageMargin-pageOffset) * (-position)
                        }
                    }

                    shopBannerList.autoScroll(3000L)
                }
            }
        }
    }
}