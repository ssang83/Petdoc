package kr.co.petdoc.petdoc.fragment.intro

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_intro_permission_guide.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.utils.Helper


/**
 * petdoc-android
 * Class: PermissionGuideFragment
 * Fixed by sungminkim on 2020/04/03.
 *
 * Description :  Intro Permission Guide Fragment, linked with SplashActivity navigator,  move from SplashIntroFragment
 */
class PermissionGuideFragment : Fragment() {

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(activity!!, true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_intro_permission_guide, container, false)
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Airbridge.trackEvent("agree", "click", "agree_사용권한", null, null, null)

        permssion_guide_confirm_button.setOnClickListener {
            Airbridge.trackEvent("agree", "click", "사용권한동의", null, null, null)

            val action = PermissionGuideFragmentDirections.actionPermissionGuideToUserGuideFragment()
            findNavController().navigate(action)
        }
    }
}