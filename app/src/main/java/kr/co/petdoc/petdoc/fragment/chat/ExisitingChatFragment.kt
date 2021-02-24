package kr.co.petdoc.petdoc.fragment.chat

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_exisiting_chat_more_item.view.*
import kotlinx.android.synthetic.main.fragment_exisiting_chat.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.chat.ChatQuitActivity
import kr.co.petdoc.petdoc.activity.chat.ChatSearchDetailActivity
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.LegacyChatItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.ChatDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: ExisitingChatFragment
 * Created by kimjoonsung on 2020/05/21.
 *
 * Description :
 */
class ExisitingChatFragment : BaseFragment() {

    private val LOG_TAG = "ExisitingChatFragment"
    private val LEGACY_CHAT_LIST_REQUEST = "${LOG_TAG}.legacyChatListRequest"
    private val LEGACY_CHAT_TOTAL_COUNT_REQUEST = "${LOG_TAG}.legacyChatTotalCountRequest"

    private lateinit var dataModel: ChatDataModel
    private lateinit var chatAdapter: ChatAdapter

    private var chatItems:MutableList<LegacyChatItem> = mutableListOf()

    var page = 0
    private val pageOffset = 20
    var isReload = false
    var isEndofData = false
    var magazineOrder = "recommendCount"

    private var petKind = ""
    private var categoryId = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(ChatDataModel::class.java)
        return inflater.inflate(R.layout.fragment_exisiting_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        FirebaseAPI(requireActivity()).logEventFirebase("상담_기존정보", "Page View", "기존 정보 확인 페이지뷰")

        btnTop.setOnClickListener {
            chatList.scrollToPosition(0)
            appBar.setExpanded(true, true)
        }

        layoutOrder.setOnClickListener {
            if (layoutOrderMenu.visibility == View.VISIBLE) {
                layoutOrderMenu.visibility = View.GONE
            } else {
                layoutOrderMenu.visibility = View.VISIBLE
            }

            orderRecommed.apply {
                when {
                    order.text == Helper.readStringRes(R.string.chat_exisiting_info_order_recommend) -> {
                        setTextColor(Helper.readColorRes(R.color.orange))
                    }

                    else -> setTextColor(Helper.readColorRes(R.color.dark_grey))
                }

                setOnClickListener {
                    order.text = Helper.readStringRes(R.string.chat_exisiting_info_order_recommend)
                    layoutOrderMenu.visibility = View.GONE

                    page = 0
                    magazineOrder = "recommendCount"
                    isReload = true
                    chatItems.clear()

                    mApiClient.getLegacyChatList(LEGACY_CHAT_LIST_REQUEST, petKind, categoryId, magazineOrder, pageOffset, page*pageOffset)
                }
            }

            orderRecently.apply {
                when {
                    order.text == Helper.readStringRes(R.string.chat_exisiting_info_order_recently) -> {
                        setTextColor(Helper.readColorRes(R.color.orange))
                    }

                    else -> setTextColor(Helper.readColorRes(R.color.dark_grey))
                }

                setOnClickListener {
                    order.text = Helper.readStringRes(R.string.chat_exisiting_info_order_recently)
                    layoutOrderMenu.visibility = View.GONE

                    page = 0
                    magazineOrder = "createdAt"
                    isReload = true
                    chatItems.clear()

                    mApiClient.getLegacyChatList(LEGACY_CHAT_LIST_REQUEST, petKind, categoryId, magazineOrder, pageOffset, page*pageOffset)
                }
            }
        }

        btnClose.setOnClickListener { requireActivity().onBackPressed() }

        //=========================================================================================
        chatAdapter = ChatAdapter()
        chatList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = chatAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        layoutOrderMenu.visibility = View.GONE
                    }

