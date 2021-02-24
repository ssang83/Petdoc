package kr.co.petdoc.petdoc.fragment.pettalk

import android.app.Activity
import android.content.Intent
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.NestedScrollView
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_pettalk.*
import kotlinx.android.synthetic.main.adapter_banner_comment_item.view.*
import kotlinx.android.synthetic.main.fragment_pettalk_detail.*
import kotlinx.android.synthetic.main.pettalk_image_item.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.PetTalkActivity
import kr.co.petdoc.petdoc.activity.life.PetTalkUploadActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.event.SoftKeyboardBus
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.PetTalk
import kr.co.petdoc.petdoc.network.response.submodel.PetTalkDetailItem
import kr.co.petdoc.petdoc.network.response.submodel.PetTalkImage
import kr.co.petdoc.petdoc.network.response.submodel.PetTalkReplyItem
import kr.co.petdoc.petdoc.recyclerview.RecyclerViewSwipeDecorator
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.KeyboardHeightProvider
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.OnSingleClickListener
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

class PetTalkDetailFragment : BaseFragment(){

    val DAY =  24 * 1000 * 60 * 60
    val HOUR = 1000 * 60 * 60
    val MIN = 1000 * 60
    val SEC = 1000

    private var yearFormat = SimpleDateFormat("yyyy", Locale.KOREA)

    private val LOGTAG = "PetTalkDetailFragment"
    private val PETTALK_DETAIL_REQUEST = "${LOGTAG}.petTalkDetailRequest"
    private val PETTALK_VIEWCOUNT_REQUET = "${LOGTAG}.petTalkViewCountRequest"
    private val PETTALK_REPLY_LIST_REQUEST = "${LOGTAG}.petTalkReplyListRequest"
    private val PETTALK_REPLY_PAGING_LIST_REQUEST = "${LOGTAG}.petTalkReplyPagingListRequest"
    private val PETTALK_REPLY_ADD_REQUEST = "${LOGTAG}.petTalkReplyAddRequest"
    private val PETTALK_REPLY_DELETE_REQUEST = "${LOGTAG}.petTalkReplyDelRequest"

    private val REQUEST_PETTALK_DELETE = 0x100

    private lateinit var replyAdapter:ReplyAdapter

    private lateinit var petTalkImageAdapter:PetTalkImageAdapter
    private var petTalkImageList:MutableList<PetTalkImage> = mutableListOf()

    private var replyItem:MutableList<PetTalkReplyItem> = mutableListOf()

    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null

    private var petTalkId = -1
    private var deletePosition = -1
    private var myUserId = ""
    private var baseId = -1
    private var shareUrl = ""

    override fun onCreateView( inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_pettalk_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        petTalkId = arguments?.getInt("petTalkId") ?: petTalkId

        btnBack.setOnSingleClickListener { requireActivity().onBackPressed() }
        btnShare.setOnSingleClickListener {
            val sendIntent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, shareUrl)
                type = "text/plain"
            }

