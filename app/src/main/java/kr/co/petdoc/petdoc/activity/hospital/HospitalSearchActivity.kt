package kr.co.petdoc.petdoc.activity.hospital

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import co.ab180.airbridge.Airbridge
import com.couchbase.lite.MutableDocument
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_hospital_search.*
import kotlinx.android.synthetic.main.adapter_hospital_search_result_item.view.*
import kotlinx.android.synthetic.main.adapter_recently_keyword_item.view.*
import kotlinx.android.synthetic.main.view_home_search_keyword.view.keyword
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.db.KeywordDBManager
import kr.co.petdoc.petdoc.event.HospitalSearchEvent
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.HospitalKeywordResponse
import kr.co.petdoc.petdoc.network.response.submodel.HospitalItem
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import kr.co.petdoc.petdoc.repository.network.form.HospitalListForm
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: HospitalSearchActivity
 * Created by kimjoonsung on 2020/07/21.
 *
 * Description :
 */
class HospitalSearchActivity : BaseActivity() {

    companion object {
        private const val KEYWORD_ID = 1
    }

    private lateinit var keywordAdapter: KeywordAdapter
    private val keywordItems: MutableList<KeywordItem> = mutableListOf()

    private lateinit var resultAdapter: ResultAdapter
    private val resultItems:MutableList<HospitalItem> = mutableListOf()

    private var currentLat = 0.0
    private var currentLng = 0.0
    private var petIdList:MutableList<Int> = mutableListOf()

    private var page = 1
    private val size = 50
    private var keyword = ""

    private val petdocRepository: PetdocRepository by inject()
    private val apiService:PetdocApiService by inject()

