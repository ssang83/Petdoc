package kr.co.petdoc.petdoc.fragment.chat.search

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
import android.widget.TextView
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.adapter_search_talk_item.view.*
import kotlinx.android.synthetic.main.fragment_chat_search_result.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.chat.ChatHomeActivity
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.ChatSearchResultListResponse
import kr.co.petdoc.petdoc.network.response.submodel.LegacyChatItem
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: ChatSearchResultFragment
 * Created by kimjoonsung on 2020/05/29.
 *
 * Description :
 */
class ChatSearchResultFragment : BaseFragment() {

    private val LOGTAG = "ChatSearchResultFragment"
    private val CHAT_SEARCH_RESULT_LIST_REQUEST = "${LOGTAG}.chatSearchResultListRequest"

    private lateinit var resultAdapter:SearchResultAdapter
    private var chatItems:MutableList<LegacyChatItem> = mutableListOf()

    private var keyword = ""

    private var page = 0
    private val pageOffset = 20
    private var isReload = false
    private var isEndofData = false
    private var order = "recommendCount"

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
        return inflater.inflate(R.layout.fragment_chat_search_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnTop.setOnClickListener { searchResultList.scrollToPosition(0) }
        layoutChat.setOnClickListener { startActivity(Intent(requireActivity(), ChatHomeActivity::class.java)) }

/*        layoutOrder.setOnClickListener {
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
                    chatOrder = "recommendCount"

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
                    chatOrder = "createdAt"

                }
            }
        }*/

        inputDelete.setOnClickListener { editSearch.setText("") }
        btnCancel.setOnClickListener { requireActivity().onBackPressed() }
        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        btnCategoryFilter.setOnClickListener {
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

        filterAll.setOnClickListener(clickListener)
        filterAction.setOnClickListener(clickListener)
        filterFeed.setOnClickListener(clickListener)
        filterDisease.setOnClickListener(clickListener)
        filterOriental.setOnClickListener(clickListener)
        filterSurgery.setOnClickListener(clickListener)
        filterVaccine.setOnClickListener(clickListener)
        filterEtc.setOnClickListener(clickListener)
        layoutPetAll.setOnClickListener(clickListener)
        layoutDog.setOnClickListener(clickListener)
        layoutCat.setOnClickListener(clickListener)
        layoutEtc.setOnClickListener(clickListener)

        editSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard(editSearch)

                    page = 0
                    order = "recommendCount"

                    mApiClient.getChatSearchResultList(
                        CHAT_SEARCH_RESULT_LIST_REQUEST,
                        editSearch.text.toString(),
                        petKind,
                        getCategoryId(),
                        order,
                        pageOffset,
                        page
                    )
                }

                return true
            }
        })

        editSearch.addTextChangedListener(object : TextWatcher {
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

        //==========================================================================================
        resultAdapter = SearchResultAdapter()
        searchResultList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        layoutFilter.visibility = View.GONE
                        btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                        isFilterOpen = false
                    }

                    // 리스트 최 하단 체크한 후 API 호출
                    if (!isEndofData && !searchResultList.canScrollVertically(1)) {
                        isReload = true
                        ++page

                        mApiClient.getChatSearchResultList(
                            CHAT_SEARCH_RESULT_LIST_REQUEST,
                            keyword,
                            petKind,
                            categoryId,
                            order,
                            pageOffset,
                            page
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

        keyword = arguments?.getString("keyword") ?: keyword
        Logger.d("keyword : $keyword")

        btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)

        imageViewPetAll.isSelected = true
        textViewPetAll.isSelected = true
        textViewPetAll.setTypeface(null, Typeface.BOLD)

        filterAll.isSelected = true
        filter = Helper.readStringRes(R.string.home_search_filter_all)

        editSearch.setText(keyword)

        mApiClient.getChatSearchResultList(
            CHAT_SEARCH_RESULT_LIST_REQUEST,
            keyword,
            petKind,
            categoryId,
            order,
            pageOffset,
            page
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(CHAT_SEARCH_RESULT_LIST_REQUEST)) {
            mApiClient.cancelRequest(CHAT_SEARCH_RESULT_LIST_REQUEST)
        }
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
            CHAT_SEARCH_RESULT_LIST_REQUEST -> {
                if (response is ChatSearchResultListResponse) {
                    isEndofData = response.searchResultList.size < pageOffset

                    if (response.totalCount > 0) {
                        searchResultList.visibility = View.VISIBLE
                        empty.visibility = View.GONE

                        searchCount.text = response.totalCount.toString()

                        if (!isReload) {
                            chatItems.clear()
                            chatItems.addAll(response.searchResultList)
                            resultAdapter.notifyDataSetChanged()
                        } else {
                            for (item in response.searchResultList) {
                                chatItems.add(item)
                                resultAdapter.notifyItemInserted(chatItems.size - 1)
                            }
                        }
                    } else {
                        searchResultList.visibility = View.GONE
                        empty.apply {
                            visibility = View.VISIBLE
                            text = "\'${keyword}\' ${Helper.readStringRes(R.string.chat_search_all_chat_empty)}"
                        }

                        searchCount.text = "0"
                    }
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
        Logger.d("message : ${event.resultMsgUser}")
    }

    private fun onTalkItemClicked(item:LegacyChatItem) {
        Airbridge.trackEvent("counsel", "click", "search_result", null, null, null)

        bundleOf("chatId" to item.id).let {
            findNavController().navigate(R.id.action_chatSearchResultFragment_to_chatSearchDetailFragment, it)
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
    inner class SearchResultAdapter : RecyclerView.Adapter<ChatHolder>() {

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

            holder.itemView.setOnClickListener { onTalkItemClicked(chatItems[position]) }
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
                order = "recommendCount"

                mApiClient.getChatSearchResultList(CHAT_SEARCH_RESULT_LIST_REQUEST, keyword, petKind, getCategoryId(), order, pageOffset, page)
            }

            R.id.filterAction -> {
                filter = Helper.readStringRes(R.string.home_search_filter_action)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_action)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                page = 0
                order = "recommendCount"

                mApiClient.getChatSearchResultList(CHAT_SEARCH_RESULT_LIST_REQUEST, keyword, petKind, getCategoryId(), order, pageOffset, page)
            }

            R.id.filterFeed -> {
                filter = Helper.readStringRes(R.string.home_search_filter_feed)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_feed)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                page = 0
                order = "recommendCount"

                mApiClient.getChatSearchResultList(CHAT_SEARCH_RESULT_LIST_REQUEST, keyword, petKind, getCategoryId(), order, pageOffset, page)
            }


            R.id.filterDisease -> {
                filter = Helper.readStringRes(R.string.home_search_filter_disease)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_disease)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                page = 0
                order = "recommendCount"

                mApiClient.getChatSearchResultList(CHAT_SEARCH_RESULT_LIST_REQUEST, keyword, petKind, getCategoryId(), order, pageOffset, page)
            }

            R.id.filterOriental -> {
                filter = Helper.readStringRes(R.string.home_search_filter_oriental)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_oriental)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                page = 0
                order = "recommendCount"

                mApiClient.getChatSearchResultList(CHAT_SEARCH_RESULT_LIST_REQUEST, keyword, petKind, getCategoryId(), order, pageOffset, page)
            }

            R.id.filterSurgery -> {
                filter = Helper.readStringRes(R.string.home_search_filter_surgery)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_surgery)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)

                handleCategoryColor(it)

                page = 0
                order = "recommendCount"

                mApiClient.getChatSearchResultList(CHAT_SEARCH_RESULT_LIST_REQUEST, keyword, petKind, getCategoryId(), order, pageOffset, page)
            }

            R.id.filterVaccine -> {
                filter = Helper.readStringRes(R.string.home_search_filter_vaccine)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_vaccine)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                page = 0
                order = "recommendCount"

                mApiClient.getChatSearchResultList(CHAT_SEARCH_RESULT_LIST_REQUEST, keyword, petKind, getCategoryId(), order, pageOffset, page)
            }

            R.id.filterEtc -> {
                filter = Helper.readStringRes(R.string.home_search_filter_etc)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_etc)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                page = 0
                order = "recommendCount"

                mApiClient.getChatSearchResultList(CHAT_SEARCH_RESULT_LIST_REQUEST, keyword, petKind, getCategoryId(), order, pageOffset, page)
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
                order = "recommendCount"

                mApiClient.getChatSearchResultList(CHAT_SEARCH_RESULT_LIST_REQUEST, keyword, petKind, getCategoryId(), order, pageOffset, page)
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
                order = "recommendCount"

                mApiClient.getChatSearchResultList(CHAT_SEARCH_RESULT_LIST_REQUEST, keyword, petKind, getCategoryId(), order, pageOffset, page)
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
                order = "recommendCount"

                mApiClient.getChatSearchResultList(CHAT_SEARCH_RESULT_LIST_REQUEST, keyword, petKind, getCategoryId(), order, pageOffset, page)
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
                order = "recommendCount"

                mApiClient.getChatSearchResultList(CHAT_SEARCH_RESULT_LIST_REQUEST, keyword, petKind, getCategoryId(), order, pageOffset, page)
            }
        }
    }
}