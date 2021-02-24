package kr.co.petdoc.petdoc.activity.chat

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.style.ForegroundColorSpan
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_chat_quit.*
import kotlinx.android.synthetic.main.adapter_recommed_product_item.view.*
import kotlinx.android.synthetic.main.adapter_related_hospital_item.view.*
import kotlinx.android.synthetic.main.view_hospital_service.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.adapter.chat.MyChatAdapter
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.fragment.chat.ChatRatingDialogFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.ChatDetailResponse
import kr.co.petdoc.petdoc.network.response.RecommendProductListResponse
import kr.co.petdoc.petdoc.network.response.RelatedHospitalListResponse
import kr.co.petdoc.petdoc.network.response.submodel.RecommendProductItem
import kr.co.petdoc.petdoc.network.response.submodel.RelatedHospitalItem
import kr.co.petdoc.petdoc.utils.AES256Cipher
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: ChatQuitActivity
 * Created by kimjoonsung on 2020/05/26.
 *
 * Description :
 */
class ChatQuitActivity : BaseActivity() {

    private val LOGTAG = "ChatQuitActivity"
    private val PRODUCT_LIST_REQUEST = "${LOGTAG}.productListRequest"
    private val HOSPITAL_LIST_REQUEST = "${LOGTAG}.hospitalListRequest"
    private val MESSAGE_LIST_REQUEST = "${LOGTAG}.messageListRequest"

    private lateinit var productAdapter:ProductAdapter
    private var productItems:MutableList<RecommendProductItem> = mutableListOf()

    private lateinit var hospitalAdapter:HospitalAdapter
    private var relatedHospitalItems:MutableList<RelatedHospitalItem> = mutableListOf()

    private lateinit var messageAdapterMy:MyChatAdapter

    private var margin16 = 0
    private var margin7 = 0
    private var margin14 = 0

