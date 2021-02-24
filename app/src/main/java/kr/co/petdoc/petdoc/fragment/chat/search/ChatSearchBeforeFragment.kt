package kr.co.petdoc.petdoc.fragment.chat.search

import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.adapter_exisiting_chat_item.view.*
import kotlinx.android.synthetic.main.adapter_exisiting_chat_item.view.recommendCount
import kotlinx.android.synthetic.main.adapter_search_talk_item.view.*
import kotlinx.android.synthetic.main.fragment_chat_search_before.*
import kotlinx.android.synthetic.main.fragment_pet_home.*
import kotlinx.android.synthetic.main.view_home_search_keyword.view.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.chat.ChatHomeActivity
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.itemdecoration.HorizontalMarginItemDecoration
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.ChatKeywordListResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.ChatKeywordItem
import kr.co.petdoc.petdoc.network.response.submodel.LegacyChatItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.OnSingleClickListener
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: ChatSearchBeforeFragment
 * Created by kimjoonsung on 2020/05/27.
 *
 * Description :
 */
class ChatSearchBeforeFragment : BaseFragment() {

    private val LOG_TAG = "ChatSearchBeforeFragment"
    private val CHAT_LIST_REQUEST = "${LOG_TAG}.chatListRequest"
    private val KEYWORD_LIST_REQUEST = "${LOG_TAG}.topKeywordListRequest"
    private val TOTAL_COUNT = "${LOG_TAG}.totalCountRequest"

    private lateinit var bestChatAdapter:ExisitingChatAdapter
    private var bestChatItems:MutableList<LegacyChatItem> = mutableListOf()

    private lateinit var allChatAdapter:AllChatAdapter
    private var chatItems:MutableList<LegacyChatItem> = mutableListOf()

    private var margin10 = 0

    private var page = 0
    private val pageOffset = 20
    private var isReload = false
    private var isFirst = true
    private var isEndofData = false
    private var chatOrder = "recommendCount"

    private var isFilterOpen = false
    private var filter = ""
    private var petKind = ""
    private var categoryId = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_chat_search_before, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        FirebaseAPI(requireActivity()).logEventFirebase("상담탭_검색", "Click Event", "상담탭의 검색버튼 클릭")

        margin10 = Helper.convertDpToPx(requireContext(), 10f).toInt()

        //==========================================================================================
        btnTop.setOnSingleClickListener {
            allChatList.scrollToPosition(0)
            appBar.setExpanded(true, true)
        }

        btnBack.setOnSingleClickListener { requireActivity().onBackPressed() }
        inputDelete.setOnSingleClickListener { editSearch.setText("") }
        btnCancel.setOnSingleClickListener { requireActivity().onBackPressed() }
        layoutChat.setOnSingleClickListener { startActivity(Intent(requireActivity(), ChatHomeActivity::class.java)) }

        layoutOrder.setOnSingleClickListener {
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

                setOnSingleClickListener {
                    order.text = Helper.readStringRes(R.string.chat_exisiting_info_order_recommend)
                    layoutOrderMenu.visibility = View.GONE

                    page = 0
                    chatOrder = "recommendCount"

                    mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                }
            }

