package kr.co.petdoc.petdoc.adapter.shopping

import android.content.Intent
import android.graphics.Paint
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import kotlinx.android.synthetic.main.adapter_product_item.view.originPrice
import kotlinx.android.synthetic.main.adapter_product_item.view.productDesc
import kotlinx.android.synthetic.main.adapter_product_item.view.productImg
import kotlinx.android.synthetic.main.adapter_product_item.view.productTitle
import kotlinx.android.synthetic.main.adapter_product_item.view.salePrice
import kotlinx.android.synthetic.main.adapter_product_item.view.saleRate
import kotlinx.android.synthetic.main.shop_list_delegate_item_godomall_product.view.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.PagingAdapterDelegate
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.domain.GodoMallProduct
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.model.ShopProductUIModel
import kr.co.petdoc.petdoc.model.PagingItem
import kr.co.petdoc.petdoc.utils.AES256Cipher
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import org.json.JSONObject

class GodoMallProductDelegate : PagingAdapterDelegate<PagingItem> {
    override fun isForViewType(item: PagingItem): Boolean = item is ShopProductUIModel

    override fun onCreateViewHolder(parent: ViewGroup): RecyclerView.ViewHolder {
        return ItemViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(R.layout.shop_list_delegate_item_godomall_product, parent, false)
        )
    }

    override fun onBindViewHolder(
        item: PagingItem,
        position: Int,
        holder: RecyclerView.ViewHolder,
        payloads: List<*>
    ) {
        (holder as ItemViewHolder).bind(item as ShopProductUIModel)
    }

    class ItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var item: GodoMallProduct? = null
        init {
            itemView.setOnClickListener {
                item?.let { onProductClicked(it) }
            }
        }

        fun bind(model: ShopProductUIModel) {
            this.item = model.item
            val item = model.item
            with(itemView) {
                if (model.isFirstPosition) {
                    tvRecommendedProduct.visibility = View.VISIBLE
                } else {
                    tvRecommendedProduct.visibility = View.GONE
                }
                productTitle.text = item.title
                productDesc.text = item.addition
                salePrice.text = "${Helper.ToPriceFormat(item.originCost)}원"
                originPrice.text = "${Helper.ToPriceFormat(item.cost)}원"
                saleRate.text = "${item.sale}%"
                if (item.sale == 0) {
                    saleRate.visibility = View.GONE
                    originPrice.visibility = View.GONE
                } else {
                    saleRate.visibility = View.VISIBLE
                    originPrice.visibility = View.VISIBLE
                    originPrice.paintFlags = originPrice.paintFlags or Paint.STRIKE_THRU_TEXT_FLAG
                }

                val imgUrl = item.wideImageUrl
                Glide.with(itemView.context)
                    .load(if(imgUrl.startsWith("http")) {
                        imgUrl
                    } else {
                        "${AppConstants.IMAGE_URL}$imgUrl"
                    })
                    .into(productImg)
            }
        }

        private fun onProductClicked(item: GodoMallProduct) {
            val context = itemView.context
            Airbridge.trackEvent("shopping", "click", "recommend", null, null, null)
            if(StorageUtils.loadBooleanValueFromPreference(context, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                val cipherStr = getAESCipherStr()
                Logger.d("shopping url : ${"http://vlab.kr/member/signin.php?renderurl=/goods/goods_view.php?goodsNo=${item.code}&referer=true&jsonbody=${cipherStr}"}")
                if (item.code.toString().isNotEmpty()) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/member/signin.php?renderurl=/goods/goods_view.php?goodsNo=${item.code}&referer=true&jsonbody=${cipherStr}")))
                } else {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/member/signin.php?&referer=true&jsonbody=${cipherStr}")))
                }
            } else {
                if (item.code.toString().isNotEmpty()) {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/goods/goods_view.php?goodsNo=${item.code}")))
                } else {
                    context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/")))
                }
            }
        }

        private fun getAESCipherStr(): String {
            val json = JSONObject().apply {
                put("user_id", StorageUtils.loadValueFromPreference(itemView.context, AppConstants.PREF_KEY_ID_GODO, ""))
                put("name", PetdocApplication.mUserInfo?.name)
            }

            return AES256Cipher.encryptURL(json.toString())
        }
    }
}