    private var chatPk = -1
    private var shareUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_quit)
        root.setPadding(0, Helper.getStatusBarHeight(this), 0, 0)

        FirebaseAPI(this).logEventFirebase("상담_기존정보_상세페이지", "Page View", "상담 기존정보 확인 상세페이지")

        margin14 = Helper.convertDpToPx(this, 14f).toInt()
        margin16 = Helper.convertDpToPx(this, 16f).toInt()
        margin7 = Helper.convertDpToPx(this, 7f).toInt()

        chatPk = intent?.getIntExtra("chatPk", chatPk)!!
        Logger.d("chatPk : $chatPk")

        //========================================================================================
        btnClose.setOnClickListener {
            finish()
        }

        btnTop.setOnClickListener { scrollView.scrollTo(0, 0) }

        btnShare.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareUrl)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        //========================================================================================
        chatList.apply {
            layoutManager = LinearLayoutManager(this@ChatQuitActivity)
        }

        productAdapter = ProductAdapter()
        recommedProdutList.apply {
            layoutManager = LinearLayoutManager(this@ChatQuitActivity).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }

            adapter = productAdapter
        }

        hospitalAdapter = HospitalAdapter()
        hospitalList.apply {
            layoutManager = LinearLayoutManager(this@ChatQuitActivity).apply {
                orientation = LinearLayoutManager.HORIZONTAL
            }

            adapter = hospitalAdapter
        }

        messageAdapterMy = MyChatAdapter(this)
        chatList.apply {
            layoutManager = LinearLayoutManager(this@ChatQuitActivity)
            adapter = messageAdapterMy
        }

        //========================================================================================

        loadData()
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mApiClient.isRequestRunning(PRODUCT_LIST_REQUEST)) {
            mApiClient.cancelRequest(PRODUCT_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(HOSPITAL_LIST_REQUEST)) {
            mApiClient.cancelRequest(HOSPITAL_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(MESSAGE_LIST_REQUEST)) {
            mApiClient.cancelRequest(MESSAGE_LIST_REQUEST)
        }
    }

    override fun onBackPressed() {
        finish()
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================

    /**
     * Response of request.
     *
     * @param response response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            PRODUCT_LIST_REQUEST -> {
                if (response is RecommendProductListResponse) {
                    productItems.addAll(response.recommendProductList)
                    productAdapter.notifyDataSetChanged()
                }
            }

            HOSPITAL_LIST_REQUEST -> {
                if (response is RelatedHospitalListResponse) {
                    relatedHospitalItems.addAll(response.relatedHospitalList)
                    hospitalAdapter.notifyDataSetChanged()
                }
            }

            MESSAGE_LIST_REQUEST -> {
                if (response is ChatDetailResponse) {
                    messageAdapterMy.setItems(response.chatDetail.messages.reversed())
                    shareUrl = response.chatDetail.webUrl

                    petSpecies.text = response.chatDetail.pet.species
                    petBirth.text = response.chatDetail.pet.birthDay.replace("-", "").let {
                        it.substring(2)
                    }
                    petGender.apply {
                        when {
                            response.chatDetail.pet.gender == "man" -> text = "남아"
                            else -> text = "여아"
                        }
                    }

                    chatResultDetail.apply {
                        when {
                            response.chatDetail.counselTag.size > 0 -> {
                                text = response.chatDetail.counselTag.toString()
                            }

                            else -> text = "미지정"
                        }
                    }

                    if (response.chatDetail.reviews == 0) {
                        ChatRatingDialogFragment().apply {
                            val bundle = Bundle().apply {
                                putInt("roomId", chatPk)
                                putString("displayName", response.chatDetail.partner?.displayName)
                            }
                            arguments = bundle
                        }.show(supportFragmentManager, "ChatRating")
                    }
                }
            }
        }
    }

    private fun loadData() {
        mApiClient.getRecommedProductList(PRODUCT_LIST_REQUEST)
        mApiClient.getHospitalList(HOSPITAL_LIST_REQUEST, 1, 10)
        mApiClient.getChatDetail(MESSAGE_LIST_REQUEST, chatPk)
    }

    private fun onHospitalItemClicked(itemRelated: RelatedHospitalItem) {
        startActivity(Intent(this, RelatedHospitalActivity::class.java).apply {
            putExtra("item", itemRelated)
        })
    }

    private fun onProductItemCliked(item: RecommendProductItem) {
        if(StorageUtils.loadBooleanValueFromPreference(this, AppConstants.PREF_KEY_LOGIN_STATUS)) {
            val cipherStr = getAESCipherStr()
            if (item.code.isNotEmpty()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/member/signin.php?renderurl=/goods/goods_view.php?goodsNo=${item.code}&referer=true&jsonbody=${cipherStr}")))
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/member/signin.php?&referer=true&jsonbody=${cipherStr}")))
            }
        } else {
            if (item.code.isNotEmpty()) {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/goods/goods_view.php?goodsNo=${item.code}")))
            } else {
                startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("http://vlab.kr/")))
            }
        }
    }

    private fun getAESCipherStr(): String {
        val json = JSONObject().apply {
            put("user_id", StorageUtils.loadValueFromPreference(this@ChatQuitActivity, AppConstants.PREF_KEY_ID_GODO, ""))
            put("name", StorageUtils.loadValueFromPreference(this@ChatQuitActivity, AppConstants.PREF_KEY_USER_NAME, ""))
        }

        return AES256Cipher.encryptURL(json.toString())
    }

    //=============================================================================================
    inner class ProductAdapter : RecyclerView.Adapter<ProductHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ProductHolder(layoutInflater.inflate(R.layout.adapter_recommed_product_item, parent, false))

        override fun onBindViewHolder(holder: ProductHolder, position: Int) {
            if (position == 0) {
                (holder.itemView.layoutRoot.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin16
                    rightMargin = 0
                }
            } else if (position == itemCount - 1) {
                (holder.itemView.layoutRoot.layoutParams as RecyclerView.LayoutParams).apply {
                    rightMargin = margin16
                    leftMargin = margin7
                }
            } else {
                (holder.itemView.layoutRoot.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin7
                    rightMargin = 0
                }
            }

            holder.setImage(productItems[position].squalImageUrl)
            holder.setTitle(productItems[position].title)

            holder.itemView.setOnClickListener { onProductItemCliked(productItems[position]) }
        }

        override fun getItemCount() = productItems.size
    }

    inner class ProductHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setImage(url: String) {
            Glide.with(this@ChatQuitActivity)
                .load( if(url.startsWith("http")) url else "${AppConstants.IMAGE_URL}$url")
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                .into(item.productImg)
        }

        fun setTitle(_title: String) {
            item.title.text = _title
        }
    }

    //==============================================================================================
    inner class HospitalAdapter : RecyclerView.Adapter<HospitalHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            HospitalHolder(layoutInflater.inflate(R.layout.adapter_related_hospital_item, parent, false))

        override fun onBindViewHolder(holder: HospitalHolder, position: Int) {
            if (position == 0) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin16
                    rightMargin = 0
                }
            } else if (position == itemCount - 1) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    rightMargin = margin16
                    leftMargin = margin14
                }
            } else {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin14
                    rightMargin = 0
                }
            }

            holder.setName(relatedHospitalItems[position].name!!)

            val kilometer = relatedHospitalItems[position].distance / 1000
            val distnce = String.format("%.1f km", kilometer)
            val info = "${distnce} ⎪ ${relatedHospitalItems[position].address}"
            val builder = SpannableStringBuilder(info).apply {
                setSpan(ForegroundColorSpan(Helper.readColorRes(R.color.orange)), 0, distnce.length, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)
            }

            holder.setInfo(builder)

            if (relatedHospitalItems[position].addition!!.size > 0) {
                holder.itemView.hospitalService.removeAllViews()
                holder.setService(relatedHospitalItems[position].addition!!)
            }

            holder.itemView.setOnClickListener { onHospitalItemClicked(relatedHospitalItems[position]) }
        }

        override fun getItemCount() = relatedHospitalItems.size
    }

    inner class HospitalHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setName(_name: String) {
            item.name.text = _name
        }

        fun setInfo(_info: SpannableStringBuilder) {
            item.hospitalInfo.text = _info
        }

        fun setService(service: List<String>) {
            for (i in 0 until service.size) {
                item.hospitalService.addView(
                    HospitalService(
                        item.context,
                        service[i]
                    )
                )
            }
        }
    }

    inner class HospitalService(context: Context, service:String) : FrameLayout(context) {

        init {
            val view = layoutInflater.inflate(R.layout.view_hospital_service, null, false)

            view.service.text = service

            addView(view)
        }
    }
}