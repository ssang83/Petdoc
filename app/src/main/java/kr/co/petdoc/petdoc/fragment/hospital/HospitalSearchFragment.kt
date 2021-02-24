package kr.co.petdoc.petdoc.fragment.hospital

import android.content.Context
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
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.couchbase.lite.MutableDocument
import kotlinx.android.synthetic.main.adapter_hospital_search_result_item.view.*
import kotlinx.android.synthetic.main.adapter_recently_keyword_item.view.*
import kotlinx.android.synthetic.main.fragment_hospital_search.*
import kotlinx.android.synthetic.main.view_home_search_keyword.view.keyword
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.db.KeywordDBManager
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.HospitalKeywordResponse
import kr.co.petdoc.petdoc.network.response.HospitalListResponse
import kr.co.petdoc.petdoc.network.response.submodel.HospitalItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: HospitalSearchBeforeFragment
 * Created by kimjoonsung on 2020/06/11.
 *
 * Description :
 */
class HospitalSearchFragment : BaseFragment() {

    companion object {
        private const val KEYWORD_ID = 1
    }

    private val TAG = "HospitalSearchBeforeFragment"
    private val RECOMMEND_KEYWORD_LIST_REQUEST = "${TAG}.recommendKeywordListRequest"
    private val SEARCH_RESULT_LIST_REQUEST = "${TAG}.searchResultListRequest"

    private lateinit var dataModel: HospitalDataModel
    private lateinit var keywordAdapter:KeywordAdapter
    private val keywordItems: MutableList<KeywordItem> = mutableListOf()

    private lateinit var resultAdapter:ResultAdapter
    private val resultItems:MutableList<HospitalItem> = mutableListOf()

    private var page = 1
    private val size = 50

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_hospital_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        dataModel.searchMode.value = true

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        inputDelete.setOnClickListener { editSearch.setText("") }
        btnCancel.setOnClickListener { requireActivity().onBackPressed() }
        btnDeleteAll.setOnClickListener { deleteKeywordAll() }

        editSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard(editSearch)
                    if (resultItems.size > 0) {
                        saveKeyword(editSearch.text.toString())
                        dataModel.hospitalItem.value = resultItems[0]
                        requireActivity().onBackPressed()
                    }
                }

                return true
            }
        })

        editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! > 0) {
                    layoutDel.visibility = View.VISIBLE
                    resultLayout.visibility = View.VISIBLE

                    mApiClient.getHomeHospitalList(
                        SEARCH_RESULT_LIST_REQUEST,
                        dataModel.currentLat.value!!.toString(),
                        dataModel.currentLng.value!!.toString(),
                        s.toString(),
                        dataModel.workingYn.value!!.toString(),
                        dataModel.registerYn.value!!.toString(),
                        dataModel.bookingYn.value!!.toString(),
                        dataModel.beautyYn.value!!.toString(),
                        dataModel.hotelYn.value!!.toString(),
                        dataModel.allDayYn.value!!.toString(),
                        dataModel.parkingYn.value!!.toString(),
                        "",
                        dataModel.animalIdList.value!!,
                        size,
                        page
                    )
                } else {
                    layoutDel.visibility = View.GONE
                    resultLayout.visibility = View.GONE
                }
            }
        })

        //==========================================================================================
        keywordAdapter = KeywordAdapter()
        recentlyKeywordList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = keywordAdapter
        }

        resultAdapter = ResultAdapter()
        resultList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultAdapter
        }

        //==========================================================================================
        mApiClient.getHospitalKeyword(RECOMMEND_KEYWORD_LIST_REQUEST)

        loadKeyword()
        showKeyBoardOnView(editSearch)
        editSearch.requestFocus()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(RECOMMEND_KEYWORD_LIST_REQUEST)) {
            mApiClient.cancelRequest(RECOMMEND_KEYWORD_LIST_REQUEST)
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
            RECOMMEND_KEYWORD_LIST_REQUEST -> {
                if (response is HospitalKeywordResponse) {
                    setKeywordList(response.keywordList)
                }
            }

            SEARCH_RESULT_LIST_REQUEST -> {
                if (response is HospitalListResponse) {
                    if (response.resultData.center.size > 0) {
                        resultList.visibility = View.VISIBLE
                        emptyList.visibility = View.GONE

                        resultItems.clear()
                        resultItems.addAll(response.resultData.center)
                        resultAdapter.notifyDataSetChanged()
                    } else {
                        resultList.visibility = View.GONE
                        emptyList.visibility = View.VISIBLE
                    }
                }
            }
        }
    }

    //==============================================================================================
    private fun setKeywordList(keywords: List<String>) {
        recommendKeywords.removeAllViews()
        for (i in 0 until keywords.size) {
            recommendKeywords.addView(
                KeywordTag(requireContext(), keywords[i])
            )
        }
    }

    private fun keywordClicked(keyword: String) {
        hideKeyboard(editSearch)
        editSearch.setText(keyword)
        saveKeyword(keyword)
    }

    private fun saveKeyword(keyword: String) {
        var nextKeywordId = KEYWORD_ID
        var tempId = -1

        KeywordDBManager.getInstance(requireContext()).loadAllExt()!!.forEach {
            if (keyword == it.getDictionary(KeywordDBManager.KEY).getString("keyword")) {
                nextKeywordId = it.getDictionary(KeywordDBManager.KEY).getString("id").toInt()
                return@forEach
            }

            tempId = it.getDictionary(KeywordDBManager.KEY).getString("id").toInt()
            if (nextKeywordId <= tempId) {
                nextKeywordId = tempId + 1
            }
        }

        if (tempId == -1) {
            nextKeywordId = KEYWORD_ID
        }

        Logger.d("keyword id : $nextKeywordId")
        val document = MutableDocument(nextKeywordId.toString()).apply {
            setString("id", nextKeywordId.toString())
            setString("keyword", keyword)
            setString(
                "reg_date",
                SimpleDateFormat("MM-dd").format(Date(System.currentTimeMillis()))
            )
        }

        Logger.e(
            "keywordID : $nextKeywordId, keyword:$keyword, date:${SimpleDateFormat("MM-dd").format(
                Date(System.currentTimeMillis())
            )}"
        )

        KeywordDBManager.getInstance(requireContext()).put(document)
    }

    private fun loadKeyword() {
        keywordItems.clear()

        KeywordDBManager.getInstance(requireContext()).loadAllByOrder()!!.forEach {
            Logger.d("keyword id :${it.getDictionary(KeywordDBManager.KEY).getString("id")}")
            keywordItems.add(
                KeywordItem(
                    it.getDictionary(KeywordDBManager.KEY).getString("id"),
                    it.getDictionary(KeywordDBManager.KEY).getString("keyword"),
                    it.getDictionary(KeywordDBManager.KEY).getString("reg_date")
                )
            )
        }

        Logger.d("keywords : ${keywordItems.size}")
        if (keywordItems.size > 0) {
            recentlyKeywordLayout.visibility = View.VISIBLE
            keywordAdapter.notifyDataSetChanged()
        } else {
            recentlyKeywordLayout.visibility = View.GONE
        }
    }

    private fun deleteKeyword(item:KeywordItem, position: Int) {
        val document = KeywordDBManager.getInstance(requireContext()).getDoc(item.id)
        KeywordDBManager.getInstance(requireContext()).delete(document)
        keywordItems.removeAt(position)
        keywordAdapter.notifyDataSetChanged()

        if(keywordItems.size == 0) {
            recentlyKeywordLayout.visibility = View.GONE
        }
    }

    private fun deleteKeywordAll() {
        KeywordDBManager.getInstance(requireContext()).deleteAll()
        keywordItems.clear()
        keywordAdapter.notifyDataSetChanged()

        recentlyKeywordLayout.visibility = View.GONE
    }

    private fun onSearchItemClicked(item: HospitalItem) {
        dataModel.hospitalItem.value = item

        saveKeyword(item.name)
        requireActivity().onBackPressed()
    }

    //==============================================================================================

    inner class KeywordTag(context: Context, keyword: String) : FrameLayout(context) {

        init {
            val view = layoutInflater.inflate(R.layout.view_home_search_keyword, null, false)

            view.keyword.text = keyword
            view.keyword.setOnClickListener { keywordClicked(keyword) }

            addView(view)
        }
    }

    //==============================================================================================
    inner class KeywordAdapter : RecyclerView.Adapter<KeywordHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            KeywordHolder(layoutInflater.inflate(R.layout.adapter_recently_keyword_item, parent, false))

        override fun onBindViewHolder(holder: KeywordHolder, position: Int) {
            holder.setKeyword(keywordItems[position].keyword)

            holder.itemView.btnDel.setOnClickListener {
                deleteKeyword(keywordItems[position], position)
            }
        }

        override fun getItemCount() = keywordItems.size
    }

    inner class KeywordHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setKeyword(_keyword: String) {
            item.keyword.text = _keyword
        }
    }

    //==============================================================================================
    inner class ResultAdapter : RecyclerView.Adapter<ResultHolder>() {
        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) =
            ResultHolder(layoutInflater.inflate(R.layout.adapter_hospital_search_result_item, parent, false))

        override fun onBindViewHolder(holder: ResultHolder, position: Int) {
            holder.setName(resultItems[position].name)
            holder.setLocation(resultItems[position].location)
            holder.setDistance(resultItems[position].distance)

            holder.itemView.setOnClickListener { onSearchItemClicked(resultItems[position]) }
        }

        override fun getItemCount() = resultItems.size
    }

    inner class ResultHolder(var item: View) : RecyclerView.ViewHolder(item) {
        fun setName(_name: String) {
            item.name.text = _name
        }

        fun setLocation(_addr: String) {
            item.location.text = _addr
        }

        fun setDistance(_distance: Int) {
            item.distance.apply {
                when{
                    _distance < 1000 -> text = "${_distance}m"
                    else -> {
                        val kilometer:Float = _distance.toFloat() / 1000
                        val distnce = String.format("%.1f km", kilometer)
                        text = distnce
                    }
                }
            }
        }
    }
}

data class KeywordItem(val id:String, val keyword:String, val regDate: String)