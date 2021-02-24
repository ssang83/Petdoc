package kr.co.petdoc.petdoc.fragment.mypage.bookmark

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
import kotlinx.android.synthetic.main.fragment_bookmark.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: BookmarkFragment
 * Created by kimjoonsung on 2020/04/13.
 *
 * Description : 북마크 화면
 */
class BookmarkFragment : Fragment() {

    private lateinit var mPagerAdapter: BookmarkTabPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_bookmark, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPagerContainer.apply {
            mPagerAdapter = BookmarkTabPagerAdapter()
            adapter = mPagerAdapter
        }

        TabLayoutMediator(tabLayout, viewPagerContainer) { tab, position ->
            when (position) {
                0 -> tab.text = Helper.readStringRes(R.string.mypage_bookmark_hospital)
                else -> tab.text = Helper.readStringRes(R.string.mypage_bookmark_pet_guide)
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

    inner class BookmarkTabPagerAdapter : FragmentStateAdapter(this) {

        private val mFragmentList:MutableList<Fragment> = mutableListOf()

        init {
            mFragmentList.apply {
                add(HospitalBookmarkFragment())
                add(MagazineBookmarkFragment())
            }
        }

        override fun getItemCount() = mFragmentList.size
        override fun createFragment(position: Int) = mFragmentList[position]
    }
}