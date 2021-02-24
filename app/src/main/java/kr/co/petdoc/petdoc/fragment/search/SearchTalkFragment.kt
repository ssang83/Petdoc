package kr.co.petdoc.petdoc.fragment.search

import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.adapter_search_talk_item.view.*
import kotlinx.android.synthetic.main.fragment_search_talk.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.chat.ChatHomeActivity
import kr.co.petdoc.petdoc.activity.chat.ChatSearchDetailActivity
import kr.co.petdoc.petdoc.extensions.highlight
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.ApiClient
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.ChatSearchResultListResponse
import kr.co.petdoc.petdoc.network.response.submodel.LegacyChatItem
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: SearchTalkFragment
 * Created by kimjoonsung on 2020/04/22.
 *
 * Description :
 */
class SearchTalkFragment : Fragment() {

    private val LOGTAG = "SearchTalkFragment"
    private val CHAT_SEARCH_RESULT_LIST_REQUEST = "${LOGTAG}.chatSearchResultListRequest"

    private lateinit var mAdapter: TalkAdapter
    private var chatItems:MutableList<LegacyChatItem> = mutableListOf()

    private var keyword = ""

    private var page = 0
    private val pageOffset = 20
    private var isReload = false
    private var isEndofData = false
    private var chatOrder = "recommendCount"

    private var isFilterOpen = false
    private var filter = ""
    private var petKind = ""
    private var categoryId = ""

    private val mApiClient: ApiClient = PetdocApplication.application.apiClient
    private lateinit var mEventBus: EventBus

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_talk, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mEventBus = EventBus.getDefault()
        if(!mEventBus.isRegistered(this)) {
            mEventBus.register(this)
        }

        Airbridge.trackEvent("identify", "click", "result_상담", null, null, null)

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

        /*layoutOrder.setOnClickListener {
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

                    mApiClient.getChatSearchResultList(
                        CHAT_SEARCH_RESULT_LIST_REQUEST,
                        keyword,
                        petKind,
                        getCategoryId(),
                        chatOrder,
                        pageOffset,
                        page
                    )
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

                    mApiClient.getChatSearchResultList(
                        CHAT_SEARCH_RESULT_LIST_REQUEST,
                        keyword,
                        petKind,
                        getCategoryId(),
                        chatOrder,
                        pageOffset,
                        page
                    )
                }
            }
        }*/

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

        btnTop.setOnClickListener { searchTalkList.scrollToPosition(0) }
        layoutChat.setOnClickListener { startActivity(Intent(requireActivity(), ChatHomeActivity::class.java)) }

        //==========================================================================================

        mAdapter = TalkAdapter()
        searchTalkList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter

