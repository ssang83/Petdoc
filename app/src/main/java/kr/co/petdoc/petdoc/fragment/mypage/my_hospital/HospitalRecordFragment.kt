package kr.co.petdoc.petdoc.fragment.mypage.my_hospital

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
import kotlinx.android.synthetic.main.fragment_hospital_record.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper

/**
 * Petdoc
 * Class: MyHospitalFragment
 * Created by kimjoonsung on 2020/04/14.
 *
 * Description : 접수/예약 화면
 */
class HospitalRecordFragment : Fragment() {

    private lateinit var mPagerAdapter:MyHospitaTabPagerAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_hospital_record, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        viewPagerContainer.apply {
            mPagerAdapter = MyHospitaTabPagerAdapter()
            adapter = mPagerAdapter
        }

        TabLayoutMediator(tabLayout, viewPagerContainer) { tab, position ->
            when (position) {
                0 -> tab.text = Helper.readStringRes(R.string.mypage_reception)
                else -> tab.text = Helper.readStringRes(R.string.mypage_reservation)
            }
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPagerContainer.setCurrentItem(tab!!.position, false)
            }
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

    inner class MyHospitaTabPagerAdapter : FragmentStateAdapter(this) {

        private val mFragmentList:MutableList<Fragment> = mutableListOf()

        init {
            mFragmentList.apply {
                add(MyHospitalRegisterFragment())
                add(MyHospitalBookingFragment())
            }
        }

        override fun getItemCount() = mFragmentList.size
        override fun createFragment(position: Int) = mFragmentList[position]
    }
}