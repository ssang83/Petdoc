package kr.co.petdoc.petdoc.activity

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Typeface
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_banner_detail.*
import kotlinx.android.synthetic.main.adapter_banner_comment_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.event.SoftKeyboardBus
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.BannerDetailResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.Comment
import kr.co.petdoc.petdoc.recyclerview.RecyclerViewSwipeDecorator
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.KeyboardHeightProvider
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.OnSingleClickListener
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.jsoup.Jsoup
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: BannerDetailActivity
 * Created by kimjoonsung on 2020/04/21.
 *
 * Description :
 */
class BannerDetailActivity : BaseActivity() {

    val DAY =  24 * 1000 * 60 * 60
    val HOUR = 1000 * 60 * 60
    val MIN = 1000 * 60
    val SEC = 1000

    private var yearFormat = SimpleDateFormat("yyyy", Locale.KOREA)

    private val BANNER_DETAIL_REQUEST_TAG = "bannerDetailRequest"
    private val BANNER_REPLY_ADD_REQUEST_TAG = "bannerReplyAddRequest"
    private val BANNER_REPLY_DELETE_REQUEST_TAG = "bannerReplyDeleteRequest"

    lateinit var mAdapter:CommentAdapter
    private var mCommentItems:MutableList<Comment> = mutableListOf()

    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null