    val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_hospital_search)
        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        currentLat = PetdocApplication.currentLat
        currentLng = PetdocApplication.currentLng

        val items = StorageUtils.loadValueFromPreference(this, AppConstants.PREF_KEY_CLINIC_PET_STATUS, "")
        if (items.isNotEmpty()) {
            petIdList = Gson().fromJson(items, Array<Int>::class.java).toMutableList()
        }

        btnBack.setOnClickListener { finish() }
        inputDelete.setOnClickListener { editSearch.setText("") }
        btnCancel.setOnClickListener { finish() }
        btnDeleteAll.setOnClickListener { deleteKeywordAll() }

        editSearch.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                hideKeyboard(editSearch)
                val searchKeyword = editSearch.text.toString().trim()
                if (searchKeyword.isNotBlank()) {
                    saveKeyword(searchKeyword)
                }
                if (resultItems.size > 0) {
                    EventBus.getDefault()
                        .post(HospitalSearchEvent(resultItems[0], true, editSearch.text.toString()))
                    finish()
                }
            }

            true
        }

        editSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! > 0) {
                    layoutDel.visibility = View.VISIBLE
                    resultLayout.visibility = View.VISIBLE

                    Airbridge.trackEvent("booking", "click", "search_done", null, null, null)

                    keyword = s.toString()
                    loadHospitalList()
                } else {
                    layoutDel.visibility = View.GONE
                    resultLayout.visibility = View.GONE
                }
            }
        })

        //==========================================================================================
        keywordAdapter = KeywordAdapter()
        recentlyKeywordList.apply {
            layoutManager = LinearLayoutManager(this@HospitalSearchActivity)
            adapter = keywordAdapter
        }

        resultAdapter = ResultAdapter()
        resultList.apply {
            layoutManager = LinearLayoutManager(this@HospitalSearchActivity)
            adapter = resultAdapter
        }

        //==========================================================================================
        loadKeyword()
        loadRecommendKeyword()
        showKeyBoardOnView(editSearch)
    }

    //==============================================================================================
    private fun setKeywordList(keywords: List<String>) {
        recommendKeywords.removeAllViews()
        for (i in 0 until keywords.size) {
            recommendKeywords.addView(
                KeywordTag(this, keywords[i])
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

        run loop@{
            KeywordDBManager.getInstance(this).loadAllExt()!!.forEach {
                Logger.d("keyword : ${it.getDictionary(KeywordDBManager.KEY).getString("keyword")}")
                if (keyword == it.getDictionary(KeywordDBManager.KEY).getString("keyword")) {
                    nextKeywordId = it.getDictionary(KeywordDBManager.KEY).getString("id").toInt()
                    return@loop
                }

                tempId = it.getDictionary(KeywordDBManager.KEY).getString("id").toInt()
                if (nextKeywordId <= tempId) {
                    nextKeywordId = tempId + 1
                }
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

        KeywordDBManager.getInstance(this).put(document)
    }

    private fun loadKeyword() {
        keywordItems.clear()

        KeywordDBManager.getInstance(this).loadAllByOrder()!!.forEach {
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

    private fun loadRecommendKeyword() {
        lifecycleScope.launch(Dispatchers.IO + exceptionHandler) {
            val response = apiService.getHospitalSearchKeyword()
            setKeywordList(response.keywordList)
        }
    }

    private fun deleteKeyword(item:KeywordItem, position: Int) {
        val document = KeywordDBManager.getInstance(this).getDoc(item.id)
        KeywordDBManager.getInstance(this).delete(document)
        keywordItems.removeAt(position)
        keywordAdapter.notifyDataSetChanged()

        if(keywordItems.size == 0) {
            recentlyKeywordLayout.visibility = View.GONE
        }
    }

    private fun deleteKeywordAll() {
        KeywordDBManager.getInstance(this).deleteAll()
        keywordItems.clear()
        keywordAdapter.notifyDataSetChanged()

        recentlyKeywordLayout.visibility = View.GONE
    }

    private fun onSearchItemClicked(item: HospitalItem) {
        Airbridge.trackEvent("booking", "click", "search_result", null, null, null)
        EventBus.getDefault().post(HospitalSearchEvent(item, true, editSearch.text.toString()))

        saveKeyword(editSearch.text.toString())
        finish()
    }

    private fun onKeywordItemClicked(item: KeywordItem) {
        saveKeyword(item.keyword)
        editSearch.setText(item.keyword)
        hideKeyboard(editSearch)
        keyword = item.keyword

        loadHospitalList()
    }

    private fun loadHospitalList() {
        lifecycleScope.launch {
            try {
                val response = petdocRepository.fetchHospitalList(
                        HospitalListForm(
                                ownerLatitude = currentLat.toString(),
                                ownerLongitude = currentLng.toString(),
                                latitude = currentLat.toString(),
                                longitude = currentLng.toString(),
                                keyword = keyword,
                                working = StorageUtils.loadValueFromPreference(
                                        this@HospitalSearchActivity,
                                        AppConstants.PREF_KEY_CLINIC_STATUS,
                                        ""
                                ),
                                register = StorageUtils.loadValueFromPreference(
                                        this@HospitalSearchActivity,
                                        AppConstants.PREF_KEY_REGISTER_STATUS,
                                        ""
                                ),
                                booking = StorageUtils.loadValueFromPreference(
                                        this@HospitalSearchActivity,
                                        AppConstants.PREF_KEY_BOOKING_STATUS,
                                        ""
                                ),
                                beauty = StorageUtils.loadValueFromPreference(
                                        this@HospitalSearchActivity,
                                        AppConstants.PREF_KEY_BEAUTY_STATUS,
                                        ""
                                ),
                                hotel = StorageUtils.loadValueFromPreference(
                                        this@HospitalSearchActivity,
                                        AppConstants.PREF_KEY_HOTEL_STATUS,
                                        ""
                                ),
                                allDay = StorageUtils.loadValueFromPreference(
                                        this@HospitalSearchActivity,
                                        AppConstants.PREF_KEY_ALLDAY_STATUS,
                                        ""
                                ),
                                parking = StorageUtils.loadValueFromPreference(
                                        this@HospitalSearchActivity,
                                        AppConstants.PREF_KEY_PARKING_STATUS,
                                        ""
                                ),
                                healthCheck = "",
                                petIdList = petIdList,
                                size = size,
                                page = page)
                )
                val items = response.resultData.center
                if (items.isNotEmpty()) {
                    refreshHospitalList(items)
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    private fun refreshHospitalList(items: List<HospitalItem>) {
        resultItems.clear()
        if (items.size > 0) {
            resultList.visibility = View.VISIBLE
            emptyList.visibility = View.GONE

            resultItems.addAll(items)
            resultAdapter.notifyDataSetChanged()
        } else {
            resultList.visibility = View.GONE
            emptyList.visibility = View.VISIBLE
        }
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

            holder.itemView.setOnClickListener {
                onKeywordItemClicked(keywordItems[position])
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