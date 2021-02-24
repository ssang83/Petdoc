package kr.co.petdoc.petdoc.fragment.login

import android.graphics.Typeface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.AppCompatTextView
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_find_account.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: FindAccountFragment
 * Created by kimjoonsung on 2020/07/30.
 *
 * Description :
 */
class FindAccountFragment : Fragment() {

    private lateinit var mPagerAdapter:FindAccountAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_find_account, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)

        viewPagerContainer.apply {
            mPagerAdapter = FindAccountAdapter()
            adapter = mPagerAdapter
        }

        TabLayoutMediator(tabLayout, viewPagerContainer) { tab, position ->
            when (position) {
                0 -> tab.text = "아이디 찾기"
                else -> tab.text = "비밀번호 찾기"
            }
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPagerContainer.setCurrentItem(tab!!.position, false)

                if (tab.position == 1) {
                    FirebaseAPI(requireActivity()).logEventFirebase("비밀번호찾기", "Click Event", "비밀번호 찾기 클릭")
                }
            }

            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        setTabLayoutFont()
    }

    private fun setTabLayoutFont() {
        val vg = tabLayout.getChildAt(0) as ViewGroup
        val tabCnt = vg.childCount

        for (i in 0 until tabCnt) {
            val vgTab = vg.getChildAt(i) as ViewGroup
            val tabChildCnt = vgTab.childCount
            for (j in 0 until tabChildCnt) {
                val tabViewChild = vgTab.getChildAt(j)
                if (tabViewChild is AppCompatTextView) {
                    tabViewChild.typeface = Typeface.createFromAsset(requireContext().assets, "fonts/notosanskr_bold.otf")
                }
            }
        }
    }

    inner class FindAccountAdapter : FragmentStateAdapter(this) {

        private val mFragmentList:MutableList<Fragment> = mutableListOf()

        init {
            mFragmentList.apply {
                add(FindIDFragment())
                add(FindPassWordFragment())
            }
        }

        override fun getItemCount() = mFragmentList.size
        override fun createFragment(position: Int) = mFragmentList[position]
    }
}