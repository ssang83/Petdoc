package kr.co.petdoc.petdoc.fragment.search

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
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_search_before.*
import kotlinx.android.synthetic.main.view_home_search_keyword.view.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.ChatKeywordListResponse
import kr.co.petdoc.petdoc.network.response.submodel.ChatKeywordItem
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.OnSingleClickListener
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode


/**
 * Petdoc
 * Class: SearchBeforeFragment
 * Created by kimjoonsung on 2020/04/22.
 *
 * Description :
 */
class SearchBeforeFragment : BaseFragment() {

    private val LOGTAG = "SearchBeforeFragment"
    private val KEYWORD_LIST_REQUEST = "${LOGTAG}.keywordListRequest"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_search_before, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnSingleClickListener { requireActivity().onBackPressed() }
        inputDelete.setOnSingleClickListener { editSearch.setText("") }
        btnCancel.setOnSingleClickListener { requireActivity().onBackPressed() }

        editSearch.setOnEditorActionListener(object : TextView.OnEditorActionListener {
            override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
                if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                    bundleOf("keyword" to editSearch.text.toString()).let {
                        hideKeyboard(editSearch)
                        findNavController().navigate(R.id.action_searchBeforeFragment_to_searchResultFragment, it)
                    }
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

        //===========================================================================================
        retryIfNetworkAbsent {
            if (!mApiClient.isRequestRunning(KEYWORD_LIST_REQUEST)) {
                mApiClient.getChatKeywordList(KEYWORD_LIST_REQUEST)
            }
        }
        
        showKeyBoardOnView(editSearch)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(KEYWORD_LIST_REQUEST)) {
            mApiClient.cancelRequest(KEYWORD_LIST_REQUEST)
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
            KEYWORD_LIST_REQUEST -> {
                if (response is ChatKeywordListResponse) {
                    setKeywordList(response.keywordList)
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

    //==============================================================================================
    private fun setKeywordList(keywords: List<ChatKeywordItem>) {
        keywordTags.removeAllViews()
        for (i in 0 until keywords.size) {
            keywordTags.addView(
                KeywordTag(requireContext(), keywords[i].keyword)
            )
        }
    }

    private fun keywordClicked(keyword: String) {
        hideKeyboard(editSearch)

        bundleOf("keyword" to keyword).let {
            hideKeyboard(editSearch)
            findNavController().navigate(R.id.action_searchBeforeFragment_to_searchResultFragment, it)
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

}