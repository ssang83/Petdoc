package kr.co.petdoc.petdoc.fragment.intro

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_intro_user_guide.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MainActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.fragment.intro.pages.UserGuidePatternFragment
import kr.co.petdoc.petdoc.fragment.intro.pages.WalkTroughPatternFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils


/**
 * petdoc-android
 * Class: UserGuideFragment
 * Fixed by sungminkim on 2020/04/03.
 *
 * Description :  Intro User Guide Fragment, linked with SplashActivity navigator,  move from PermissionGuideFragment
 */
class UserGuideFragment : Fragment() {

    lateinit var guidePageAdapter : GuideViewPages
    lateinit var pageChangeListener : ViewPager2.OnPageChangeCallback

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha=0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_intro_user_guide, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        readyGuidePages()

        intro_user_guide_skip_button.setOnClickListener {
            StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_FIRST_START_USER_FLAG, "1")

            requireActivity().apply {
                startActivity(Intent(requireContext(), MainActivity::class.java))
                finish()
            }
        }
    }

    private fun readyGuidePages(){

        pageChangeListener = object : ViewPager2.OnPageChangeCallback(){
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                Logger.d("position : $position")
            }
        }

        intro_user_guide_viewpager.apply{
            offscreenPageLimit = 3
            registerOnPageChangeCallback(pageChangeListener)
            guidePageAdapter = GuideViewPages()
            adapter = guidePageAdapter
            overScrollMode = RecyclerView.OVER_SCROLL_NEVER
        }

        indicator.setViewPager2(intro_user_guide_viewpager)
    }

    override fun onDestroyView() {
        intro_user_guide_viewpager.unregisterOnPageChangeCallback(pageChangeListener)
        super.onDestroyView()
    }

    // ---------------------------------------------------------------------------------------------
    inner class GuideViewPages : FragmentStateAdapter(this){

        val mFragmentList:MutableList<Fragment> = mutableListOf()

        init{
            mFragmentList.apply{
                // add Fragments
                add(WalkTroughPatternFragment(R.drawable.img_walktrough_01, "반려동물 건강관리는\n미리미리,똑똑하게", "스캐너로 매일 건강 기록하고\n펫 체크 어드밴스드로\n우리 아이 건강검진까지!"))
                add(WalkTroughPatternFragment(R.drawable.img_walktrough_02, "수의사 1:1 상담부터\n동물병원 예약까지", "궁금한 것이 있다면\n수의사와 바로 상담하고\n가까운 동물병원 예약까지 한 번에!"))
                add(WalkTroughPatternFragment(R.drawable.img_walktrough_03, "수의사들이 설계한\n커스터밀!", "문진 한 번으로 우리 아이에게\n딱! 맞는 맞춤식 사료"))
            }
        }

        fun getCount() = mFragmentList.size

        override fun getItemCount(): Int = mFragmentList.size
        override fun createFragment(position: Int): Fragment = mFragmentList[position]
    }
    // ---------------------------------------------------------------------------------------------
}