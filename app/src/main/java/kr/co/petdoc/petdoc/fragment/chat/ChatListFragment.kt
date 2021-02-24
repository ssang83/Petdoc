package kr.co.petdoc.petdoc.fragment.chat

import android.content.Intent
import android.graphics.Canvas
import android.graphics.Typeface
import android.os.Bundle
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.ItemTouchHelper.ACTION_STATE_IDLE
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_chat_item.view.*
import kotlinx.android.synthetic.main.fragment_chat_list.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.chat.ChatMessageActivity
import kr.co.petdoc.petdoc.activity.chat.ChatQuitActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.ChatListResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.ChatItem
import kr.co.petdoc.petdoc.recyclerview.RecyclerViewSwipeDecorator
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.lang.IndexOutOfBoundsException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: ChatListFragment
 * Created by kimjoonsung on 2020/05/19.
 *
 * Description :
 */
class ChatListFragment : BaseFragment() {

    private val LOGTAG = "ChatListFragment"
    private val CHAT_LIST_REQUEST_TAG = "$LOGTAG.chatListRequest"
    private val CHAT_ITEM_DELETE_REQUEST_TAG = "$LOGTAG.chatItemDeleteRequest"

    private val DAY =  24 * 1000 * 60 * 60
    private val HOUR = 1000 * 60 * 60
    private val MIN = 1000 * 60
    private val SEC = 1000

    private lateinit var mAdapter: ChatAdapter

    private val chatItems:MutableList<ChatItem> = mutableListOf()

    private var deletePosition = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_chat_list, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseAPI(requireActivity()).logEventFirebase("상담_상담내역", "Page View", "상담내역 페이지뷰")

        btnLogin.setOnSingleClickListener {
            startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
        }

