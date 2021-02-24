package kr.co.petdoc.petdoc.fragment.mypage.my_hospital

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_reception_detail.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.BookingStatus
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.HospitalHistoryResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.HospitalHistoryData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: MyHospitalDetailFragment
 * Created by kimjoonsung on 2020/04/16.
 *
 * Description : 접수 내역 상세 화면
 */
class ReceptionDetailFragment : BaseFragment() {

    private val LOGTAG = "ReceptionDetailFragment"
    private val REGISTER_DETAIL_REQUEST = "$LOGTAG.registerDetailRequest"
    private val REGISTER_CANCEL_REQUEST = "$LOGTAG.registerCancelRequest"

    private var historyData: HospitalHistoryData? = null

    private var registerId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        return inflater.inflate(R.layout.fragment_reception_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)
        super.onViewCreated(view, savedInstanceState)

        registerId = arguments?.getInt("id") ?: registerId
        Logger.d("register id : $registerId")

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnCancel.setOnClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("접수상세_접수취소", "Click Event", "접수상세페이지 내 접수 취소버튼 클릭")
            mApiClient.bookingCancel(
                REGISTER_CANCEL_REQUEST,
                historyData?.bookingData?.centerId!!,
                historyData?.facilityType!!,
                registerId
            )
        }

        imageViewMap.setOnClickListener {
            // TODO : 구글 맵 실행
        }

        imageViewCall.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${historyData?.bookingData?.telephone}")))
        }

        mApiClient.getMyHospitalHistoryDetail(REGISTER_DETAIL_REQUEST, "CLINIC", registerId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(REGISTER_DETAIL_REQUEST)) {
            mApiClient.cancelRequest(REGISTER_DETAIL_REQUEST)
        }
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
    fun onEventMainThread(response: AbstractApiResponse) {
        when(response.requestTag) {
            REGISTER_DETAIL_REQUEST -> {
                if (response is HospitalHistoryResponse) {
                    val code = response.code
                    val message = response.messageKey
                    if ("SUCCESS" == code) {
                        historyData = response.resultData

                        setResigerStatus(response.resultData.bookingData.statusType)

                        hospitalName.text = response.resultData.bookingData.centerName
                        petName.text = response.resultData.bookingData.petName
                        category.text = response.resultData.bookingData.clinicName

                        val vetName = response.resultData.bookingData.vetName
                        val vetPosition = response.resultData.bookingData.vetPosition
                        val roomNumber = response.resultData.bookingData.roomOrder
                        doctorOffice.text = "${vetName}${vetPosition}, ${roomNumber}번 진료실"

                        comment.text = response.resultData.bookingData.memo
                    } else {
                        Logger.d("error : $message")
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            REGISTER_CANCEL_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val mesageKey = JSONObject(event.response)["messageKey"].toString()
                    if ("SUCCESS" == code) {
                        AppToast(requireContext()).showToastMessage(
                            "정상적으로 접수 취소 되었습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )

                        Airbridge.trackEvent("mypage", "click", "receipt_cancel", null, null, null)
                    } else {
                        AppToast(requireContext()).showToastMessage(
                            mesageKey,
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }
                }
            }
        }
    }

    //==============================================================================================

    private fun setResigerStatus(_status: String) {
        status.apply {
            when (_status) {
                BookingStatus.RD.name -> {
                    text = BookingStatus.RD.status
                }
                BookingStatus.RC.name -> {
                    text = BookingStatus.RC.status
                }
                BookingStatus.BD.name -> {
                    text = BookingStatus.BD.status
                }
                BookingStatus.RV.name -> {
                    text = BookingStatus.RV.status
                }
                BookingStatus.BR.name -> {
                    text = BookingStatus.BR.status
                }
                BookingStatus.BB.name -> {
                    text = BookingStatus.BB.status
                }
                BookingStatus.BC.name -> {
                    text = BookingStatus.BC.status
                }
                BookingStatus.BS.name -> {
                    text = BookingStatus.BS.status
                }
                BookingStatus.BV.name -> {
                    text = BookingStatus.BV.status
                }
                BookingStatus.NV.name -> {
                    text = BookingStatus.NV.status
                }
            }
        }
    }
}