                    // 리스트 최 하단 체크한 후 API 호출
                    if (!isEndofData && !chatList.canScrollVertically(1)) {
                        isReload = true
                        ++page

                        mApiClient.getLegacyChatList(
                            LEGACY_CHAT_LIST_REQUEST,
                            petKind,
                            categoryId,
                            magazineOrder,
                            pageOffset,
                            page * pageOffset
                        )
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    (layoutManager as LinearLayoutManager).apply {
                        val visibleCount = findFirstVisibleItemPosition()
                        if (visibleCount > 10) {
                            btnTop.visibility = View.VISIBLE
                        } else {
                            btnTop.visibility = View.GONE
                        }
                    }
                }
            })
        }

        //=========================================================================================
        petKind = dataModel.petInfo.value?.kind.toString()
        categoryId = dataModel.category.value?.pk.toString()

        Logger.d("petKind : $petKind, categoryId : $categoryId")

        mApiClient.getLegacyChatList(LEGACY_CHAT_LIST_REQUEST, petKind, categoryId, magazineOrder, pageOffset, page*pageOffset)
        mApiClient.getLegacyChatTotalCount(LEGACY_CHAT_TOTAL_COUNT_REQUEST, petKind, categoryId)

        setPetType(petKind)
        setCategoryType(dataModel.category.value?.name!!)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(LEGACY_CHAT_LIST_REQUEST)) {
            mApiClient.cancelRequest(LEGACY_CHAT_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(LEGACY_CHAT_TOTAL_COUNT_REQUEST)) {
            mApiClient.cancelRequest(LEGACY_CHAT_TOTAL_COUNT_REQUEST)
        }
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            LEGACY_CHAT_LIST_REQUEST -> {
                if (event.status == "ok") {
                    val items:MutableList<LegacyChatItem> = Gson().fromJson(event.response, object : TypeToken<MutableList<LegacyChatItem>>() {}.type)

                    isEndofData = items.size < pageOffset

                    if (items.size > 0) {
                        for (item in items) {
                            chatItems.add(item)
                            chatAdapter.notifyItemInserted(chatItems.size - 1)
                        }
                    }
                } else {
                    Logger.d("code : ${event.code}, response : ${event.response}")
                }
            }

            LEGACY_CHAT_TOTAL_COUNT_REQUEST -> {
                if (event.status == "ok") {
                    chatCount.text = event.response
                } else {
                    Logger.d("code : ${event.code}, response : ${event.response}")
                }
            }

        }

    }

    /**
     * EventBus listener. An API call failed. No error message was returned.
     *
     * @param event ApiErrorEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ApiErrorEvent) {
        Logger.p(event.throwable)
    }

    /**
     * EventBus listener. An API call failed. An error message was returned.
     *
     * @param event ApiErrorWithMessageEvent Contains the error message.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ApiErrorWithMessageEvent) {
        Logger.d("error : ${event.resultMsgUser}")
    }

    private fun onChatDetail(item: LegacyChatItem) {
        startActivity(Intent(requireActivity(), ChatSearchDetailActivity::class.java).apply {
            putExtra("chatId", item.id)
        })
    }

    private fun setPetType(kind: String) {
        when (kind) {
            "강아지" -> {
                layoutPetType.setBackgroundResource(R.drawable.bg_chat_dog)
                petTypeImg.setBackgroundResource(R.drawable.ic_category_dog)
                petTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_dog))
                petTypeTxt.setText(Helper.readStringRes(R.string.chat_pet_dog))
            }

            "고양이" -> {
                layoutPetType.setBackgroundResource(R.drawable.bg_chat_cat)
                petTypeImg.setBackgroundResource(R.drawable.ic_category_cat)
                petTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_cat))
                petTypeTxt.setText(Helper.readStringRes(R.string.chat_pet_cat))
            }

            else -> {
                layoutPetType.setBackgroundResource(R.drawable.bg_chat_extra)
                petTypeImg.setBackgroundResource(R.drawable.ic_category_extra)
                petTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_extra))
                petTypeTxt.setText(Helper.readStringRes(R.string.chat_pet_extra))
            }
        }
    }

    private fun setCategoryType(category: String) {
        when (category) {
            "행동" -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_behavior)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_behavior)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_behavior))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_behavior))
            }

            "수술" -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_surgery)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_surgery)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_surgery))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_surgery))
            }

            "영양" -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_nutrition)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_nutrition)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_nutrition))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_nutrition))
            }

            "예방접종" -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_shot)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_shot)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_shot))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_shot))
            }

            "질병" -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_disease)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_disease)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_disease))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_disease))
            }

            "한방/재활" -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_oriental)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_oriental)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_oriental))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_oriental))
            }

            else -> {
                layoutCategoryType.setBackgroundResource(R.drawable.bg_chat_category_etc)
                categoryTypeImg.setBackgroundResource(R.drawable.ic_category_etc)
                categoryTypeTxt.setText(Helper.readStringRes(R.string.chat_category_etc))
                categoryTypeTxt.setTextColor(Helper.readColorRes(R.color.chat_category_etc))
            }
        }
    }

    //==============================================================================================
    inner class ChatAdapter : RecyclerView.Adapter<ChatHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ChatHolder(layoutInflater.inflate(R.layout.adapter_exisiting_chat_more_item, parent, false))

        override fun onBindViewHolder(holder: ChatHolder, position: Int) {
            holder.setContent(chatItems[position].counselRequestText)
            holder.setRecommendCount(chatItems[position].recommendCount.toString())
            holder.setPetCategory("${chatItems[position].kind} ∙ ${chatItems[position].categoryParentName}")

            val createdAt = chatItems[position].createdAt.split("T")
            val format1 = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
            val format = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
            val date = format1.parse(createdAt[0].replace("-", ""))
            val regDate = format.format(date)
            holder.setDate(regDate)

            holder.itemView.setOnClickListener {
                onChatDetail(chatItems[position])
            }
        }

        override fun getItemCount() = chatItems.size
    }

    inner class ChatHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setPetCategory(_petCategory: String) {
            item.petCategory.text = _petCategory
        }

        fun setDate(_date: String) {
            item.date.text = _date
        }

        fun setContent(_content: String) {
            item.content.text = _content
        }

        fun setRecommendCount(_count: String) {
            item.recommendCount.text = "추천 ${_count}"
        }
    }
}