    private var deletePosition = -1
    private var bannerId = -1
    private var myUserId = ""
    private var appLink = ""
    private var shareUrl = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_banner_detail)

        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        bannerId = intent.getIntExtra("bannerId", -1)

        btnSend.setOnSingleClickListener {
            if(StorageUtils.loadBooleanValueFromPreference(this@BannerDetailActivity, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                sendReply()
            }else{
                startActivity(Intent(this@BannerDetailActivity, LoginAndRegisterActivity::class.java))
            }
        }

        btnShare.setOnSingleClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareUrl)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        //==========================================================================================
        mAdapter = CommentAdapter()
        commentList.apply {
            layoutManager = LinearLayoutManager(this@BannerDetailActivity)
            adapter = mAdapter
        }

        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(commentList)

        btnClose.setOnSingleClickListener { onBackPressed() }

        htmlContent.apply {
            settings.apply {
                loadWithOverviewMode = true
                useWideViewPort = true
            }

            webViewClient = object : WebViewClient() {
                override fun shouldOverrideUrlLoading(view: WebView?, url: String?): Boolean {
                    Logger.d("url : $url")
                    if (url?.startsWith("http")!!) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } else {
                        EventBus.getDefault().post(appLink)
                    }
                    return true
                }

                override fun shouldOverrideUrlLoading(
                    view: WebView?,
                    request: WebResourceRequest?
                ): Boolean {
                    Logger.d("url : ${request?.url}")
                    val url = request?.url.toString()
                    if (url.startsWith("http")) {
                        startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(url)))
                    } else {
                        EventBus.getDefault().post(appLink)
                    }
                    return true
                }
            }

        }

        editComment.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! > 0) {
                    btnSend.isEnabled = true
                    btnSend.setBackgroundResource(R.drawable.ic_send_enable)
                } else {
                    btnSend.isEnabled = false
                    btnSend.setBackgroundResource(R.drawable.ic_send_disable)
                }
            }
        })

        myUserId = StorageUtils.loadValueFromPreference(this, AppConstants.PREF_KEY_OLD_USER_ID, "")
        //==========================================================================================
        mApiClient.getBanner(BANNER_DETAIL_REQUEST_TAG, bannerId)

        // 소프트 키보드가 변경 될 경우 Bus 로 data 가 온다.
        mKeyboardHeightProvider = KeyboardHeightProvider(this)
        Handler().postDelayed( { mKeyboardHeightProvider!!.start() }, 1000)
    }

    override fun onBackPressed() {
        finish()
    }

    override fun onDestroy() {
        if (mApiClient.isRequestRunning(BANNER_DETAIL_REQUEST_TAG)) {
            mApiClient.cancelRequest(BANNER_DETAIL_REQUEST_TAG)
        }

        if (mApiClient.isRequestRunning(BANNER_REPLY_ADD_REQUEST_TAG)) {
            mApiClient.cancelRequest(BANNER_REPLY_ADD_REQUEST_TAG)
        }

        if (mApiClient.isRequestRunning(BANNER_REPLY_DELETE_REQUEST_TAG)) {
            mApiClient.cancelRequest(BANNER_REPLY_DELETE_REQUEST_TAG)
        }

        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider!!.close()
        }
        super.onDestroy()
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
            BANNER_DETAIL_REQUEST_TAG -> {
                if (response is BannerDetailResponse) {
                    bannerTitle.text = response.bannerDetail.title
                    shareUrl = response.bannerDetail.url
                    commentList.apply {
                        when {
                            response.bannerDetail.comments.size == 0 -> {
                                visibility = View.GONE
                                btnShare.visibility = View.GONE
                            }

                            else -> {
                                visibility = View.VISIBLE
                                btnShare.visibility = View.VISIBLE
                                mCommentItems.clear()
                                mCommentItems.addAll(response.bannerDetail.comments)
                                mAdapter.notifyDataSetChanged()
                            }
                        }
                    }

                    val doc = Jsoup.parse(response.bannerDetail.content)
                    val link = doc.select("a").first()
                    if (link != null) {
                        appLink = link.attr("href")
                        Logger.d("appLink : ${appLink}")
                    }

                    val content =
                            "<!DOCTYPE html><html>" +
                                    "<head>" +
                                    "<meta name='viewport' content='width=100%, initial-scale=1.0, maximum-scale=1.0, minimum-scale=1.0'>" +
                                    "<meta charset='UTF-8'>" +
                                    "</head>" +
                                    "<body>${response.bannerDetail.content}</body>" +
                                    "<style type='text/css'>" +
                                    "body {" +
                                    "margin: 0;" +
                                    "padding: 0;" +
                                    "display: block;" +
                                    "}" +
                                    "img {" +
                                    "margin: 0;" +
                                    "padding: 0;" +
                                    "max-width: 100%;" +
                                    "max-height: 100%;" +
                                    "width: 100%!important;" +
                                    "height: 100%!important;" +
                                    "display: block;" +
                                    "}" +
                                    "</style>" +
                                    "</html>"

                    htmlContent.loadData(content, "text/html", "UTF-8")
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(data:NetworkBusResponse) {
        when(data.tag) {
            BANNER_REPLY_ADD_REQUEST_TAG -> {
                if ("ok" == data.status) {
                    AppToast(this).showToastMessage(
                        "댓글이 등록 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )

                    mApiClient.getBanner(BANNER_DETAIL_REQUEST_TAG, bannerId)
                } else {
                    AppToast(this).showToastMessage(
                        "댓글 등록이 실패하였습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                }
            }

            BANNER_REPLY_DELETE_REQUEST_TAG -> {
                if ("ok" == data.status) {
                    mAdapter.removeItem(deletePosition)

                    AppToast(this).showToastMessage(
                        "댓글이 삭제 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )

                    mApiClient.getBanner(BANNER_DETAIL_REQUEST_TAG, bannerId)
                } else {
                    AppToast(this).showToastMessage(
                        "댓글 삭제 실패하였습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )

                    mAdapter.notifyItemChanged(deletePosition)
                }
            }
        }
    }

    /**
     * 소프트 키보드가 올라오면 이벤트가 넘어온다.
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(bus: SoftKeyboardBus) {
        Logger.e("height:" + bus.height)

        // 키보드가 올라가면 리스트 제일 끝으로 스크롤
        if (bus.height > 100) {
            emptyView?.layoutParams?.height = bus.height
            emptyView?.invalidate()
            emptyView?.requestLayout()

            if(editComment.text.toString().isNotEmpty()) {
                btnSend.isEnabled = true
                btnSend.setBackgroundResource(R.drawable.ic_send_enable)
            }

        } else {
            emptyView?.layoutParams?.height = 1
            emptyView?.invalidate()
            emptyView?.requestLayout()

            btnSend.isEnabled = false
            btnSend.setBackgroundResource(R.drawable.ic_send_disable)
        }
    }

    private fun sendReply() {
        hideKeyboard(editComment)
        mApiClient.postReplyAdd(
            BANNER_REPLY_ADD_REQUEST_TAG,
            "banners",
            bannerId,
            editComment.text.toString()
        )

        editComment.setText("")
    }

    // ============================================================================================================
    inner class CommentAdapter : RecyclerView.Adapter<CommentHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            CommentHolder(layoutInflater.inflate(R.layout.adapter_banner_comment_item, parent, false))

        override fun onBindViewHolder(holder: CommentHolder, position: Int) {
            if (myUserId == mCommentItems[position].userId.toString()) {
                holder.itemView.tag = true
            } else {
                holder.itemView.tag = false
            }

            holder.setNickname(mCommentItems[position].userNickName)
            holder.setComment(mCommentItems[position].content)
            holder.setImage(mCommentItems[position].userImg)

            val date = mCommentItems[position].createdAt.split(".")
            val createDate = date[0].replace("T", " ")
            val tempDate = date[0].split("T")
            val currentYear = yearFormat.format(System.currentTimeMillis())
            val year = tempDate[0].split("-")
            val duration = calculateTime(createDate)
            if (currentYear == year[0]) {
                if (duration / DAY > 0) {
                    holder.setRegTime(calculateDate(tempDate[0]))
                } else if (duration / HOUR > 0) {
                    holder.setRegTime("${duration/HOUR}시간전")
                } else if (duration / MIN > 0) {
                    holder.setRegTime("${duration/MIN}분전")
                } else {
                    holder.setRegTime("${duration/SEC}초전")
                }
            } else {
                holder.setRegTime(calculateDateForYear(tempDate[0]))
            }
        }

        override fun getItemCount() = mCommentItems.size

        fun getItem(position: Int) = mCommentItems[position]

        fun removeItem(position: Int) {
            mCommentItems.removeAt(position)
            notifyItemRemoved(position)
        }

        private fun calculateTime(createdAt: String) : Long {
            val dateNow = Date(System.currentTimeMillis())
            val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
            val fromDate = dateFormat.parse(createdAt)

            return dateNow.time - fromDate.time
        }

        private fun calculateDate(date: String) : String {
            val regDate = date.split("-")
            return String.format("%d월%d일", regDate[1].toInt(), regDate[2].toInt())
        }

        private fun calculateDateForYear(date: String) : String {
            val regDate = date.split("-")
            return String.format("%d년%d월%d일", regDate[0].toInt(), regDate[1].toInt(), regDate[2].toInt())
        }
    }

    inner class CommentHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setNickname(_text: String) {
            item.nickName.text = _text
        }

        fun setRegTime(_text: String) {
            item.regTime.text = _text
        }

        fun setComment(_text: String) {
            item.comment.text = _text
        }

        fun setImage(url:String){
            if (url.isNotEmpty()) {
                Glide.with(this@BannerDetailActivity)
                    .load( if(url.startsWith("http")) url else "${AppConstants.IMAGE_URL}$url")
                    .apply(RequestOptions.circleCropTransform())
                    .into(item.profileImage)
            } else {
                item.profileImage.setBackgroundResource(R.drawable.profile_default)
            }
        }
    }

    //==============================================================================================
    private val callback =
        object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                return true
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                try {
                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            Logger.d("delete position : $position")
                            deletePosition = position
                            if (StorageUtils.loadBooleanValueFromPreference(this@BannerDetailActivity, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                                val item = mAdapter.getItem(position)
                                if (myUserId == item.userId.toString()) {
                                    mApiClient.deleteReply(
                                            BANNER_REPLY_DELETE_REQUEST_TAG,
                                            "banners",
                                            item.id
                                    )
                                } else {
                                    AppToast(this@BannerDetailActivity).showToastMessage(
                                            "본인이 작성한 댓글만 삭제 가능합니다.",
                                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                                            AppToast.GRAVITY_BOTTOM
                                    )
                                    mAdapter.notifyItemChanged(deletePosition)
                                }
                            } else {
                                startActivity(Intent(this@BannerDetailActivity, LoginAndRegisterActivity::class.java))
                            }
                        }
                    }
                } catch (e: Exception) {
                    Logger.p(e)
                }
            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                RecyclerViewSwipeDecorator.Builder(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
                    .addSwipeLeftBackgroundColor(Helper.readColorRes(R.color.orange))
                    .addSwipeLeftLabel("삭제")
                    .setSwipeLeftLabelColor(Helper.readColorRes(R.color.white))
                    .setSwipeLeftLabelTextSize(TypedValue.COMPLEX_UNIT_DIP, 17f)
                    .setSwipeLeftLabelTypeface(Typeface.DEFAULT_BOLD)
                    .addSwipeRightBackgroundColor(Helper.readColorRes(R.color.periwinkleBlueTwo))
                    .create()
                    .decorate()

                super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive)
            }
        }
}