        //==========================================================================================
        mAdapter = ChatAdapter()
        chatList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        val itemTouchHelper = ItemTouchHelper(callback)
        itemTouchHelper.attachToRecyclerView(chatList)
    }

    override fun onResume() {
        super.onResume()
        retryIfNetworkAbsent {
            if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
                layoutChatList.visibility = View.VISIBLE
                layoutLogin.visibility = View.GONE

                mApiClient.getChatList(CHAT_LIST_REQUEST_TAG)
            } else {
                layoutChatList.visibility = View.GONE
                layoutLogin.visibility = View.VISIBLE
            }
        }
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(CHAT_LIST_REQUEST_TAG)) {
            mApiClient.cancelRequest(CHAT_LIST_REQUEST_TAG)
        }

        if (mApiClient.isRequestRunning(CHAT_ITEM_DELETE_REQUEST_TAG)) {
            mApiClient.cancelRequest(CHAT_ITEM_DELETE_REQUEST_TAG)
        }

        super.onDestroyView()
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================

    /**
     * Response of Image list.
     *
     * @param imageListResponse ImageListResponse
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when(response.requestTag) {
            CHAT_LIST_REQUEST_TAG -> {
                if (response is ChatListResponse) {
                    chatItems.clear()
                    chatItems.addAll(response.chatList)

                    if (chatItems.size > 0) {
                        layoutEmpty.visibility = View.GONE
                        chatList.visibility = View.VISIBLE

                        mAdapter.notifyDataSetChanged()
                    } else {
                        layoutEmpty.visibility = View.VISIBLE
                        chatList.visibility = View.GONE
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when(event.tag) {
            CHAT_ITEM_DELETE_REQUEST_TAG -> {
                if ("ok" == event.status) {
                    mAdapter.removeItem(deletePosition)

                    if (mAdapter.itemCount == 0) {
                        layoutEmpty.visibility = View.VISIBLE
                        chatList.visibility = View.GONE
                    }
                }
            }
        }
    }

    private fun calculateTime(createdAt: String) : Long {
        val dateNow = Date(System.currentTimeMillis())
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.KOREA)
        val fromDate = dateFormat.parse(createdAt)

        return dateNow.time - fromDate.time
    }

    private fun onChatItemClicked(item:ChatItem) {
        when (item.status) {
            "end" -> {
                Airbridge.trackEvent("counsel", "click", "counsel_list", null, null, null)

                startActivity(Intent(requireActivity(), ChatQuitActivity::class.java).apply {
                    putExtra("chatPk", item.pk)
                })
            }

            "wait" -> {
                AppToast(requireContext()).showToastMessage(
                    "상담 대기중입니다.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            }

            else -> {
                Airbridge.trackEvent("counsel", "click", "counsel_list", null, null, null)
                
                startActivity(Intent(requireActivity(), ChatMessageActivity::class.java).apply {
                    putExtra("chatPk", item.pk)
                    putExtra("status", item.status)
                    putExtra("name", item.partner?.name)
                })
            }
        }
    }

    //==============================================================================================
    inner class ChatAdapter : RecyclerView.Adapter<ChatHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ChatHolder(layoutInflater.inflate(R.layout.adapter_chat_item, parent, false))

        override fun onBindViewHolder(holder: ChatHolder, position: Int) {
            if (chatItems[position].partner != null) {
                holder.itemView.layoutReply.visibility = View.VISIBLE
                holder.itemView.waitingReply.visibility =View.GONE

                holder.setName(chatItems[position].partner!!.displayName)
                holder.setImage(chatItems[position].partner!!.profileImage)
            } else {
                holder.setImage("")
                holder.itemView.layoutReply.visibility = View.GONE
                holder.itemView.waitingReply.visibility =View.VISIBLE
            }

            holder.setContent(chatItems[position].lastMessage)
            holder.setStatus(chatItems[position].status, chatItems[position].userUnreadCount.toString())

            val date = chatItems[position].createdAt.split(".")
            val createDate = date[0].replace("T", " ")

            val duration = calculateTime(createDate)
            if (duration / DAY > 0) {
                holder.setRegDate("${duration/DAY}일전")
            } else if (duration / HOUR > 0) {
                holder.setRegDate("${duration/HOUR}시간전")
            } else if (duration / MIN > 0) {
                holder.setRegDate("${duration/MIN}분전")
            } else {
                holder.setRegDate("${duration/SEC}초전")
            }

            holder.itemView.setOnSingleClickListener {
                try {
                    onChatItemClicked(chatItems[position])
                } catch (e: IndexOutOfBoundsException) { }
            }
        }

        override fun getItemCount() = chatItems.size

        fun getItem(position: Int) = chatItems[position]

        fun removeItem(positon: Int) {
            chatItems.removeAt(positon)
            notifyItemRemoved(positon)
        }
    }

    inner class ChatHolder(var item:View) : RecyclerView.ViewHolder(item) {

        fun setImage(_url: String) {
            item.petDoctorImg.apply {
                if (_url.isNotEmpty()) {
                    Glide.with(requireContext())
                        .load( if(_url.startsWith("http")) _url else "${AppConstants.IMAGE_URL}$_url")
                        .apply(RequestOptions.circleCropTransform())
                        .into(item.petDoctorImg)
                } else {
                    Glide.with(requireContext())
                        .load(R.drawable.profile_default)
                        .apply(RequestOptions.circleCropTransform())
                        .into(item.petDoctorImg)
                }
            }
        }

        fun setName(_name: String) {
            item.petDoctor.text = "${_name} 수의사님"
        }

        fun setContent(_content: String) {
            item.chatContent.text = _content
        }

        fun setRegDate(_date: String) {
            item.regDate.text = _date
        }

        /**
         * TODO
         *
         * @param _status : wait(대기), ing(진행중), should_end(종료예정), end(완료)
         * @param _count : 보호자 안 읽은 숫자.
         */
        fun setStatus(_status: String, _count:String) {
            item.tag = _status
            when (_status) {
                "wait" -> {
                    item.replyStatusWating.visibility = View.VISIBLE
                    item.replyStatusCompleted.visibility = View.GONE
                    item.unReadCount.visibility = View.GONE
                }

                "end" -> {
                    item.replyStatusWating.visibility = View.GONE
                    item.replyStatusCompleted.visibility = View.VISIBLE
                    item.unReadCount.visibility = View.GONE
                }

                else -> {
                    item.replyStatusWating.visibility = View.GONE
                    item.replyStatusCompleted.visibility = View.GONE
                    item.unReadCount.visibility = View.GONE

                    item.unReadCount.text = _count
                }
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

            override fun getMovementFlags(recyclerView: RecyclerView, viewHolder: RecyclerView.ViewHolder): Int {
                // 진행중 상태인 경우 swipe 비활성화
                val state = (viewHolder.itemView.tag as String)
                return if ((state == "ing") || (state == "should_end")) {
                    ACTION_STATE_IDLE
                } else {
                    super.getMovementFlags(recyclerView, viewHolder)
                }
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                val position = viewHolder.bindingAdapterPosition
                try {
                    when (direction) {
                        ItemTouchHelper.LEFT -> {
                            Logger.d("delete position : $position")
                            deletePosition = position
                            TwoBtnDialog(requireContext()).apply {
                                setTitle("상담 삭제")
                                setMessage("상담하신 내용이 모두 삭제됩니다.\n삭제하시겠습니까?")
                                setConfirmButton("확인", View.OnClickListener {
                                    mApiClient.deleteChatItem(CHAT_ITEM_DELETE_REQUEST_TAG, chatItems[position].pk)
                                    dismiss()
                                })
                                setCancelButton("취소", View.OnClickListener {
                                    mAdapter.notifyItemChanged(position)
                                    dismiss()
                                })
                            }.show()
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