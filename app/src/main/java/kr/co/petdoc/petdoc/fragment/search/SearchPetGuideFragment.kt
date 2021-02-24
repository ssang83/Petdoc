package kr.co.petdoc.petdoc.fragment.search

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.adapter_magazine_item.view.*
import kotlinx.android.synthetic.main.fragment_search_pet_guide.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MagazineDetailActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.ApiClient
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.LifeMagazineSearchListResponse
import kr.co.petdoc.petdoc.network.response.submodel.MagazineItem
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: SearchPetGuideFragment
 * Created by kimjoonsung on 2020/04/22.
 *
 * Description :
 */
class SearchPetGuideFragment : Fragment() {

    private val TAG = "SearchPetGuideFragment"
    private val MAGAZIME_SEARCH_LIST_REQUEST = "${TAG}.magazineSearchListRequest"

    private lateinit var mAdapter: PetGuideAdapter
    private var petGuideBox = mutableListOf<MagazineItem>()

    private var page = 0
    private val pageOffset = 20
    private var order = "recommendCount"

    private var isReload = false
    private var isFilterOpen = false
    private var filter = ""
    private var pageTiggerPoint = Int.MAX_VALUE

    private var keyword = ""
    private var margin16 = 0

    private val mApiClient: ApiClient = PetdocApplication.application.apiClient
    private lateinit var mEventBus: EventBus

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_search_pet_guide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mEventBus = EventBus.getDefault()
        if(!mEventBus.isRegistered(this)) {
            mEventBus.register(this)
        }

        Airbridge.trackEvent("search", "click", "result_백과", null, null, null)

        margin16 = Helper.convertDpToPx(requireContext(), 16f).toInt()

        keyword = SearchResultFragment.instance.getKeyword()
        Logger.d("keyword : $keyword")

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

        btnTop.setOnClickListener { searchPetGuideList.scrollToPosition(0) }

