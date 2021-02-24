package kr.co.petdoc.petdoc.activity.life

import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.KeyEvent
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.TextView
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CenterCrop
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import kotlinx.android.synthetic.main.activity_life_magazine.*
import kotlinx.android.synthetic.main.adapter_life_magazine_item.view.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.activity.MagazineDetailActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.LifeMagazineSearchListResponse
import kr.co.petdoc.petdoc.network.response.MagazineListRespone
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.MagazineItem
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: LifeMagazineActivity
 * Created by kimjoonsung on 2020/06/03.
 *
 * Description :
 */
class LifeMagazineActivity : BaseActivity() {

    private val TAG = "LifeMagazineActivity"
    private val MAGAZIME_LIST_REQUEST = "${TAG}.magazineListRequest"
    private val MAGAZIME_SEARCH_LIST_REQUEST = "${TAG}.magazineSearchListRequest"
    private val MAGAZIME_TOTAL_COUNT_REQUEST = "${TAG}.magazineTotalCountRequest"

    private lateinit var magazineAdapter:MagazineAdapter
    private var magazineItems:MutableList<MagazineItem> = mutableListOf()

    private var page = 0
    private val pageOffset = 100
    private var isEndofData = false
    private var order = "recommendCount"

    private var isReload = false
    private var isFilterOpen = false
    private var filter = ""
    private var pageTiggerPoint = Int.MAX_VALUE

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_life_magazine)
        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        btnTop.setOnClickListener { magazineList.scrollToPosition(0) }
        btnBack.setOnClickListener { onBackPressed() }

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

        editSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard(editSearch)

                    isReload = false
                    page = 0
                    order = "recommendCount"

                    mApiClient.getLifeMagazineSearchList(
                        MAGAZIME_SEARCH_LIST_REQUEST,
                        editSearch.text.toString(),
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )
                }

                return true
            }
        })

        //==========================================================================================
        magazineAdapter = MagazineAdapter()
        magazineList.apply {
            adapter = magazineAdapter
            layoutManager = GridLayoutManager(this@LifeMagazineActivity, 2, LinearLayoutManager.VERTICAL, false)
            addItemDecoration(MagazineDecoration())

            addOnScrollListener(object:RecyclerView.OnScrollListener(){
                override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                    super.onScrolled(recyclerView, dx, dy)
                    if(layoutManager is GridLayoutManager){
                        (layoutManager as GridLayoutManager).apply{
                            val visibleCount = findFirstVisibleItemPosition()
                            if (visibleCount > 10) {
                                btnTop.visibility = View.VISIBLE
                            } else {
                                btnTop.visibility = View.GONE
                            }

                            val lastItem = findLastVisibleItemPosition()
                            synchronized(pageTiggerPoint) {
                                if (!isEndofData && lastItem > pageTiggerPoint) {
                                    isReload = true
                                    pageTiggerPoint = Int.MAX_VALUE

                                    if (editSearch.text.toString().isEmpty()) {
                                        mApiClient.getLifeMagazineList(
                                            MAGAZIME_LIST_REQUEST,
                                            getCategoryId(),
                                            order,
                                            pageOffset,
                                            page * pageOffset
                                        )
                                    } else {
                                        mApiClient.getLifeMagazineSearchList(
                                            MAGAZIME_SEARCH_LIST_REQUEST,
                                            editSearch.text.toString(),
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

        //==========================================================================================
        btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)
        filterAll.isSelected = true
        filter = Helper.readStringRes(R.string.home_search_filter_all)

        retryIfNetworkAbsent {
            mApiClient.getMagazineTotalCount(MAGAZIME_TOTAL_COUNT_REQUEST, getCategoryId())
            mApiClient.getLifeMagazineList(MAGAZIME_LIST_REQUEST, getCategoryId(), order, pageOffset, page*pageOffset)
        }
    }

    override fun onDestroy() {
        if (mApiClient.isRequestRunning(MAGAZIME_LIST_REQUEST)) {
            mApiClient.cancelRequest(MAGAZIME_LIST_REQUEST)
        }

        if (mApiClient.isRequestRunning(MAGAZIME_TOTAL_COUNT_REQUEST)) {
            mApiClient.cancelRequest(MAGAZIME_TOTAL_COUNT_REQUEST)
        }
        super.onDestroy()
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
     * @param data response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(data: NetworkBusResponse) {
        when (data.tag) {
            MAGAZIME_TOTAL_COUNT_REQUEST -> {
                if ("ok" == data.status) {
                    try {
                        val code = JSONObject(data.response)["code"]
                        if ("SUCCESS" == code) {
                            val count = JSONObject(data.response)["resultData"]
                            searchCount.text = count.toString()
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            MAGAZIME_SEARCH_LIST_REQUEST -> {
                if (response is LifeMagazineSearchListResponse) {
                    if (response.resultData.totalCount > 0) {
                        magazineList.visibility = View.VISIBLE
                        layoutEmpty.visibility = View.GONE

                        searchCount.text = response.resultData.totalCount.toString()

                        if (!isReload) {
                            magazineItems.clear()
                            magazineItems.addAll(response.resultData.data)
                            magazineAdapter.notifyDataSetChanged()
                        } else {
                            for (item in response.resultData.data) {
                                magazineItems.add(item)
                                magazineAdapter.notifyItemInserted(magazineItems.size - 1)
                            }
                        }

                        ++page
                        pageTiggerPoint = magazineItems.size - 4
                    } else {
                        magazineList.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE

                        searchCount.text = response.resultData.totalCount.toString()
                    }
                }
            }

            MAGAZIME_LIST_REQUEST -> {
                if (response is MagazineListRespone) {
                    isEndofData = response.resultData.size < pageOffset

                    if (response.resultData.size > 0) {
                        magazineList.visibility = View.VISIBLE
                        layoutEmpty.visibility = View.GONE

                        if (!isReload) {
                            magazineItems.clear()
                            magazineItems.addAll(response.resultData)
                            magazineAdapter.notifyDataSetChanged()
                        } else {
                            for (item in response.resultData) {
                                magazineItems.add(item)
                                magazineAdapter.notifyItemInserted(magazineItems.size - 1)
                            }
                        }

                        ++page
                        pageTiggerPoint = magazineItems.size - 4
                    } else {
                        magazineList.visibility = View.GONE
                        layoutEmpty.visibility = View.VISIBLE
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
        FirebaseAPI(this).logEventFirebase("반려백과_반려백과상세", "Click Event", "반려백과 더보기 화면에서 상세 페이지를 클릭")
        startActivity(Intent(this, MagazineDetailActivity::class.java).apply {
            putExtra("id", item.id)
        })
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

                isReload = false
                page = 0
                order = "recommendCount"

                if (editSearch.text.toString().isEmpty()) {
                    mApiClient.getLifeMagazineList(
                        MAGAZIME_LIST_REQUEST,
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )

                    mApiClient.getMagazineTotalCount(MAGAZIME_TOTAL_COUNT_REQUEST, getCategoryId())
                } else {
                    mApiClient.getLifeMagazineSearchList(
                        MAGAZIME_SEARCH_LIST_REQUEST,
                        editSearch.text.toString(),
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )
                }
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

                if (editSearch.text.toString().isEmpty()) {
                    mApiClient.getLifeMagazineList(
                        MAGAZIME_LIST_REQUEST,
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )

                    mApiClient.getMagazineTotalCount(MAGAZIME_TOTAL_COUNT_REQUEST, getCategoryId())
                } else {
                    mApiClient.getLifeMagazineSearchList(
                        MAGAZIME_SEARCH_LIST_REQUEST,
                        editSearch.text.toString(),
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )
                }
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

                if (editSearch.text.toString().isEmpty()) {
                    mApiClient.getLifeMagazineList(
                        MAGAZIME_LIST_REQUEST,
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )

                    mApiClient.getMagazineTotalCount(MAGAZIME_TOTAL_COUNT_REQUEST, getCategoryId())
                } else {
                    mApiClient.getLifeMagazineSearchList(
                        MAGAZIME_SEARCH_LIST_REQUEST,
                        editSearch.text.toString(),
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )
                }
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

                if (editSearch.text.toString().isEmpty()) {
                    mApiClient.getLifeMagazineList(
                        MAGAZIME_LIST_REQUEST,
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )

                    mApiClient.getMagazineTotalCount(MAGAZIME_TOTAL_COUNT_REQUEST, getCategoryId())
                } else {
                    mApiClient.getLifeMagazineSearchList(
                        MAGAZIME_SEARCH_LIST_REQUEST,
                        editSearch.text.toString(),
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )
                }
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

                if (editSearch.text.toString().isEmpty()) {
                    mApiClient.getLifeMagazineList(
                        MAGAZIME_LIST_REQUEST,
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )

                    mApiClient.getMagazineTotalCount(MAGAZIME_TOTAL_COUNT_REQUEST, getCategoryId())
                } else {
                    mApiClient.getLifeMagazineSearchList(
                        MAGAZIME_SEARCH_LIST_REQUEST,
                        editSearch.text.toString(),
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )
                }
            }

            R.id.filterSurgery -> {
                filter = Helper.readStringRes(R.string.home_search_filter_surgery)
                filterTitle.text = Helper.readStringRes(R.string.home_search_filter_surgery)
                layoutFilter.visibility = View.GONE
                btnCategoryFilter.setBackgroundResource(R.drawable.ic_filter_open)

                handleCategoryColor(it)

                isReload = false
                page = 0
                order = "recommendCount"

                if (editSearch.text.toString().isEmpty()) {
                    mApiClient.getLifeMagazineList(
                        MAGAZIME_LIST_REQUEST,
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )

                    mApiClient.getMagazineTotalCount(MAGAZIME_TOTAL_COUNT_REQUEST, getCategoryId())
                } else {
                    mApiClient.getLifeMagazineSearchList(
                        MAGAZIME_SEARCH_LIST_REQUEST,
                        editSearch.text.toString(),
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )
                }
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

                if (editSearch.text.toString().isEmpty()) {
                    mApiClient.getLifeMagazineList(
                        MAGAZIME_LIST_REQUEST,
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )

                    mApiClient.getMagazineTotalCount(MAGAZIME_TOTAL_COUNT_REQUEST, getCategoryId())
                } else {
                    mApiClient.getLifeMagazineSearchList(
                        MAGAZIME_SEARCH_LIST_REQUEST,
                        editSearch.text.toString(),
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )
                }
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

                if (editSearch.text.toString().isEmpty()) {
                    mApiClient.getLifeMagazineList(
                        MAGAZIME_LIST_REQUEST,
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )

                    mApiClient.getMagazineTotalCount(MAGAZIME_TOTAL_COUNT_REQUEST, getCategoryId())
                } else {
                    mApiClient.getLifeMagazineSearchList(
                        MAGAZIME_SEARCH_LIST_REQUEST,
                        editSearch.text.toString(),
                        getCategoryId(),
                        order,
                        pageOffset,
                        page * pageOffset
                    )
                }
            }
        }
    }

    //==============================================================================================
    inner class MagazineAdapter : RecyclerView.Adapter<MagazineHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            MagazineHolder(layoutInflater.inflate(R.layout.adapter_life_magazine_item, parent, false))

        override fun onBindViewHolder(holder: MagazineHolder, position: Int) {
            holder.setCategoryText(magazineItems[position].categoryNm)
            holder.setContext(magazineItems[position].title)
            holder.setImage(magazineItems[position].titleImage)
            holder.setLikeCount(magazineItems[position].likeCount.toString())

            holder.itemView.setOnClickListener { onItemClicked(magazineItems[position]) }
        }

        override fun getItemCount() = magazineItems.size
    }

    inner class MagazineHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setCategoryText(_text : String){
            item.category.text = _text
        }

        fun setContext(_text : String){
            item.title.text = _text
        }

        fun setImage(url:String){
            Glide.with(this@LifeMagazineActivity)
                .load( if(url.startsWith("http")) url else "${AppConstants.IMAGE_URL}$url")
                .apply(RequestOptions().transform(CenterCrop(), RoundedCorners(20)))
                .into(item.magazineImg)
        }

        fun setLikeCount(_count: String) {
            item.likeCnt.text = _count
        }
    }

    //==============================================================================================
    // =============================================================================================================
    inner class MagazineDecoration : RecyclerView.ItemDecoration() {

        var space = 0

        init{
            space = Helper.convertDPResourceToPx(this@LifeMagazineActivity,R.dimen.recommend_text_item_gaps).toInt()
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