            val shareIntent = Intent.createChooser(sendIntent, null)
            startActivity(shareIntent)
        }

        btnSend.setOnSingleClickListener {
            if (StorageUtils.loadBooleanValueFromPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_LOGIN_STATUS
                )
            ) {
                sendReply()
            } else {
                startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
            }
        }

        btnEdit.setOnSingleClickListener {
            if (StorageUtils.loadBooleanValueFromPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_LOGIN_STATUS
                )
            ) {
                startActivityForResult(Intent(requireActivity(), PetTalkUploadActivity::class.java).apply {
                    putExtra("petTalkId", petTalkId)
                }, REQUEST_PETTALK_DELETE)
            } else {
                startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
            }
        }

        //==========================================================================================
        scrollView.setOnScrollChangeListener { v: NestedScrollView, scrollX: Int, scrollY: Int, oldScrollX: Int, oldScrollY: Int ->
            if (scrollY == (v.getChildAt(0).measuredHeight - v.measuredHeight)) {
                Logger.d("bottom scroll")
                if (baseId != -1) {
                    mApiClient.getPetTalkReplyPagingList(PETTALK_REPLY_PAGING_LIST_REQUEST, petTalkId, baseId)
                }
            }
        }

        replyAdapter = ReplyAdapter()
        replyList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = replyAdapter
        }

        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(replyList)

        petTalkImageAdapter = PetTalkImageAdapter()
        viewPager.apply {
            adapter = petTalkImageAdapter
            orientation = ViewPager2.ORIENTATION_HORIZONTAL
        }

        TabLayoutMediator(indicator, viewPager) {tab, position ->  }.attach()

        editReply.addTextChangedListener(object : TextWatcher {
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

        myUserId = StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_OLD_USER_ID, "")
        //==========================================================================================

        // 소프트 키보드가 변경 될 경우 Bus 로 data 가 온다.
        mKeyboardHeightProvider = KeyboardHeightProvider(requireActivity())
        Handler().postDelayed( { mKeyboardHeightProvider!!.start() }, 1000)
    }

    override fun onResume() {
        super.onResume()
        retryIfNetworkAbsent {
            mApiClient.getPetTalkDetail(PETTALK_DETAIL_REQUEST, petTalkId)
            mApiClient.postPetTalkViewCount(PETTALK_VIEWCOUNT_REQUET, petTalkId)
            mApiClient.getPetTalkReplyList(PETTALK_REPLY_LIST_REQUEST, petTalkId)
        }
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(PETTALK_DETAIL_REQUEST)) {
            mApiClient.cancelRequest(PETTALK_DETAIL_REQUEST)
        }

        if (mApiClient.isRequestRunning(PETTALK_VIEWCOUNT_REQUET)) {
            mApiClient.cancelRequest(PETTALK_VIEWCOUNT_REQUET)
        }

        if (mApiClient.isRequestRunning(PETTALK_REPLY_LIST_REQUEST)) {
            mApiClient.cancelRequest(PETTALK_REPLY_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(PETTALK_REPLY_PAGING_LIST_REQUEST)) {
            mApiClient.cancelRequest(PETTALK_REPLY_PAGING_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(PETTALK_REPLY_ADD_REQUEST)) {
            mApiClient.cancelRequest(PETTALK_REPLY_ADD_REQUEST)
        }

        if (mApiClient.isRequestRunning(PETTALK_REPLY_DELETE_REQUEST)) {
            mApiClient.cancelRequest(PETTALK_REPLY_DELETE_REQUEST)
        }

        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider!!.close()
        }

        super.onDestroyView()

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_PETTALK_DELETE -> {
                if (resultCode == Activity.RESULT_OK) {
                    PetTalkActivity.instance.popBackStack()
                }
            }

            else -> super.onActivityResult(requestCode, resultCode, data)
        }
    }

    // =============================================================================================
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(data: NetworkBusResponse) {
        when(data.tag) {
            PETTALK_DETAIL_REQUEST -> {
                if (data.status == "ok") {
                    val petTalkDetailItem = Gson().fromJson(data.response, PetTalkDetailItem::class.java)

                    viewPagerImg.apply {
                        when {
                            petTalkDetailItem.petTalkImageList.size > 0 -> {
                                visibility = View.VISIBLE
                                petTalkImageList.clear()
                                petTalkImageList.addAll(petTalkDetailItem.petTalkImageList)
                                petTalkImageAdapter.notifyDataSetChanged()
                            }

                            else -> visibility = View.GONE
                        }
                    }

                    shareUrl = petTalkDetailItem.webUrl
                    setPetTalkProfile(petTalkDetailItem.petTalk, petTalkDetailItem.createdAt)
                } else {
                    Logger.d("error : ${data.code}, ${data.response}")
                }
            }

            PETTALK_REPLY_LIST_REQUEST -> {
                if ("ok" == data.status) {
                    val items: MutableList<PetTalkReplyItem> = Gson().fromJson(
                        data.response,
                        object : TypeToken<MutableList<PetTalkReplyItem>>() {}.type
                    )

                    if (replyItem.size != 0) {
                        replyItem.clear()
                    }

                    replyItem.addAll(items)
                    replyAdapter.notifyDataSetChanged()

                    if (items.size == 10) {
                        baseId = replyAdapter.getItem(replyAdapter.itemCount -1)?.id!!
                    }
                }
            }

            PETTALK_REPLY_PAGING_LIST_REQUEST -> {
                if ("ok" == data.status) {
                    val items: MutableList<PetTalkReplyItem> = Gson().fromJson(
                        data.response,
                        object : TypeToken<MutableList<PetTalkReplyItem>>() {}.type
                    )

                    if(items.size>0) {
                        for(item in items){
                            replyItem.add(item)
                            replyAdapter.notifyItemInserted(replyItem.size-1)
                        }
                    }

                    if (items.size == 10) {
                        baseId = replyAdapter.getItem(replyAdapter.itemCount -1)?.id!!
                    }
                } else {
                    Logger.d("error : ${data.code}, ${data.response}")
                }
            }

            PETTALK_REPLY_ADD_REQUEST -> {
                if ("ok" == data.status) {
                    AppToast(requireContext()).showToastMessage(
                        "댓글이 등록 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )

                    mApiClient.getPetTalkReplyList(PETTALK_REPLY_LIST_REQUEST, petTalkId)
                } else {
                    AppToast(requireContext()).showToastMessage(
                        "댓글 등록이 실패하였습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                }
            }

            PETTALK_REPLY_DELETE_REQUEST -> {
                if ("ok" == data.status) {
                    replyAdapter.removeItem(deletePosition)

                    AppToast(requireContext()).showToastMessage(
                        "댓글이 삭제 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )

                    mApiClient.getPetTalkReplyList(PETTALK_REPLY_LIST_REQUEST, petTalkId)
                } else {
                    AppToast(requireContext()).showToastMessage(
                        "댓글 삭제 실패하였습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )

                    replyAdapter.notifyItemChanged(deletePosition)
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

            if(editReply.text.toString().isNotEmpty()) {
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
        hideKeyboard(editReply)
        mApiClient.postReplyAdd(
            PETTALK_REPLY_ADD_REQUEST,
            "boards",
            petTalkId,
            editReply.text.toString()
        )

        editReply.setText("")
    }

    private fun setPetTalkProfile(item: PetTalk, createdAt: String) {
        profileImage.apply {
            when {
                item.profileImage.isNotEmpty() -> {
                    Glide.with(requireActivity())
                        .load( if(item.profileImage.startsWith("http")) item.profileImage else "${AppConstants.IMAGE_URL}${item.profileImage}")
                        .apply(RequestOptions.circleCropTransform())
                        .into(profileImage)
                }

                else -> setBackgroundResource(R.drawable.profile_default)
            }
        }

        title.text = item.title
        content.text = item.content
        nickName.text = item.nickName
        replyCount.text = "댓글 ${item.commentCount}"

        val date = createdAt.split(".")
        val createDate = date[0].replace("T", " ")
        val createdDay = date[0].split("T")
        val year = createdDay[0].split("-")
        val currentYear = yearFormat.format(System.currentTimeMillis())
        val duration = calculateTime(createDate)
        if (currentYear == year[0]) {
            if (duration / DAY > 0) {
                regDate.text = calculateDate(createdDay[0])
            } else if (duration / HOUR > 0) {
                regDate.text = "${duration/HOUR}시간전"
            } else if (duration / MIN > 0) {
                regDate.text = "${duration/MIN}분전"
            } else {
                regDate.text = "${duration/SEC}초전"
            }
        } else {
            regDate.text = calculateDateForYear(createdDay[0])
        }

        if (myUserId == item.userId.toString()) {
            btnEdit.visibility = View.VISIBLE
        } else {
            btnEdit.visibility = View.GONE
        }
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

    // ============================================================================================================
    inner class ReplyAdapter : RecyclerView.Adapter<ReplyHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ReplyHolder(layoutInflater.inflate(R.layout.adapter_banner_comment_item, parent, false))

        override fun onBindViewHolder(holder: ReplyHolder, position: Int) {
            if (myUserId == replyItem[position].userId.toString()) {
                holder.itemView.tag = true
            } else {
                holder.itemView.tag = false
            }

            holder.setNickname(replyItem[position].userNickName)
            holder.setComment(replyItem[position].content)

            holder.setImage(replyItem[position].userImg)

            val createDate = replyItem[position].createdAt.replace("T", " ")
            val createdDay = replyItem[position].createdAt.split("T")
            val currentYear = yearFormat.format(System.currentTimeMillis())
            val date = createdDay[0].split("-")
            val duration = calculateTime(createDate)
            if (currentYear == date[0]) {
                if(duration / DAY > 0) {
                    holder.setRegTime(calculateDate(createdDay[0]))
                } else if (duration / HOUR > 0) {
                    holder.setRegTime("${duration/HOUR}시간전")
                } else if (duration / MIN > 0) {
                    holder.setRegTime("${duration/MIN}분전")
                } else {
                    holder.setRegTime("${duration/SEC}초전")
                }
            } else {
                holder.setRegTime(calculateDateForYear(createdDay[0]))
            }
        }

        override fun getItemCount() = replyItem.size

        fun getItem(position: Int) = replyItem[position]

        fun removeItem(position: Int) {
            replyItem.removeAt(position)
            notifyItemRemoved(position)
        }
    }

    inner class ReplyHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setNickname(_text: String) {
            item.nickName.text = _text
        }

        fun setRegTime(_text: String) {
            item.regTime.text = _text
        }

        fun setComment(_text: String) {
            item.comment.text = _text
        }

        fun setImage(url:String?){
            if (url.isNullOrEmpty()) {
                item.profileImage.setBackgroundResource(R.drawable.profile_default)
            } else {
                Glide.with(requireActivity())
                    .load( if(url.startsWith("http")) url else "${AppConstants.IMAGE_URL}$url")
                    .apply(RequestOptions.circleCropTransform())
                    .into(item.profileImage)
            }
        }
    }

    //==============================================================================================
    inner class PetTalkImageAdapter : RecyclerView.Adapter<ImageHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ImageHolder(layoutInflater.inflate(R.layout.pettalk_image_item, parent, false))

        override fun onBindViewHolder(holder: ImageHolder, position: Int) {
            holder.imagePrint(petTalkImageList[position].image)
        }

        override fun getItemCount() = petTalkImageList.size
    }

    inner class ImageHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun imagePrint(url:String){
            Glide.with(requireActivity())
                .load( if(url.startsWith("http")) url else "${AppConstants.IMAGE_URL}$url")
                .into(item.petTalkImg)
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
                            val item = replyAdapter.getItem(position)
                            val oldUserId = StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_OLD_USER_ID, "").toInt()
                            if (oldUserId == item.userId) {
                                mApiClient.deleteReply(PETTALK_REPLY_DELETE_REQUEST, "boards", item.id)
                            } else {
                                AppToast(requireContext()).showToastMessage(
                                        "본인이 작성한 댓글만 삭제 가능합니다.",
                                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                                        AppToast.GRAVITY_BOTTOM
                                )
                                replyAdapter.notifyItemChanged(deletePosition)
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