package kr.co.petdoc.petdoc.fragment

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AccelerateInterpolator
import android.view.animation.AlphaAnimation
import android.view.animation.Animation
import androidx.appcompat.widget.AppCompatImageView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import co.ab180.airbridge.Airbridge
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import kotlinx.android.synthetic.main.fragment_pet_chat.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.MyPageActivity
import kr.co.petdoc.petdoc.activity.PushNotiActivity
import kr.co.petdoc.petdoc.activity.chat.ChatSearchActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.chat.ChatListFragment
import kr.co.petdoc.petdoc.fragment.chat.ChatRegisterFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import org.koin.android.ext.android.inject


/**
 * Petdoc
 * Class: PetConsultingFragment
 * Created by kimjoonsung on 2020/04/02.
 *
 * Description : 메인 상담 화면
 */
class PetChatFragment : Fragment() {

    companion object {
        private const val TAB_CHAT = 0
        private const val TAB_CHAT_LIST = 1
    }

    private lateinit var pagerAdapter: ChatTabPagerAdapter

    private var isActivity = false

    private var handler : Handler? = null

    private val apiService: PetdocApiService by inject()
    private val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pet_chat, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if (arguments != null) {
            isActivity = requireArguments().getBoolean("isActivity", isActivity)
            btnSearch.setImageResource(R.drawable.x_button)
            btnMyPage.visibility = View.GONE
        } else {
            Airbridge.trackEvent("tab", "click", "counsel", null, null, null)
            FirebaseAPI(requireActivity()).logEventFirebase("펫닥_상담_홈", "Page View", "상담탭 선택 화면 page view")

            btnSearch.setImageResource(R.drawable.ic_search)
            btnMyPage.visibility = View.VISIBLE
        }

        //===========================================================================================
        btnSearch.setOnSingleClickListener {
            if (isActivity) {
                requireActivity().onBackPressed()
            } else {
                startActivity(Intent(requireActivity(), ChatSearchActivity::class.java))
            }
        }

        btnMyPage.setOnSingleClickListener {
            if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
                startActivity(Intent(requireActivity(), MyPageActivity::class.java))
            }else{
                startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
            }
        }

        btnNoti.setOnSingleClickListener {
            if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS)) {
                startActivity(Intent(requireActivity(), PushNotiActivity::class.java))
            }else{
                startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
            }
        }

        //==========================================================================================
        viewPagerContainer.apply {
            pagerAdapter = ChatTabPagerAdapter()
            adapter = pagerAdapter
            isUserInputEnabled = false
        }

        TabLayoutMediator(tabLayout, viewPagerContainer) { tab, position ->
            when (position) {
                TAB_CHAT -> tab.text = resources.getString(R.string.chat_request)
                TAB_CHAT_LIST -> tab.text = resources.getString(R.string.chat_list)
            }
        }.attach()

        tabLayout.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabReselected(tab: TabLayout.Tab?) {}

            override fun onTabSelected(tab: TabLayout.Tab?) {
                viewPagerContainer.setCurrentItem(tab!!.position, false)
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {}
        })

        if (isActivity) {
            searchGuideImg.visibility = View.GONE
        } else {
            searchGuideImg.visibility = View.VISIBLE

            handler = Handler{
                fadeOutAndHideImage(searchGuideImg)
                handler?.sendEmptyMessageDelayed(0, 3000)
                handler?.removeMessages(0)
                true
            }
        }

        checkPushStatus()
    }

    override fun onResume() {
        super.onResume()
        if (!isActivity) {
            if(handler?.hasMessages(0)==false) handler?.sendEmptyMessageDelayed(0, 3000)
        }
    }

    override fun onPause() {
        super.onPause()
        if (!isActivity) {
            handler?.removeMessages(0)
        }
    }

    private fun fadeOutAndHideImage(imageView: AppCompatImageView) {
        val fadeOut: Animation = AlphaAnimation(1f, 0f).apply {
            interpolator = AccelerateInterpolator()
            duration = 1000
        }

        fadeOut.setAnimationListener(object : Animation.AnimationListener {
            override fun onAnimationEnd(animation: Animation?) {
                imageView.setVisibility(View.GONE)
            }

            override fun onAnimationRepeat(animation: Animation?) {}
            override fun onAnimationStart(animation: Animation?) {}
        })

        imageView.startAnimation(fadeOut)
    }

    private fun checkPushStatus() {
        lifecycleScope.launch(Dispatchers.Main + exceptionHandler) {
            val response = withContext(Dispatchers.IO) { apiService.getPushUnRead() }
            if (response.code == "SUCCESS") {
                val status = response.resultData as Boolean
                notiUpdate.visibility = if (status) {
                    View.VISIBLE
                } else {
                    View.GONE
                }
            }
        }
    }

    //==============================================================================================
    inner class ChatTabPagerAdapter : FragmentStateAdapter(this) {

        private val mFragmentList:MutableList<Fragment> = mutableListOf()

        init {
            mFragmentList.apply {
                add(ChatRegisterFragment())
                add(ChatListFragment())
            }
        }

        override fun getItemCount() = mFragmentList.size
        override fun createFragment(position: Int) = mFragmentList[position]
    }
}