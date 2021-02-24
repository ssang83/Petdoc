package kr.co.petdoc.petdoc.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_home_tab.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.home.*

/**
 * petdoc-android
 * Class: HomeFragment
 * Created by kimjoonsung on 2020/04/02.
 *
 * Description : 메인 홈 화면
 */
class HomeTabFragment : Fragment() {

    companion object {
        private const val TAB_ALL = 0
        private const val TAB_COUNSELING = 1
        private const val TAB_PET_GUIDE = 2
        private const val TAB_PETDOC_TV = 3
        private const val TAB_PET_TALK = 4
    }

    private lateinit var mPagerAdapter: HomeTabPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home_tab, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewPagerContainer.apply {
            mPagerAdapter = HomeTabPagerAdapter()
            adapter = mPagerAdapter
            isUserInputEnabled = false
        }

        TabLayoutMediator(tabLayout, viewPagerContainer) { tab, position ->
            when (position) {
                TAB_ALL -> tab.text = resources.getString(R.string.home_all)
                TAB_COUNSELING -> tab.text = resources.getString(R.string.home_consulting)
                TAB_PET_GUIDE -> tab.text = resources.getString(R.string.home_guideline)
                TAB_PETDOC_TV -> tab.text = resources.getString(R.string.home_petdoc_tv)
                TAB_PET_TALK -> tab.text = resources.getString(R.string.home_pet_talk)
            }
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPagerContainer.setCurrentItem(tab!!.position, false)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })
    }

    inner class HomeTabPagerAdapter : FragmentStateAdapter(this) {

        private val mFragmentList:MutableList<Fragment> = mutableListOf()

        init {
            mFragmentList.apply {
                add(HomeFragment())
                add(ConsultingFragment())
                add(PetGuideFragment())
                add(PetdocTVFragment())
                add(PetTalkFragment())
            }
        }

        override fun getItemCount() = mFragmentList.size
        override fun createFragment(position: Int) = mFragmentList[position]
    }
}