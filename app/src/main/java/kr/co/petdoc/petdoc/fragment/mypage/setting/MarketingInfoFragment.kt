package kr.co.petdoc.petdoc.fragment.mypage.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_marketing_info.*
import kotlinx.android.synthetic.main.fragment_marketing_info.btnBack
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: MarketingInfoFragment
 * Created by kimjoonsung on 2020/04/14.
 *
 * Description : 마케팅 정보 수신 설정 화면
 */
class MarketingInfoFragment : BaseFragment() {

    private val LOGTAG = "MarketingInfoFragment"
    private val MARKETING_STATUS_REQUEST = "$LOGTAG.marketingStatusRequest"

    private val flagBox = booleanArrayOf(false, false)

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_marketing_info, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnAll.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                flagBox[0] = true
                flagBox[1] = true
            } else {
                flagBox[0] = false
                flagBox[1] = false
            }

            checkAllStatus()

            StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_EMAIL_STATUS, isChecked)
            StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_SMS_STATUS, isChecked)
        }

        btnEmail.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                flagBox[0] = true
            } else {
                flagBox[0] = false
            }

            checkAllStatus()

            StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_EMAIL_STATUS, isChecked)
        }

        btnSms.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                flagBox[1] = true
            } else {
                flagBox[1] = false
            }

            checkAllStatus()

            StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_SMS_STATUS, isChecked)
        }

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        btnEmail.isChecked = StorageUtils.loadBooleanValueFromPreference(
            requireContext(),
            AppConstants.PREF_KEY_EMAIL_STATUS
        )

        btnSms.isChecked = StorageUtils.loadBooleanValueFromPreference(
            requireContext(),
            AppConstants.PREF_KEY_SMS_STATUS
        )
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
            MARKETING_STATUS_REQUEST -> {
                if (event.status == "ok") {
                    Logger.d("response : ${event.response}")
                }
            }
        }
    }

    //==============================================================================================

    private fun checkAllStatus() {
        if (flagBox[0] && flagBox[1]) {
            btnAll.isChecked = true
            btnEmail.isChecked = true
            btnSms.isChecked = true

            mApiClient.putMarketingStatus(MARKETING_STATUS_REQUEST, "true")
            Airbridge.trackEvent("signup", "click", "agree_marketing", null, null, null)
        } else {
            if (flagBox[0]) {
                btnAll.isChecked = false
                btnSms.isChecked = false
                btnEmail.isChecked = true
            } else if (flagBox[1]) {
                btnAll.isChecked = false
                btnEmail.isChecked = false
                btnSms.isChecked = true
            } else {
                btnAll.isChecked = false
                btnSms.isChecked = false
                btnEmail.isChecked = false
            }

            mApiClient.putMarketingStatus(MARKETING_STATUS_REQUEST, "false")
            Airbridge.trackEvent("signup", "click", "disagree_marketing", null, null, null)
        }
    }

    private fun setBtnAll(status:Boolean) {
        mApiClient.putMarketingStatus(MARKETING_STATUS_REQUEST, status.toString())
        Airbridge.trackEvent("signup", "click", "agree_marketing", null, null, null)

        btnEmail.isChecked = status
        btnSms.isChecked = status

        StorageUtils.writeBooleanValueInPreference(
            requireContext(),
            AppConstants.PREF_KEY_EMAIL_STATUS,
            status
        )

        StorageUtils.writeBooleanValueInPreference(
            requireContext(),
            AppConstants.PREF_KEY_SMS_STATUS,
            status
        )
    }
}