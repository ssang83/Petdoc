package kr.co.petdoc.petdoc.adapter.shopping

import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.banner.view.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.network.response.submodel.BannerItem
import kr.co.petdoc.petdoc.utils.AES256Cipher
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import org.json.JSONObject

class ShoppingBannerAdapter(
    private val items: List<BannerItem>
) : RecyclerView.Adapter<ShoppingBannerAdapter.BannerViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
        BannerViewHolder(
            LayoutInflater.from(parent.context).inflate(R.layout.banner, parent, false)
        )

    override fun getItemCount() = Integer.MAX_VALUE

    override fun onBindViewHolder(holder: BannerViewHolder, position: Int) {
        holder.bind(items[position % items.size])
    }

    class BannerViewHolder(view: View) :
        RecyclerView.ViewHolder(view) {

        fun bind(bannerItem: BannerItem) {
            itemView.imgBanner.setOnSingleClickListener {
                val context = itemView.context
                if(StorageUtils.loadBooleanValueFromPreference(context, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                    val cipherStr = getAESCipherStr()
                    if (bannerItem.url.isNotEmpty()) {
                        if (bannerItem.url.startsWith("goods_list")) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://vlab.kr/goods/${bannerItem.url}&referer=true&jsonbody=${cipherStr}")))
                        } else {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/member/signin.php?renderurl=/goods/goods_view.php?goodsNo=${bannerItem.url}&referer=true&jsonbody=${cipherStr}")))
                        }
                    } else {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/member/signin.php?&referer=true&jsonbody=${cipherStr}")))
                    }
                } else {
                    if (bannerItem.url.isNotEmpty()) {
                        if(bannerItem.url.startsWith("goods_list")) {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://vlab.kr/goods/${bannerItem.url}&referer=true&jsonbody=")))
                        } else {
                            context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/goods/goods_view.php?goodsNo=${bannerItem.url}")))
                        }
                    } else {
                        context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/")))
                    }
                }
            }

            Glide.with(itemView.context)
                .load(if(bannerItem.bannerImageUrl.startsWith("http")) bannerItem.bannerImageUrl else "${AppConstants.IMAGE_URL}${bannerItem.bannerImageUrl}")
                .apply(RequestOptions().transform(RoundedCorners(30)))
                .into(itemView.imgBanner)
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