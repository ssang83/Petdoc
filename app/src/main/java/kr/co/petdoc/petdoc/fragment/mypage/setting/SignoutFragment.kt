package kr.co.petdoc.petdoc.fragment.mypage.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_sign_out.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.event.LogoutEvent
import kr.co.petdoc.petdoc.fragment.BaseFragment
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
 * Class: SignoutFragment
 * Created by kimjoonsung on 2020/04/14.
 *
 * Description : 탈퇴하기 화면
 */
class SignoutFragment : BaseFragment() {

    private val LOGTAG = "SignoutFragment"
    private val SIGN_OUT_REQUEST = "$LOGTAG.signOutRequest"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_sign_out, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val nickName = StorageUtils.loadValueFromPreference(
            requireContext(),
            AppConstants.PREF_KEY_USER_NICK_NAME,
            ""
        )

        signoutTitle.text = "${nickName} 고객님,\n정말 탈퇴 하시려구요?"

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        btnCheckPetdoc.setOnClickListener {
            it.isSelected = !it.isSelected

            layoutCheckDetail.apply {
                when {
                    it.isSelected == true -> {
                        visibility = View.VISIBLE

                        btnSignOut.apply {
                            isEnabled = true
                            setTextColor(Helper.readColorRes(R.color.orange))
                            setBackgroundResource(R.drawable.main_color_round_rect)
                        }
                    }
                    else -> {
                        visibility = View.GONE

                        btnSignOut.apply {
                            isEnabled = false
                            setTextColor(Helper.readColorRes(R.color.light_grey3))
                            setBackgroundResource(R.drawable.grey_round_rect)
                        }
                    }
                }
            }
        }

        btnCheckVCare.setOnClickListener {
            layoutCheckDetail.apply {
                it.isSelected = !it.isSelected

                when {
                    it.isSelected == true -> {
                        visibility = View.VISIBLE

                        btnSignOut.apply {
                            isEnabled = true
                            setTextColor(Helper.readColorRes(R.color.orange))
                            setBackgroundResource(R.drawable.main_color_round_rect)
                        }

                    }
                    else -> {
                        visibility = View.GONE

                        btnSignOut.apply {
                            isEnabled = false
                            setTextColor(Helper.readColorRes(R.color.light_grey3))
                            setBackgroundResource(R.drawable.grey_round_rect)
                        }
                    }
                }
            }
        }

        btnSignOut.setOnClickListener {
            TwoBtnDialog(requireContext()).apply {
                setTitle("회원탈퇴")
                setMessage("확인 버튼을 누르시면 탈퇴처리가 완료됩니다.")
                setConfirmButton("확인", View.OnClickListener {
                    mApiClient.deleteSignOut(SIGN_OUT_REQUEST)
                    dismiss()
                })
                setCancelButton("취소", View.OnClickListener {
                    dismiss()
                })
            }.show()

//            OneBtnDialog(requireContext()).apply {
//                setTitle("회원탈퇴")
//                setMessage("${nickName} 님께서 현재\n이용중인 서비스가 있습니다. 이용완료 후 삭제가\n가능합니다.")
//                setConfirmButton("확인", View.OnClickListener {
//                    dismiss()
//                })
//            }.show()
        }

        btnSignOut.apply {
            isEnabled = true
            setTextColor(Helper.readColorRes(R.color.orange))
            setBackgroundResource(R.drawable.main_color_round_rect)
        }
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(SIGN_OUT_REQUEST)) {
            mApiClient.cancelRequest(SIGN_OUT_REQUEST)
        }
        super.onDestroyView()
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
    fun onEventMainThread(event:NetworkBusResponse) {
        when(event.tag) {
            SIGN_OUT_REQUEST -> {
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

                    val appCache by lazy { GlobalContext.get().koin.get<AppCache>() }
                    appCache.clear()
                    EventBus.getDefault().post(LogoutEvent())
                    requireActivity().finish()
                    Airbridge.trackEvent("mypage", "click", "signout", null, null, null)
                    FirebaseAPI(requireActivity()).logEventFirebase("탈퇴하기", "Click Event", "탈퇴하기 완료")
                }
            }
        }
    }
}