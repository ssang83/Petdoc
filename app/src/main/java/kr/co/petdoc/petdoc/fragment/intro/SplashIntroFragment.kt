package kr.co.petdoc.petdoc.fragment.intro

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_intro_splash.*
import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MainActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.SplashImages
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.DeepLinkViewModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import kotlin.random.Random


/**
 * petdoc-android
 * Class: SplashIntroFragment
 * Fixed by sungminkim on 2020/04/03.
 *
 * Description :  Intro Splash Fragment, linked with SplashActivity navigator
 */
class SplashIntroFragment : BaseFragment() {


    private val splashTimeOut = if (BuildConfig.DEBUG) 100L else 3200L

    private val logTag = "splashImageRequest"
    private val tagSplashImageRequest = "$logTag.splashImageRequest"

    private lateinit var handler: Handler

    private lateinit var dataModel:DeepLinkViewModel

    private val msgMoveToGuide = 0
    private val msgMoveToMain = 1


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        initialize()
        dataModel = ViewModelProvider(requireActivity()).get(DeepLinkViewModel::class.java)
        return inflater.inflate(R.layout.fragment_intro_splash, container, false)
    }

    private fun initialize(){
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen =  true)
        handler = Handler{
            when(it.what){
                msgMoveToGuide -> {
                    val action = SplashIntroFragmentDirections.actionSplashIntroFragmentToPermissionGuide()
                    findNavController().navigate(action)
                }
                msgMoveToMain -> {
                    requireActivity().apply {
                        startActivity(Intent(requireContext(), MainActivity::class.java).apply {
                            putExtra("deepLinkType", dataModel.deepLinkType.value.toString())
                            putExtra("deepLinkId", dataModel.deepLinkId.value.toString())
                            putExtra("deepLinkBookingId", dataModel.deepLinkBookingId.value.toString())
                        })
                        finish()
                        overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out)
                    }
                }
                else -> {}
            }
            true
        }

//        mApiClient.loadSplash(tagSplashImageRequest)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        if(StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_FIRST_START_USER_FLAG, "").isNotBlank()) {
            handler.sendEmptyMessageDelayed(msgMoveToMain, splashTimeOut)
        }else{
            handler.sendEmptyMessageDelayed(msgMoveToGuide, splashTimeOut)
        }

        handler.postDelayed({
            if (isAdded) {
                splahLayout.visibility = View.VISIBLE
            }
        }, 1000)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        handler.removeMessages(msgMoveToGuide)
        handler.removeMessages(msgMoveToMain)

        if(!PetdocApplication.application.apiClient.isRequestRunning(tagSplashImageRequest)) {
            PetdocApplication.application.apiClient.cancelRequest(tagSplashImageRequest)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(data: NetworkBusResponse) {
        when(data.tag) {
            tagSplashImageRequest -> {
                if (data.status == "ok") {
                    val splashImageList:SplashImages = Gson().fromJson(data.response, object : TypeToken<SplashImages>() {}.type)
                    val itemPosition = Random(System.currentTimeMillis()).nextInt(splashImageList.splashImages.size)

                    if(isAdded) {
                        splash_image_area.postDelayed({
                            if(StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_LAST_SPLASH_IMAGE_URL,"").isNullOrBlank()){
                                Glide.with(requireActivity()).load(splashImageList.splashImages[itemPosition].image_url).transition(DrawableTransitionOptions.withCrossFade()).into(splash_image_area)
                            }else{
                                Glide.with(requireActivity()).load(StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_LAST_SPLASH_IMAGE_URL,"")).transition(DrawableTransitionOptions.withCrossFade()).into(splash_image_area)
                            }
                            StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_LAST_SPLASH_IMAGE_URL, splashImageList.splashImages[itemPosition].image_url)
                        },800)
                    }
                } else {
                    Logger.d("code : ${data.code}, response : ${data.response}")
                }
            }
        }

    }
}