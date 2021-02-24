package kr.co.petdoc.petdoc.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_magazine_detail.*
import kotlinx.android.synthetic.main.adapter_exisiting_magazine_item.view.*
import kotlinx.android.synthetic.main.adapter_exisiting_magazine_item.view.category
import kotlinx.android.synthetic.main.adapter_exisiting_magazine_item.view.likeCnt
import kotlinx.android.synthetic.main.adapter_exisiting_magazine_item.view.magazineImg
import kotlinx.android.synthetic.main.adapter_magazine_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.chat.ChatHomeActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.MagazineDetailResponse
import kr.co.petdoc.petdoc.network.response.MagazineListRespone
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.EncyContent
import kr.co.petdoc.petdoc.network.response.submodel.MagazineDetailItem
import kr.co.petdoc.petdoc.network.response.submodel.MagazineItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.*
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: MagazineDetailActivity
 * Created by kimjoonsung on 2020/06/04.
 *
 * Description :
 */
class MagazineDetailActivity : BaseActivity() {

    companion object {
        private const val IMAGE_TITLE_CONTENT = "image_title_content"
        private const val IMAGE_CONTENT = "image_content"
        private const val IMAGE = "image"
        private const val CONTENT = "content"
        private const val QUESTION = "question"
        private const val TITLE_CONTENT = "title_content"
        private const val VIDEO = "video"
    }

    private val TAG = "MagazineDetailActivity"
    private val MAGAZINE_DETAIL_REQUEST = "$TAG.magazineDetailRequest"
    private val MAGAZINE_SUB_LIST_REQUEST = "$TAG.magazineSubListRequest"
    private val MAGAZINE_LIKE_BOOKMARK_COUNT_REQUEST = "$TAG.magazineLikeAndBookmarkCountRequest"
    private val MAGAZINE_LIKE_BOOKMARK_STATUS_REQUEST = "$TAG.magazineLikeAndBookmarkStatusRequest"
    private val MAGAZINE_LIKE_REQUEST = "$TAG.magazineLikeRequest"
    private val MAGAZINE_LIKE_CANCEL_REQUEST = "$TAG.magazineLikeCancelRequest"
    private val MAGAZINE_BOOKMARK_REQUEST = "$TAG.magazineBookmarkRequest"
    private val MAGAZINE_BOOKMARK_CANCEL_REQUEST = "$TAG.magazineBookmarkCancelRequest"

    private lateinit var magazineAdapter:MagazineAdapter
    private var magazineItems:MutableList<MagazineItem> = mutableListOf()

    private var margin20 = 0
    private var margin7 = 0
    private var margin10 = 0