        mAdapter = PetGuideAdapter()
        searchPetGuideList.apply {
            adapter = mAdapter
            layoutManager = GridLayoutManager(requireContext(), 2, LinearLayoutManager.VERTICAL, false)
//            addItemDecoration(PetGuideDecoration())

            addOnScrollListener(object:RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if(layoutManager is GridLayoutManager){
                        (layoutManager as GridLayoutManager).apply{
                            val lastItem = findLastVisibleItemPosition()
                            val visibleCount = findFirstVisibleItemPosition()
                            if (visibleCount > 10) {
                                btnTop.visibility = View.VISIBLE
                            } else {
                                btnTop.visibility = View.GONE
                            }

                            synchronized(pageTiggerPoint) {
                                if (lastItem > pageTiggerPoint) {
                                    isReload = true
                                    pageTiggerPoint = Int.MAX_VALUE

                                    mApiClient.getLifeMagazineSearchList(
                                        MAGAZIME_SEARCH_LIST_REQUEST,
                                        keyword,
                                        getCategoryId(),
                                        order,
                                        pageOffset,
                                        page * pageOffset
                                    )
                                }
                            }
                        }
                    }
                }

                override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                    super.onScrollStateChanged(recyclerView, newState)
                    if (newState == RecyclerView.SCROLL_STATE_DRAGGING) {
                        layoutFilter.visibility = View.GONE
                        btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                        isFilterOpen = false
                    }
                }
            })
        }

        btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
        filterAll.isSelected = true
        filter = Helper.readStringRes(R.string.home_search_filter_all)

        mApiClient.getLifeMagazineSearchList(
            MAGAZIME_SEARCH_LIST_REQUEST,
            keyword,
            getCategoryId(),
            order,
            pageOffset,
            page * pageOffset
        )
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(MAGAZIME_SEARCH_LIST_REQUEST)) {
            mApiClient.cancelRequest(MAGAZIME_SEARCH_LIST_REQUEST)
        }

        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this)
        }

        super.onDestroyView()
    }

    fun refreshWithKeyword(newKeyword: String) {
        if (isAdded) {
            page = 0
            isReload = false
            keyword = newKeyword
            mApiClient.getLifeMagazineSearchList(
                    MAGAZIME_SEARCH_LIST_REQUEST,
                    keyword,
                    getCategoryId(),
                    order,
                    pageOffset,
                    page * pageOffset
            )
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
        when(response.requestTag) {
            MAGAZIME_SEARCH_LIST_REQUEST -> {
                if (response is LifeMagazineSearchListResponse) {
                    if (response.resultData.data.size > 0) {
                        searchPetGuideList.visibility = View.VISIBLE
                        layoutEmpty.visibility = View.GONE

                        searchCount.text = response.resultData.totalCount.toString()

                        if (!isReload) {
                            petGuideBox.clear()
                            petGuideBox.addAll(response.resultData.data)
                            mAdapter.notifyDataSetChanged()
                        } else {
                            for (item in response.resultData.data) {
                                petGuideBox.add(item)
                                mAdapter.notifyItemInserted(petGuideBox.size - 1)
                            }
                        }

                        ++page
                        pageTiggerPoint = petGuideBox.size - 4
                    } else if (page < 1) {
                        searchPetGuideList.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE

                        searchCount.text = response.resultData.totalCount.toString()
                        emptyText.text = "${keyword}에 대한 검색 내용이 없습니다."
                    }
                }
            }
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

    private fun onItemClicked(item: MagazineItem) {
        startActivity(Intent(requireActivity(), MagazineDetailActivity::class.java).apply {
            putExtra("id", item.id)
        })
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

                isReload = false
                page = 0
                order = "recommendCount"

                mApiClient.getLifeMagazineSearchList(
                    MAGAZIME_SEARCH_LIST_REQUEST,
                    keyword,
                    getCategoryId(),
                    order,
                    pageOffset,
                    page * pageOffset
                )
            }

            R.id.filterAction -> {
                filter = Helper.readStringRes(R.string.home_search_filter_action)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_action)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                isReload = false
                page = 0
                order = "recommendCount"

                mApiClient.getLifeMagazineSearchList(
                    MAGAZIME_SEARCH_LIST_REQUEST,
                    keyword,
                    getCategoryId(),
                    order,
                    pageOffset,
                    page * pageOffset
                )
            }

            R.id.filterFeed -> {
                filter = Helper.readStringRes(R.string.home_search_filter_feed)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_feed)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                isReload = false
                page = 0
                order = "recommendCount"

                mApiClient.getLifeMagazineSearchList(
                    MAGAZIME_SEARCH_LIST_REQUEST,
                    keyword,
                    getCategoryId(),
                    order,
                    pageOffset,
                    page * pageOffset
                )
            }

            R.id.filterDisease -> {
                filter = Helper.readStringRes(R.string.home_search_filter_disease)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_disease)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                isReload = false
                page = 0
                order = "recommendCount"

                mApiClient.getLifeMagazineSearchList(
                    MAGAZIME_SEARCH_LIST_REQUEST,
                    keyword,
                    getCategoryId(),
                    order,
                    pageOffset,
                    page * pageOffset
                )
            }

            R.id.filterOriental -> {
                filter = Helper.readStringRes(R.string.home_search_filter_oriental)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_oriental)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                isReload = false
                page = 0
                order = "recommendCount"

                mApiClient.getLifeMagazineSearchList(
                    MAGAZIME_SEARCH_LIST_REQUEST,
                    keyword,
                    getCategoryId(),
                    order,
                    pageOffset,
                    page * pageOffset
                )
            }

            R.id.filterSurgery -> {
                filter = Helper.readStringRes(R.string.home_search_filter_surgery)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_surgery)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                isReload = false
                page = 0
                order = "recommendCount"

                mApiClient.getLifeMagazineSearchList(
                    MAGAZIME_SEARCH_LIST_REQUEST,
                    keyword,
                    getCategoryId(),
                    order,
                    pageOffset,
                    page * pageOffset
                )
            }

            R.id.filterVaccine -> {
                filter = Helper.readStringRes(R.string.home_search_filter_vaccine)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_vaccine)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                isReload = false
                page = 0
                order = "recommendCount"

                mApiClient.getLifeMagazineSearchList(
                    MAGAZIME_SEARCH_LIST_REQUEST,
                    keyword,
                    getCategoryId(),
                    order,
                    pageOffset,
                    page * pageOffset
                )
            }

            R.id.filterEtc -> {
                filter = Helper.readStringRes(R.string.home_search_filter_etc)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_etc)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
                isFilterOpen = false

                handleCategoryColor(it)

                isReload = false
                page = 0
                order = "recommendCount"

                mApiClient.getLifeMagazineSearchList(
                    MAGAZIME_SEARCH_LIST_REQUEST,
                    keyword,
                    getCategoryId(),
                    order,
                    pageOffset,
                    page * pageOffset
                )
            }
        }
    }

    //=============================================================================================
    inner class PetGuideAdapter : RecyclerView.Adapter<PetGuideHolder>() {

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            PetGuideHolder(layoutInflater.inflate(R.layout.adapter_magazine_item, parent, false))

        override fun onBindViewHolder(holder: PetGuideHolder, position: Int) {
            if (position == 0) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    topMargin = margin16
                }
            } else if (position == 1) {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    topMargin = margin16
                }
            } else {
                (holder.itemView.root.layoutParams as RecyclerView.LayoutParams).apply {
                    topMargin = 0
                }
            }

            holder.setCategoryText(petGuideBox[position].categoryNm)
            holder.setContext(petGuideBox[position].title)
            holder.setImage(petGuideBox[position].titleImage)
            holder.setLikeCount(petGuideBox[position].viewCount.toString())

            holder.itemView.setOnClickListener { onItemClicked(petGuideBox[position]) }
        }

        override fun getItemCount() = petGuideBox.size
    }

    inner class PetGuideHolder(var item: View) : RecyclerView.ViewHolder(item) {

        fun setCategoryText(_text : String){
            item.category.text = _text
        }

        fun setContext(_text : String){
            item.magazineTitle.text = _text
        }

        fun setImage(url:String){
            Glide.with(requireContext())
                .load( if(url.startsWith("http")) url else "${AppConstants.IMAGE_URL}$url")
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                .into(item.magazineImg)
        }

        fun setLikeCount(_count: String) {
            item.likeCnt.text = _count
        }
    }

    // =============================================================================================================
    inner class PetGuideDecoration : RecyclerView.ItemDecoration() {

        var space = 0

        init{
            space = Helper.convertDPResourceToPx(requireContext(),R.dimen.home_text_gap2).toInt()
        }

        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            //super.getItemOffsets(outRect, view, parent, state)
            outRect.left = space
            outRect.right = space
        }
    }
}