            orderRecently.apply {
                when {
                    order.text == Helper.readStringRes(R.string.chat_exisiting_info_order_recently) -> {
                        setTextColor(Helper.readColorRes(R.color.orange))
                    }

                    else -> setTextColor(Helper.readColorRes(R.color.dark_grey))
                }

                setOnSingleClickListener {
                    order.text = Helper.readStringRes(R.string.chat_exisiting_info_order_recently)
                    layoutOrderMenu.visibility = View.GONE

                    page = 0
                    chatOrder = "createdAt"

                    mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                }
            }
        }

        btnCategoryFilter.setOnSingleClickListener {
            if (!isFilterOpen) {
                layoutFilter.visibility = View.VISIBLE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_close)
                isFilterOpen = true
            } else {
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false
            }
        }

        filterAll.setOnSingleClickListener(clickListener)
        filterAction.setOnSingleClickListener(clickListener)
        filterFeed.setOnSingleClickListener(clickListener)
        filterDisease.setOnSingleClickListener(clickListener)
        filterOriental.setOnSingleClickListener(clickListener)
        filterSurgery.setOnSingleClickListener(clickListener)
        filterVaccine.setOnSingleClickListener(clickListener)
        filterEtc.setOnSingleClickListener(clickListener)
        layoutPetAll.setOnSingleClickListener(clickListener)
        layoutDog.setOnSingleClickListener(clickListener)
        layoutCat.setOnSingleClickListener(clickListener)
        layoutEtc.setOnSingleClickListener(clickListener)

        emptyTouch.setOnSingleClickListener { layoutOrderMenu.visibility = View.GONE }

        //========================================================================================
        editSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                layoutDel.apply {
                    when {
                        s?.length!! > 0 -> visibility = View.VISIBLE
                        else -> visibility = View.GONE
                    }
                }
            }

            override fun afterTextChanged(s: Editable?) {}
        })

        //========================================================================================
        bestChatAdapter = ExisitingChatAdapter()
        bestChatList.apply {
            layoutManager = LinearLayoutManager(requireContext()).apply {
                orientation = LinearLayoutManager.HORIZONTAL }
            adapter = bestChatAdapter

            addItemDecoration(
                HorizontalMarginItemDecoration(
                    marginStart = margin10,
                    marginBetween = 0,
                    marginEnd = margin10
                )
            )
        }

        allChatAdapter = AllChatAdapter()
        allChatList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = allChatAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        layoutFilter.visibility = View.GONE
                        layoutOrderMenu.visibility = View.GONE
                        btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                        isFilterOpen = false
                    }

                    // 리스트 최 하단 체크한 후 API 호출
                    if (!isEndofData && !allChatList.canScrollVertically(1)) {
                        isReload = true
                        ++page

                        mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, categoryId, chatOrder, pageOffset, page*pageOffset)
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    (layoutManager as LinearLayoutManager).apply{
                        val visibleCount = findFirstVisibleItemPosition()
                        if(visibleCount > 10) {
                            btnTop.visibility = View.VISIBLE
                        } else {
                            btnTop.visibility = View.GONE
                        }
                    }
                }
            })
        }

        editSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    Airbridge.trackEvent("counsel", "click", "search_done", null, null, null)
                    bundleOf("keyword" to editSearch.text.toString()).let {
                        hideKeyboard(editSearch)
                        findNavController().navigate(R.id.action_chatSearchBeforeFragment_to_chatSearchResultFragment, it)
                    }
                }

                return true
            }
        })

        btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)

        imageViewPetAll.isSelected = true
        textViewPetAll.isSelected = true
        textViewPetAll.setTypeface(null, Typeface.BOLD)

        filterAll.isSelected = true
        filter = Helper.readStringRes(R.string.home_search_filter_all)
        
        loadData()
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(CHAT_LIST_REQUEST)) {
            mApiClient.cancelRequest(CHAT_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(KEYWORD_LIST_REQUEST)) {
            mApiClient.cancelRequest(KEYWORD_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(TOTAL_COUNT)) {
            mApiClient.cancelRequest(TOTAL_COUNT)
        }
        super.onDestroyView()
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
    fun onEventMainThread(event: NetworkBusResponse) {
        when(event.tag) {
            CHAT_LIST_REQUEST -> {
                if (event.status == "ok") {
                    val items:MutableList<LegacyChatItem> = Gson().fromJson(event.response, object : TypeToken<MutableList<LegacyChatItem>>() {}.type)

                    isEndofData = items.size < pageOffset

                    // 전체 상담 리스트
                    if (items.size > 0) {
                        allChatList.visibility = View.VISIBLE
                        empty.visibility = View.GONE

                        if (!isReload) {
                            chatItems.clear()
                            chatItems.addAll(items)
                            allChatAdapter.notifyDataSetChanged()
                        } else {
                            for (item in items) {
                                chatItems.add(item)
                                allChatAdapter.notifyItemInserted(chatItems.size - 1)
                            }
                        }
                    } else {
                        allChatList.visibility = View.GONE
                        empty.visibility = View.VISIBLE
                    }

                    // 베스트 상담 리스트
                    if(isFirst) {
                        for (i in 0..9) {
                            bestChatItems.add(chatItems[i])
                            bestChatAdapter.notifyItemInserted(bestChatItems.size -1)
                        }

                        isFirst = false
                    }
                } else {
                    Logger.d("code : ${event.code}, response : ${event.response}")
                }
            }

            TOTAL_COUNT -> {
                if (event.status == "ok") {
                    searchCount.text = event.response
                } else {
                    Logger.d("code : ${event.code}, response : ${event.response}")
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            KEYWORD_LIST_REQUEST -> {
                if (response is ChatKeywordListResponse) {
                    setKeywordList(response.keywordList)
                }
            }
        }
    }

    //==============================================================================================
    private fun loadData() {
        if (!mApiClient.isRequestRunning(CHAT_LIST_REQUEST)) {
            mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, categoryId, chatOrder, 20, 0)
        }

        if (!mApiClient.isRequestRunning(KEYWORD_LIST_REQUEST)) {
            mApiClient.getChatKeywordList(KEYWORD_LIST_REQUEST)
        }

        if (!mApiClient.isRequestRunning(TOTAL_COUNT)) {
            mApiClient.getLegacyChatTotalCount(TOTAL_COUNT, petKind, categoryId)
        }
    }

    private fun setKeywordList(keywords: List<ChatKeywordItem>) {
        keywordTags.removeAllViews()
        for (i in 0 until keywords.size) {
            keywordTags.addView(
                KeywordTag(requireContext(), keywords[i].keyword)
            )
        }
    }

    private fun keywordClicked(keyword: String) {
        Airbridge.trackEvent("counsel", "click", "search_done", null, null, null)

        hideKeyboard(editSearch)

        bundleOf("keyword" to keyword).let {
            findNavController().navigate(
                R.id.action_chatSearchBeforeFragment_to_chatSearchResultFragment,
                it
            )
        }
    }

    private fun onTalkItemClicked(item:LegacyChatItem) {
        bundleOf("chatId" to item.id).let {
            findNavController().navigate(R.id.action_chatSearchBeforeFragment_to_chatSearchDetailFragment, it)
        }
    }

    private fun handleCategoryColor(view: View) {
        filterAll.isSelected = false
        filterAction.isSelected = false
        filterFeed.isSelected = false
        filterDisease.isSelected = false
        filterOriental.isSelected = false
        filterSurgery.isSelected = false
        filterVaccine.isSelected = false
        filterEtc.isSelected = false

        view.isSelected = true
    }

    private fun getCategoryId(): String {
        var categoryId = ""
        for (item in PetdocApplication.mSearchCategoryList) {
            if (item.name == filter) {
                categoryId = item.pk.toString()
            }
        }

        return categoryId
    }

    //==============================================================================================
    inner class ExisitingChatAdapter : RecyclerView.Adapter<ExisitingChatHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ExisitingChatHolder(layoutInflater.inflate(R.layout.adapter_exisiting_chat_item, parent, false))

        override fun onBindViewHolder(holder: ExisitingChatHolder, position: Int) {
            val createdAt = bestChatItems[position].createdAt.split("T")
            val format1 = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
            val format = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
            val date = format1.parse(createdAt[0].replace("-", ""))
            val regDate = format.format(date)

            holder.setDate(regDate)
            holder.setContent(bestChatItems[position].counselRequestText)
            holder.setRecommendCount(bestChatItems[position].recommendCount.toString())

            holder.itemView.setOnSingleClickListener {
                onTalkItemClicked(bestChatItems[position])
            }
        }

        override fun getItemCount() = bestChatItems.size
    }

    inner class ExisitingChatHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setDate(_date: String) {
            item.regDate.text = _date
        }

        fun setContent(_content: String) {
            item.chatContent.text = _content
        }

        fun setRecommendCount(_count: String) {
            item.recommendCount.text = "추천 ${_count}"
        }
    }

    //================================================================================================
    inner class AllChatAdapter : RecyclerView.Adapter<ChatHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ChatHolder(layoutInflater.inflate(R.layout.adapter_search_talk_item, parent, false))

        override fun onBindViewHolder(holder: ChatHolder, position: Int) {
            holder.setContent(chatItems[position].counselRequestText)
            holder.setRecommendCnt(chatItems[position].recommendCount.toString())

            val category = if(chatItems[position].categoryParentName == null) {
                "미지정"
            } else {
                chatItems[position].categoryParentName
            }

            holder.setPetCategory("${chatItems[position].kind} ∙ ${category}")

            val createdAt = chatItems[position].createdAt.split("T")
            val format1 = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
            val format = SimpleDateFormat("yyyy년MM월dd일", Locale.KOREA)
            val date = format1.parse(createdAt[0].replace("-", ""))
            val regDate = format.format(date)
            holder.setDate(regDate)

            holder.itemView.setOnSingleClickListener { onTalkItemClicked(chatItems[position]) }
        }

        override fun getItemCount() = chatItems.size
    }

    inner class ChatHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setPetCategory(_text: String) {
            item.petFilter.text = _text
        }

        fun setDate(_text: String) {
            item.date.text = _text
        }

        fun setContent(_text: String) {
            item.content.text = _text
        }

        fun setRecommendCnt(_text: String) {
            item.recommendCount.text = "추천 ${_text}"
        }
    }

    //==============================================================================================
    inner class KeywordTag(context: Context, keyword:String) : FrameLayout(context) {

        init {
            val view = layoutInflater.inflate(R.layout.view_home_search_keyword, null, false)

            view.keyword.text = keyword
            view.keyword.setOnSingleClickListener {
                keywordClicked(keyword)
            }

            addView(view)
        }
    }

    private val clickListener = View.OnClickListener {
        when (it?.id) {
            R.id.filterAll -> {
                filter = Helper.readStringRes(R.string.home_search_filter_all)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_all)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                page = 0
                chatOrder = "recommendCount"

                mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                mApiClient.getLegacyChatTotalCount(TOTAL_COUNT, petKind, getCategoryId())
            }

            R.id.filterAction -> {
                filter = Helper.readStringRes(R.string.home_search_filter_action)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_action)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                page = 0
                chatOrder = "recommendCount"

                mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                mApiClient.getLegacyChatTotalCount(TOTAL_COUNT, petKind, getCategoryId())
            }

            R.id.filterFeed -> {
                filter = Helper.readStringRes(R.string.home_search_filter_feed)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_feed)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                page = 0
                chatOrder = "recommendCount"

                mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                mApiClient.getLegacyChatTotalCount(TOTAL_COUNT, petKind, getCategoryId())
            }

            R.id.filterDisease -> {
                filter = Helper.readStringRes(R.string.home_search_filter_disease)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_disease)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                page = 0
                chatOrder = "recommendCount"

                mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                mApiClient.getLegacyChatTotalCount(TOTAL_COUNT, petKind, getCategoryId())
            }

            R.id.filterOriental -> {
                filter = Helper.readStringRes(R.string.home_search_filter_oriental)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_oriental)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                page = 0
                chatOrder = "recommendCount"

                mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                mApiClient.getLegacyChatTotalCount(TOTAL_COUNT, petKind, getCategoryId())
            }

            R.id.filterSurgery -> {
                filter = Helper.readStringRes(R.string.home_search_filter_surgery)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_surgery)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)

                handleCategoryColor(it)

                page = 0
                chatOrder = "recommendCount"

                mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                mApiClient.getLegacyChatTotalCount(TOTAL_COUNT, petKind, getCategoryId())
            }

            R.id.filterVaccine -> {
                filter = Helper.readStringRes(R.string.home_search_filter_vaccine)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_vaccine)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                page = 0
                chatOrder = "recommendCount"

                mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                mApiClient.getLegacyChatTotalCount(TOTAL_COUNT, petKind, getCategoryId())
            }

            R.id.filterEtc -> {
                filter = Helper.readStringRes(R.string.home_search_filter_etc)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_etc)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                page = 0
                chatOrder = "recommendCount"

                mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                mApiClient.getLegacyChatTotalCount(TOTAL_COUNT, petKind, getCategoryId())
            }

            R.id.layoutPetAll -> {
                petKind =""

                imageViewPetAll.isSelected = true
                imageViewDog.isSelected = false
                imageViewCat.isSelected = false
                imageViewEtc.isSelected = false

                textViewPetAll.isSelected = true
                textViewPetAll.setTypeface(null, Typeface.BOLD)
                textViewDog.isSelected = false
                textViewCat.isSelected = false
                textViewEtc.isSelected = false

                page = 0
                chatOrder = "recommendCount"

                mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                mApiClient.getLegacyChatTotalCount(TOTAL_COUNT, petKind, getCategoryId())
            }

            R.id.layoutDog -> {
                petKind ="강아지"

                imageViewPetAll.isSelected = false
                imageViewDog.isSelected = true
                imageViewCat.isSelected = false
                imageViewEtc.isSelected = false

                textViewPetAll.isSelected = false
                textViewDog.isSelected = true
                textViewDog.setTypeface(null, Typeface.BOLD)
                textViewCat.isSelected = false
                textViewEtc.isSelected = false

                page = 0
                chatOrder = "recommendCount"

                mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                mApiClient.getLegacyChatTotalCount(TOTAL_COUNT, petKind, getCategoryId())
            }

            R.id.layoutCat -> {
                petKind ="고양이"

                imageViewPetAll.isSelected = false
                imageViewDog.isSelected = false
                imageViewCat.isSelected = true
                imageViewEtc.isSelected = false

                textViewPetAll.isSelected = false
                textViewDog.isSelected = false
                textViewCat.isSelected = true
                textViewCat.setTypeface(null, Typeface.BOLD)
                textViewEtc.isSelected = false

                page = 0
                chatOrder = "recommendCount"

                mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                mApiClient.getLegacyChatTotalCount(TOTAL_COUNT, petKind, getCategoryId())
            }

            R.id.layoutEtc -> {
                petKind ="특수동물"

                imageViewPetAll.isSelected = false
                imageViewDog.isSelected = false
                imageViewCat.isSelected = false
                imageViewEtc.isSelected = true

                textViewPetAll.isSelected = false
                textViewDog.isSelected = false
                textViewCat.isSelected = false
                textViewEtc.isSelected = true
                textViewEtc.setTypeface(null, Typeface.BOLD)

                page = 0
                chatOrder = "recommendCount"

                mApiClient.getLegacyChatList(CHAT_LIST_REQUEST, petKind, getCategoryId(), chatOrder, pageOffset, page*pageOffset)
                mApiClient.getLegacyChatTotalCount(TOTAL_COUNT, petKind, getCategoryId())
            }
        }
    }
}