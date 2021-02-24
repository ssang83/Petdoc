package kr.co.petdoc.petdoc.fragment.mypage.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import co.ab180.airbridge.Airbridge
import com.facebook.login.LoginManager
import com.google.firebase.auth.FirebaseAuth
import com.kakao.sdk.user.UserApiClient
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.LogoutResponseCallback
import com.nhn.android.naverlogin.OAuthLogin
import kotlinx.android.synthetic.main.fragment_setting_main.*
import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.event.LogoutEvent
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.repository.local.cache.AppCache
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.core.context.GlobalContext

/**
 * Petdoc
 * Class: SettingMainFragment
 * Created by kimjoonsung on 2020/04/13.
 *
 * Description : 설정 메인 화면
 */
class SettingMainFragment : BaseFragment() {

    private val LOGTAG = "SettingMainFragment"
    private val LOGOUT_REQUEST = "$LOGTAG.logoutRequest"

    // naver login
    private var authLoginModule: OAuthLogin? =null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_setting_main, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        versionInfo.text = BuildConfig.VERSION_NAME

        layoutAlarm.setOnClickListener {
            findNavController().navigate(SettingMainFragmentDirections.actionSettingMainFragmentToAlarmSettingFragment())
        }

        layoutMarketingInfo.setOnClickListener {
            findNavController().navigate(SettingMainFragmentDirections.actionSettingMainFragmentToMarketingInfoFragment())
        }

        layoutSignOut.setOnClickListener {
            findNavController().navigate(SettingMainFragmentDirections.actionSettingMainFragmentToSignoutFragment())
        }

        layoutTerms.setOnClickListener {
            findNavController().navigate(SettingMainFragmentDirections.actionSettingMainFragmentToTermsFragment())
        }

        layoutPrivacyPolicy.setOnClickListener {
            findNavController().navigate(SettingMainFragmentDirections.actionSettingMainFragmentToPrivacyPolicyFragment())
        }

        layoutMarketingAgree.setOnClickListener {
            findNavController().navigate(SettingMainFragmentDirections.actionSettingMainFragmentToMarketingAgreeFragment())
        }

        layoutLogout.setOnClickListener {
            TwoBtnDialog(requireContext()).apply {
                setTitle(Helper.readStringRes(R.string.logout_title))
                setMessage(Helper.readStringRes(R.string.logout_message))
                setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
//                    socialLogout(StorageUtils.loadIntValueFromPreference(requireContext(), AppConstants.PREF_KEY_USER_LOGIN_TYPE))
                    logout()
                    dismiss()
                })
                setCancelButton(Helper.readStringRes(R.string.cancel), View.OnClickListener {
                    dismiss()
                })
            }.show()
        }

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
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
    fun onEventMainThread(event: NetworkBusResponse) {
        when(event.tag) {
            LOGOUT_REQUEST -> {
                if (event.status == "ok") {
                    StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS, false)
                    StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_MARKETING_POPUP_NO_SHOW, false)
                    StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_ACCESS_TOKEN, "")
                    StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_REFRESH_TOKEN, "")
                    StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_OLD_TOKEN, "")
                    StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_OLD_USER_ID, "")
                    StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_ID, "")
                    StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_NICK_NAME, "")
                    StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_NAME, "")
                    StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_EMAIL, "")
                    StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_ID_GODO, "")
                    StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_LOGIN_TYPE, 0)
                    StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_AUTH_ID, 0)

                    val appCache by lazy { GlobalContext.get().koin.get<AppCache>() }
                    appCache.clear()
                    EventBus.getDefault().post(LogoutEvent())
                    Airbridge.trackEvent("mypage", "click", "logout", null, null, null)
                    FirebaseAPI(requireActivity()).logEventFirebase("로그아웃", "Click Event", "로그아웃 완료")

                    requireActivity().finish()
                }
            }
        }
    }

    //==============================================================================================
    private fun logout() {
        StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS, false)
        StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_MARKETING_POPUP_NO_SHOW, false)
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_ACCESS_TOKEN, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_REFRESH_TOKEN, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_OLD_TOKEN, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_OLD_USER_ID, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_ID, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_NICK_NAME, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_NAME, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_EMAIL, "")
        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_ID_GODO, "")
        StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_LOGIN_TYPE, 0)
        StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_AUTH_ID, 0)

        PetdocApplication.mPetInfoList.clear()

        val appCache by lazy { GlobalContext.get().koin.get<AppCache>() }
        appCache.clear()
        EventBus.getDefault().post(LogoutEvent())
        Airbridge.trackEvent("mypage", "click", "logout", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("로그아웃", "Click Event", "로그아웃 완료")

        requireActivity().finish()
    }

    private fun socialLogout(socialType: Int) {
        when (socialType) {
            2 -> kakaoLogout()
            3 -> faceBookLogout()
            4 -> naverLogout()
            5 -> googleLogout()
            6 -> appleLogout()
        }
    }

    private fun appleLogout() {

    }

    private fun kakaoLogout() {
        UserManagement.getInstance()
            .requestLogout(object : LogoutResponseCallback() {
                override fun onCompleteLogout() {
                    // 로그아웃 성공
                    Logger.d("kakao logout success")
                }
            })

//        UserApiClient.instance.unlink { error ->
//            if (error != null) {
//                Logger.d("unlink fail, $error")
//            } else {
//                Logger.d("unlink success")
//                UserApiClient.instance.logout { error1 ->
//                    if (error1 != null) {
//                        Logger.d("kakao logout fail, token deleted in SDK : $error1")
//                    } else {
//                        Logger.d("kakao logout success, token deleted in SDK")
//                    }
//                }
//            }
//        }
    }

    private fun faceBookLogout() {
        LoginManager.getInstance().logOut()
    }

    private fun naverLogout() {
        authLoginModule = OAuthLogin.getInstance()
        if (authLoginModule != null) {
            authLoginModule!!.init(
                requireContext(),
                AppConstants.OAUTH_CLIENT_ID,
                AppConstants.OAUTH_CLIENT_SECRET,
                AppConstants.OAUTH_CLIENT_NAME
            )

            authLoginModule!!.logout(requireContext())
        }
    }

    private fun googleLogout() {
        FirebaseAuth.getInstance().signOut()
    }
}