package kr.co.petdoc.petdoc.fragment.search

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import co.ab180.airbridge.Airbridge
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_search_result.*
import kotlinx.android.synthetic.main.fragment_search_result.btnBack
import kotlinx.android.synthetic.main.fragment_search_result.btnCancel
import kotlinx.android.synthetic.main.fragment_search_result.editSearch
import kotlinx.android.synthetic.main.fragment_search_result.inputDelete
import kotlinx.android.synthetic.main.fragment_search_result.layoutDel
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: SearchResultFragment
 * Created by kimjoonsung on 2020/04/22.
 *
 * Description :
 */
class SearchResultFragment : Fragment() {

    companion object {
        lateinit var instance:SearchResultFragment
    }

    private lateinit var mAdapter: SearchResultPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_search_result, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)
        instance = this

        FirebaseAPI(requireActivity()).logEventFirebase("검색결과", "Page View", "홈 > 검색에서 검색어 입력 완료 후 검색 결과 Page view")

        val keyword = arguments?.getString("keyword") ?: ""
        Logger.d("keyword : $keyword")

        viewPagerContainer.apply {
            mAdapter = SearchResultPagerAdapter()
            adapter = mAdapter
        }

        TabLayoutMediator(tabLayout, viewPagerContainer) { tab, position ->
            when (position) {
                0 -> tab.text = Helper.readStringRes(R.string.home_consulting)
                else -> tab.text = Helper.readStringRes(R.string.home_guideline)
            }
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPagerContainer.setCurrentItem(tab!!.position, false)
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        inputDelete.setOnClickListener { editSearch.setText("") }
        btnCancel.setOnClickListener { requireActivity().onBackPressed() }

        //============================================================================================
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

        editSearch.setText(keyword)
        editSearch.setOnEditorActionListener { v, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val newKeyword = v.text.toString().trim()
                if (newKeyword.isNotBlank()) {
                    (mAdapter.fragmentList[0] as SearchTalkFragment).refreshWithKeyword(newKeyword)
                    (mAdapter.fragmentList[1] as SearchPetGuideFragment).refreshWithKeyword(newKeyword)

                    hideKeyboard(editSearch)
                }
            }

            true
        }

        Airbridge.trackEvent("search", "click", "search_done", null, null, null)
    }

    fun getKeyword() = editSearch.text.toString()

    /**
     * Hide KeyBoard
     *
     * @param v
     */
    private fun hideKeyboard(v: View) {
        Helper.hideKeyboard(requireActivity(), v)
        v.clearFocus()
    }

    //==========================================================================
    inner class SearchResultPagerAdapter : FragmentStateAdapter(this) {

        val fragmentList:MutableList<Fragment> = mutableListOf()

        init {
            fragmentList.apply {
                add(SearchTalkFragment())
                add(SearchPetGuideFragment())
            }
        }

        override fun getItemCount() = fragmentList.size
        override fun createFragment(position: Int) = fragmentList[position]
    }
}