    private var magazineId = -1
    private var userId = -1
    private var shareUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_magazine_detail)
        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        magazineId = intent?.getIntExtra("id", magazineId)!!
        userId = if(StorageUtils.loadValueFromPreference(this, AppConstants.PREF_KEY_USER_ID, "").isNotEmpty()) {
            StorageUtils.loadValueFromPreference(this, AppConstants.PREF_KEY_USER_ID, "").toInt()
        } else {
            0
        }

        Logger.d("magazineId : $magazineId, userId : $userId")

        margin10 = Helper.convertDpToPx(this, 10f).toInt()
        margin20 = Helper.convertDpToPx(this, 20f).toInt()
        margin7 = Helper.convertDpToPx(this, 7f).toInt()

        btnClose.setOnClickListener {
            finish()
        }

        banner.setOnClickListener {
            if (StorageUtils.loadBooleanValueFromPreference(this, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                startActivity(Intent(this, ChatHomeActivity::class.java))
            } else {
                startActivity(Intent(this, LoginAndRegisterActivity::class.java))
            }
        }

        btnLike.setOnClickListener {
            if (StorageUtils.loadBooleanValueFromPreference(this, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                if (!it.isSelected) {
                    mApiClient.setMagazineLike(MAGAZINE_LIKE_REQUEST, magazineId)
                } else {
                    mApiClient.cancelMagazineLike(MAGAZINE_LIKE_CANCEL_REQUEST, magazineId)
                }
            } else {
                startActivity(Intent(this, LoginAndRegisterActivity::class.java))
            }
        }

        btnBookmark.setOnClickListener {
            if (StorageUtils.loadBooleanValueFromPreference(this, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                if (!it.isSelected) {
                    mApiClient.setMagazineBookmark(MAGAZINE_BOOKMARK_REQUEST, magazineId)
                } else {
                    mApiClient.cancelMagazineBookmark(MAGAZINE_BOOKMARK_CANCEL_REQUEST, magazineId)
                }
            } else {
                startActivity(Intent(this, LoginAndRegisterActivity::class.java))
            }
        }

        btnShare.setOnClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareUrl)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        btnTop.setOnClickListener { scrollView.scrollTo(0, 0) }
        //==========================================================================================
        magazineAdapter = MagazineAdapter()
        magazineList.apply {
            layoutManager = LinearLayoutManager(this@MagazineDetailActivity).apply {
                orientation = LinearLayoutManager.HORIZONTAL }
            adapter = magazineAdapter
        }
    }

    override fun onResume() {
        super.onResume()
        retryIfNetworkAbsent {
            mApiClient.getMagazineDetail(MAGAZINE_DETAIL_REQUEST, magazineId)
            mApiClient.getMagazineDetailSubList(MAGAZINE_SUB_LIST_REQUEST, magazineId)
            mApiClient.getMagazineLikeAndBookmarkCount(MAGAZINE_LIKE_BOOKMARK_COUNT_REQUEST, magazineId)
            mApiClient.getMagazineLikeAndBookmarkStatus(MAGAZINE_LIKE_BOOKMARK_STATUS_REQUEST, magazineId)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (mApiClient.isRequestRunning(MAGAZINE_SUB_LIST_REQUEST)) {
            mApiClient.cancelRequest(MAGAZINE_SUB_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(MAGAZINE_DETAIL_REQUEST)) {
            mApiClient.cancelRequest(MAGAZINE_DETAIL_REQUEST)
        }

        if (mApiClient.isRequestRunning(MAGAZINE_LIKE_BOOKMARK_COUNT_REQUEST)) {
            mApiClient.cancelRequest(MAGAZINE_LIKE_BOOKMARK_COUNT_REQUEST)
        }

        if (mApiClient.isRequestRunning(MAGAZINE_LIKE_REQUEST)) {
            mApiClient.cancelRequest(MAGAZINE_LIKE_REQUEST)
        }

        if (mApiClient.isRequestRunning(MAGAZINE_LIKE_CANCEL_REQUEST)) {
            mApiClient.cancelRequest(MAGAZINE_LIKE_CANCEL_REQUEST)
        }

        if (mApiClient.isRequestRunning(MAGAZINE_BOOKMARK_REQUEST)) {
            mApiClient.cancelRequest(MAGAZINE_BOOKMARK_REQUEST)
        }

        if (mApiClient.isRequestRunning(MAGAZINE_BOOKMARK_CANCEL_REQUEST)) {
            mApiClient.cancelRequest(MAGAZINE_BOOKMARK_CANCEL_REQUEST)
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
    fun onEventMainThread(data: NetworkBusResponse) {
        when(data.tag) {
            MAGAZINE_LIKE_BOOKMARK_COUNT_REQUEST -> {
                if (data.status == "ok") {
                    try {
                        val code = JSONObject(data.response)["code"]
                        val resultData = JSONObject(data.response)["resultData"] as JSONObject
                        if ("SUCCESS" == code) {
                            likeCount.text = resultData["likeCount"].toString()
                            bookmarkCount.text = resultData["bookmarkCount"].toString()
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
            }

            MAGAZINE_LIKE_BOOKMARK_STATUS_REQUEST -> {
                if (data.status == "ok") {
                    btnLike.isSelected = JSONObject(data.response)["addedLikeUser"] as Boolean
                    btnBookmark.isSelected = JSONObject(data.response)["addedBookmarkUser"] as Boolean
                }
            }

            MAGAZINE_LIKE_REQUEST -> {
                if (data.status == "ok") {
                    btnLike.isSelected = true
                } else {
                    Logger.d("error : ${data.code}, ${data.response}")
                }
            }

            MAGAZINE_LIKE_CANCEL_REQUEST -> {
                if (data.status == "ok") {
                    btnLike.isSelected = false
                } else {
                    Logger.d("error : ${data.code}, ${data.response}")
                }
            }

            MAGAZINE_BOOKMARK_REQUEST -> {
                if (data.status == "ok") {
                    btnBookmark.isSelected = true
                } else {
                    Logger.d("error : ${data.code}, ${data.response}")
                }
            }

            MAGAZINE_BOOKMARK_CANCEL_REQUEST -> {
                if (data.status == "ok") {
                    btnBookmark.isSelected = false
                } else {
                    Logger.d("error : ${data.code}, ${data.response}")
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            MAGAZINE_SUB_LIST_REQUEST -> {
                if (response is MagazineListRespone) {
                    if (response.resultData.size > 0) {
                        magazineSubList.visibility = View.VISIBLE
                        magazineItems = response.resultData
                        magazineAdapter.notifyDataSetChanged()
                    } else {
                        magazineSubList.visibility = View.GONE
                    }
                } else {
                    Logger.d("error : ${response.messageKey}")
                }
            }

            MAGAZINE_DETAIL_REQUEST -> {
                if (response is MagazineDetailResponse) {
                    magazineTitle.text = response.resultData.title
                    magazineBigTitle.text = response.resultData.title
                    shareUrl = response.resultData.webUrl

                    val imgUrl = response.resultData.titleImage
                    Glide.with(this)
                            .load(if(imgUrl.startsWith("http")) imgUrl else "${AppConstants.IMAGE_URL}${imgUrl}")
                            .into(titleImg)

                    setMagazineContent(response.resultData.encyContents)
                } else {
                    Logger.d("error : ${response.messageKey}")
                }
            }
        }
    }

    private fun setMagazineContent(contents:List<EncyContent>) {
        magazineContainer.removeAllViews()

        when (contents[0].contentType) {
            IMAGE_CONTENT -> {
                for (i in 0 until contents.size) {
                    magazineContainer.addView(
                        MagazineImageContent(this, contents[i].image, contents[i].content)
                    )
                }
            }

            IMAGE_TITLE_CONTENT -> {
                for (i in 0 until contents.size) {
                    magazineContainer.addView(
                        MagazineImageTitleContent(this, contents[i].title, contents[i].content, contents[i].image)
                    )
                }
            }

            IMAGE -> {
                for (i in 0 until contents.size) {
                    magazineContainer.addView(
                        MagazineImage(this, contents[i].image)
                    )
                }
            }

            CONTENT -> {
                for (i in 0 until contents.size) {
                    magazineContainer.addView(
                        MagazineTitle(this, contents[i].title)
                    )
                }
            }

            QUESTION -> {
                for (i in 0 until contents.size) {
                    magazineContainer.addView(
                        MagazineQuestion(this, contents[i].title)
                    )
                }
            }

            TITLE_CONTENT -> {
                for (i in 0 until contents.size) {
                    magazineContainer.addView(
                        MagazineTitleContent(this, contents[i].title, contents[i].content)
                    )
                }
            }

            VIDEO -> { }
        }
    }

    private fun onMagazineClicked(item: MagazineItem) {
        startActivity(Intent(this, MagazineDetailActivity::class.java).apply {
            putExtra("id", item.id)
        })
    }

    //===============================================================================================
    inner class MagazineAdapter : RecyclerView.Adapter<MagazineHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MagazineHolder(layoutInflater.inflate(R.layout.adapter_exisiting_magazine_item, parent, false))

        override fun onBindViewHolder(holder: MagazineHolder, position: Int) {
            if (position == 0) {
                (holder.itemView.root_view.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin20
                    rightMargin = 0
                }
            } else if (position == itemCount - 1) {
                (holder.itemView.root_view.layoutParams as RecyclerView.LayoutParams).apply {
                    rightMargin = margin20
                    leftMargin = margin7
                }
            } else {
                (holder.itemView.root_view.layoutParams as RecyclerView.LayoutParams).apply {
                    leftMargin = margin7
                    rightMargin = 0
                }
            }

            holder.setImage(magazineItems[position].titleImage)
            holder.setCategory(magazineItems[position].categoryNm)
            holder.setTitle(magazineItems[position].title)
            holder.setLikeCount(magazineItems[position].likeCount.toString())

            holder.itemView.setOnClickListener {
                onMagazineClicked(magazineItems[position])
            }
        }

        override fun getItemCount() = magazineItems.size
    }

    inner class MagazineHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setImage(url: String) {
            Glide.with(this@MagazineDetailActivity)
                .load( if(url.startsWith("http")) url else "${AppConstants.IMAGE_URL}$url")
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                .into(item.magazineImg)
        }

        fun setCategory(_category: String) {
            item.category.text = _category
        }

        fun setTitle(_title: String) {
            item.title.text = _title
        }

        fun setLikeCount(_count: String) {
            item.likeCnt.text = _count
        }
    }
}