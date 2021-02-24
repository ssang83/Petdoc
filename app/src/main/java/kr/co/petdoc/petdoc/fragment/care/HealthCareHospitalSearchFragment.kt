package kr.co.petdoc.petdoc.fragment.care

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import android.view.inputmethod.EditorInfo
import android.widget.FrameLayout
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.couchbase.lite.MutableDocument
import kotlinx.android.synthetic.main.activity_hospital_search.*
import kotlinx.android.synthetic.main.adapter_hospital_search_result_item.view.*
import kotlinx.android.synthetic.main.adapter_recently_keyword_item.view.*
import kotlinx.android.synthetic.main.fragment_health_care_hospital_search.*
import kotlinx.android.synthetic.main.fragment_health_care_hospital_search.btnBack
import kotlinx.android.synthetic.main.fragment_health_care_hospital_search.btnCancel
import kotlinx.android.synthetic.main.fragment_health_care_hospital_search.btnDeleteAll
import kotlinx.android.synthetic.main.fragment_health_care_hospital_search.editSearch
import kotlinx.android.synthetic.main.fragment_health_care_hospital_search.emptyList
import kotlinx.android.synthetic.main.fragment_health_care_hospital_search.inputDelete
import kotlinx.android.synthetic.main.fragment_health_care_hospital_search.layoutDel
import kotlinx.android.synthetic.main.fragment_health_care_hospital_search.recentlyKeywordLayout
import kotlinx.android.synthetic.main.fragment_health_care_hospital_search.recentlyKeywordList
import kotlinx.android.synthetic.main.fragment_health_care_hospital_search.recommendKeywords
import kotlinx.android.synthetic.main.fragment_health_care_hospital_search.resultLayout
import kotlinx.android.synthetic.main.fragment_health_care_hospital_search.resultList
import kotlinx.android.synthetic.main.view_home_search_keyword.view.keyword
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.db.KeywordDBManager
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.submodel.HospitalItem
import kr.co.petdoc.petdoc.repository.PetdocRepository
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import kr.co.petdoc.petdoc.repository.network.form.HospitalListForm
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import org.koin.android.ext.android.inject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: HealthCareHospitalSearchFragment
 * Created by kimjoonsung on 2020/09/10.
 *
 * Description :
 */
class HealthCareHospitalSearchFragment : Fragment() {

    companion object {
        private const val KEYWORD_ID = 1
    }

    private lateinit var dataModel: HospitalDataModel

    private lateinit var keywordAdapter: KeywordAdapter
    private val keywordItems: MutableList<SearchKeywordItem> = mutableListOf()

    private lateinit var resultAdapter: ResultAdapter
    private val resultItems:MutableList<HospitalItem> = mutableListOf()

    private var page = 1
    private val size = 50

    private val petdocRepository: PetdocRepository by inject()
    private val apiService: PetdocApiService by inject()

    val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_health_care_hospital_search, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        inputDelete.setOnClickListener { editSearch.setText("") }
        btnCancel.setOnClickListener { requireActivity().onBackPressed() }
        btnDeleteAll.setOnClickListener { deleteKeywordAll() }

        editSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    hideKeyboard(editSearch)
                    saveKeyword(editSearch.text.toString())
                    if (resultItems.size > 0) {
                        dataModel.hospitalItem.value = resultItems[0]
                        dataModel.keyword.value = editSearch.text.toString()
                        dataModel.searchMode.value = true
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
                    dataModel.keyword.value = s.toString()

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
            layoutManager = LinearLayoutManager(requireContext())
            adapter = keywordAdapter
        }

        resultAdapter = ResultAdapter()
        resultList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultAdapter
        }

        //==========================================================================================
        loadKeyword()
        loadRecommendKeyword()
        showKeyBoardOnView(editSearch)
        editSearch.requestFocus()
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

        run loop@{
            KeywordDBManager.getInstance(requireContext()).loadAllExt()!!.forEach {
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
            setString("keyword", editSearch.text.toString())
            setString(
                "reg_date",
                SimpleDateFormat("MM-dd").format(Date(System.currentTimeMillis()))
            )
        }

        Logger.e(
            "keywordID : $nextKeywordId, keyword:$keyword, date:${
                SimpleDateFormat("MM-dd").format(
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
                SearchKeywordItem(
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

    private fun deleteKeyword(item:SearchKeywordItem, position: Int) {
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
        dataModel.keyword.value = editSearch.text.toString()
        dataModel.searchMode.value = true

        saveKeyword(editSearch.text.toString())
        requireActivity().onBackPressed()
    }

    private fun onKeywordItemClicked(item: SearchKeywordItem) {
        editSearch.setText(item.keyword)
        hideKeyboard(editSearch)
    }

    private fun loadRecommendKeyword() {
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO + exceptionHandler) {
            val response = apiService.getHospitalSearchKeyword()
            setKeywordList(response.keywordList)
        }
    }

    private fun loadHospitalList() {
        viewLifecycleOwner.lifecycleScope.launch {
            try {
                val response = petdocRepository.fetchHospitalList(
                        HospitalListForm(
                                ownerLatitude = dataModel.currentLat.value.toString(),
                                ownerLongitude = dataModel.currentLng.value.toString(),
                                latitude = dataModel.currentLat.value.toString(),
                                longitude = dataModel.currentLng.value.toString(),
                                keyword = dataModel.keyword.value.toString(),
                                healthCheck = "Y",
                                petIdList = dataModel.animalIdList.value!!,
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

    /**
     * Open KeyBoard
     *
     * @param v
     */
    private fun showKeyBoardOnView(v: View) {
        v.requestFocus()
        Helper.showKeyBoard(requireActivity(), v)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE)
        v.requestFocus()
    }

    /**
     * Hide KeyBoard
     *
     * @param v
     */
    private fun hideKeyboard(v: View) {
        Helper.hideKeyboard(requireActivity(), v)
        v.clearFocus()
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

data class SearchKeywordItem(val id:String, val keyword:String, val regDate: String)