            addOnScrollListener(object : RecyclerView.OnScrollListener() {
                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        layoutFilter.visibility = View.GONE
                        btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                        isFilterOpen = false
                    }

                    if (!isEndofData && !searchTalkList.canScrollVertically(1)) {
                        isReload = true
                        ++page

                        mApiClient.getChatSearchResultList(
                                CHAT_SEARCH_RESULT_LIST_REQUEST,
                                keyword,
                                petKind,
                                categoryId,
                                chatOrder,
                                pageOffset,
                                page
                        )
                    }
                }

                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if (layoutManager is LinearLayoutManager) {
                        (layoutManager as LinearLayoutManager).apply {
                            val visibleCount = findFirstVisibleItemPosition()
                            if (visibleCount > 10) {
                                btnTop.visibility = View.VISIBLE
                            } else {
                                btnTop.visibility = View.GONE
                            }
                        }
                    }
                }
            })
        }

        keyword = SearchResultFragment.instance.getKeyword()
        Logger.d("keyword : $keyword")

        btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)

        imageViewPetAll.isSelected = true
        textViewPetAll.isSelected = true
        textViewPetAll.setTypeface(null, Typeface.BOLD)

        filterAll.isSelected = true
        filter = Helper.readStringRes(R.string.home_search_filter_all)

        mApiClient.getChatSearchResultList(
            CHAT_SEARCH_RESULT_LIST_REQUEST,
            keyword,
            petKind,
            categoryId,
            chatOrder,
            pageOffset,
            page
        )
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(CHAT_SEARCH_RESULT_LIST_REQUEST)) {
            mApiClient.cancelRequest(CHAT_SEARCH_RESULT_LIST_REQUEST)
        }

        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this)
        }

        super.onDestroyView()
    }

    fun refreshWithKeyword(newKeyword: String) {
        keyword = newKeyword
        page = 0
        isReload = false
        mApiClient.getChatSearchResultList(
                CHAT_SEARCH_RESULT_LIST_REQUEST,
                keyword,
                petKind,
                categoryId,
                chatOrder,
                pageOffset,
                page
        )
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
        when(response.requestTag) {
            CHAT_SEARCH_RESULT_LIST_REQUEST -> {
                if (response is ChatSearchResultListResponse) {
                    isEndofData = response.searchResultList.size < pageOffset

                    if (response.totalCount > 0) {
                        searchTalkList.visibility = View.VISIBLE
                        empty.visibility = View.GONE

                        searchCount.text = response.totalCount.toString()

                        if (!isReload) {
                            chatItems.clear()
                            chatItems.addAll(response.searchResultList)
                            mAdapter.notifyDataSetChanged()
                        } else {
                            for (item in response.searchResultList) {
                                chatItems.add(item)
                                mAdapter.notifyItemInserted(chatItems.size - 1)
                            }
                        }
                    } else {
                        searchTalkList.visibility = View.GONE
                        empty.apply {
                            visibility = View.VISIBLE
                            text =
                                "\'${keyword}\' ${Helper.readStringRes(R.string.chat_search_all_chat_empty)}"
                        }

                        searchCount.text = "0"
                    }
                }
            }
        }
    }

    fun onTalkItemClicked(item: LegacyChatItem) {
        startActivity(Intent(requireActivity(), ChatSearchDetailActivity::class.java).apply {
            putExtra("chatId", item.id)
        })
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

                mApiClient.getChatSearchResultList(
                    CHAT_SEARCH_RESULT_LIST_REQUEST,
                    keyword,
                    petKind,
                    getCategoryId(),
                    chatOrder,
                    pageOffset,
                    page
                )
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

                mApiClient.getChatSearchResultList(
                    CHAT_SEARCH_RESULT_LIST_REQUEST,
                    keyword,
                    petKind,
                    getCategoryId(),
                    chatOrder,
                    pageOffset,
                    page
                )
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

                mApiClient.getChatSearchResultList(
                    CHAT_SEARCH_RESULT_LIST_REQUEST,
                    keyword,
                    petKind,
                    getCategoryId(),
                    chatOrder,
                    pageOffset,
                    page
                )
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

                mApiClient.getChatSearchResultList(
                    CHAT_SEARCH_RESULT_LIST_REQUEST,
                    keyword,
                    petKind,
                    getCategoryId(),
                    chatOrder,
                    pageOffset,
                    page
                )
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

                mApiClient.getChatSearchResultList(
                    CHAT_SEARCH_RESULT_LIST_REQUEST,
                    keyword,
                    petKind,
                    getCategoryId(),
                    chatOrder,
                    pageOffset,
                    page
                )
            }

            R.id.filterSurgery -> {
                filter = Helper.readStringRes(R.string.home_search_filter_surgery)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_surgery)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)

                handleCategoryColor(it)

                page = 0
                chatOrder = "recommendCount"

                mApiClient.getChatSearchResultList(
                    CHAT_SEARCH_RESULT_LIST_REQUEST,
                    keyword,
                    petKind,
                    getCategoryId(),
                    chatOrder,
                    pageOffset,
                    page
                )
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

                mApiClient.getChatSearchResultList(
                    CHAT_SEARCH_RESULT_LIST_REQUEST,
                    keyword,
                    petKind,
                    getCategoryId(),
                    chatOrder,
                    pageOffset,
                    page
                )
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

                mApiClient.getChatSearchResultList(
                    CHAT_SEARCH_RESULT_LIST_REQUEST,
                    keyword,
                    petKind,
                    getCategoryId(),
                    chatOrder,
                    pageOffset,
                    page
                )
            }

            R.id.layoutPetAll -> {
                petKind = ""

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

                mApiClient.getChatSearchResultList(
                    CHAT_SEARCH_RESULT_LIST_REQUEST,
                    keyword,
                    petKind,
                    getCategoryId(),
                    chatOrder,
                    pageOffset,
                    page
                )
                isReload = false
            }

            R.id.layoutDog -> {
                petKind = "강아지"

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

                mApiClient.getChatSearchResultList(
                    CHAT_SEARCH_RESULT_LIST_REQUEST,
                    keyword,
                    petKind,
                    getCategoryId(),
                    chatOrder,
                    pageOffset,
                    page
                )
                isReload = false
            }

            R.id.layoutCat -> {
                petKind = "고양이"

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

                mApiClient.getChatSearchResultList(
                    CHAT_SEARCH_RESULT_LIST_REQUEST,
                    keyword,
                    petKind,
                    getCategoryId(),
                    chatOrder,
                    pageOffset,
                    page
                )
                isReload = false
            }

            R.id.layoutEtc -> {
                petKind = "특수동물"

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

                mApiClient.getChatSearchResultList(
                    CHAT_SEARCH_RESULT_LIST_REQUEST,
                    keyword,
                    petKind,
                    getCategoryId(),
                    chatOrder,
                    pageOffset,
                    page
                )
                isReload = false
            }
        }
    }

    //=================================================================================================
    inner class TalkAdapter : RecyclerView.Adapter<TalkHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            TalkHolder(layoutInflater.inflate(R.layout.adapter_search_talk_item, parent, false))

        override fun onBindViewHolder(holder: TalkHolder, position: Int) {
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

    inner class TalkHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setPetCategory(_text: String) {
            item.petFilter.text = _text
        }

        fun setDate(_text: String) {
            item.date.text = _text
        }

        fun setContent(_text: String) {
            context?.let {
                item.content.text = _text.highlight(it.getColor(R.color.orange), keyword)
            }
        }

        fun setRecommendCnt(_text: String) {
            item.recommendCount.text = "추천 ${_text}"
        }
    }
}