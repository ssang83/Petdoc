package kr.co.petdoc.petdoc.fragment.hospital

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_hospital_info_edit.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.LoginAndRegisterActivity
import kr.co.petdoc.petdoc.activity.auth.MobileAuthActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: HospitalInfoEditFragment
 * Created by kimjoonsung on 2020/07/22.
 *
 * Description :
 */
class HospitalInfoEditFragment : BaseFragment() {

    private val TAG = "HospitalInfoEditFragment"
    private val HOSPITAL_INFO_EDIT_REQUEST = "$TAG.hospitalInfoEditRequest"

    enum class CenterEditType {
        C,  // 폐업
        N,  // 병원이름
        A,  // 주소
        P,  // 전화번호
        T,  // 진료/영업시간
        E   // 기타
    }

    private var hospitalName = ""
    private var address = ""
    private var centerId = -1
    private var flagbox = booleanArrayOf(false, false, false, false, false)
    private var type = HashSet<String>()

        override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_hospital_info_edit, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        hospitalName = arguments?.getString("name") ?: hospitalName
        address = arguments?.getString("address") ?: address
        centerId = arguments?.getInt("centerId") ?: centerId
        Logger.d("hospitalName : $hospitalName, address : $address, centerId : $centerId")

        name.text = hospitalName
        location.text = address

        //==========================================================================================
        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        btnCheck1.setOnClickListener {
            it.isSelected = !it.isSelected

            type.apply {
                if(this.contains(CenterEditType.C.name)) {
                    this.remove(CenterEditType.C.name)
                    flagbox[0] = false
                }
                else {
                    this.add(CenterEditType.C.name)
                    flagbox[0] = true
                }
            }
        }

        btnCheck2.setOnClickListener {
            it.isSelected = !it.isSelected

            type.apply {
                if(this.contains(CenterEditType.N.name)) {
                    this.remove(CenterEditType.N.name)
                    flagbox[1] = false
                }
                else {
                    this.add(CenterEditType.N.name)
                    flagbox[1] = true
                }
            }
        }

        btnCheck3.setOnClickListener {
            it.isSelected = !it.isSelected

            type.apply {
                if(this.contains(CenterEditType.A.name)) {
                    this.remove(CenterEditType.A.name)
                    flagbox[1] = false
                }
                else {
                    this.add(CenterEditType.A.name)
                    flagbox[1] = true
                }
            }
        }

        btnCheck4.setOnClickListener {
            it.isSelected = !it.isSelected

            type.apply {
                if(this.contains(CenterEditType.P.name)) {
                    this.remove(CenterEditType.P.name)
                    flagbox[3] = false
                }
                else {
                    this.add(CenterEditType.P.name)
                    flagbox[3] = true
                }
            }
        }

        btnCheck5.setOnClickListener {
            it.isSelected = !it.isSelected

            type.apply {
                if(this.contains(CenterEditType.T.name)) {
                    this.remove(CenterEditType.T.name)
                    flagbox[4] = false
                }
                else {
                    this.add(CenterEditType.T.name)
                    flagbox[4] = true
                }
            }
        }

        btnEdit.setOnClickListener {
            if (flagbox[0] || flagbox[1] || flagbox[2] || flagbox[3] || flagbox[4]) {
                if (StorageUtils.loadBooleanValueFromPreference(
                        requireContext(),
                        AppConstants.PREF_KEY_LOGIN_STATUS
                    )
                ) {
                    mApiClient.postHospitalEdit(HOSPITAL_INFO_EDIT_REQUEST, centerId, type)
                } else {
                    startActivity(Intent(requireActivity(), LoginAndRegisterActivity::class.java))
                }
            } else {
                AppToast(requireContext()).showToastMessage(
                    "수정 제안할 항목을 선택하세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(HOSPITAL_INFO_EDIT_REQUEST)) {
            mApiClient.cancelRequest(HOSPITAL_INFO_EDIT_REQUEST)
        }
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================
    /**
     * response
     *
     * @param response
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event:NetworkBusResponse) {
        when (event.tag) {
            HOSPITAL_INFO_EDIT_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        requireActivity().onBackPressed()
                    } else {
                        AppToast(requireContext()).showToastMessage(
                            "수정 제안 요청이 실패되었습니다.\n다시 시도해 주세요.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }
                }
            }
        }
    }
}