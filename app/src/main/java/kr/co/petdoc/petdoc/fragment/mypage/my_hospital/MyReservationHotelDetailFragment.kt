package kr.co.petdoc.petdoc.fragment.mypage.my_hospital

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_reservation_detail.*
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
 * Class: ReservationDetailFragment
 * Created by kimjoonsung on 2020/04/16.
 *
 * Description : 예약 내역 상세 화면
 */
class MyReservationHotelDetailFragment : BaseFragment(){

    private val LOGTAG = "MyReservationHotelDetailFragment"
    private val BOOKING_DETAIL_REQUEST = "$LOGTAG.bookingDetailRequest"
    private val BOOKING_CANCEL_REQUEST = "$LOGTAG.bookingCancelRequest"

    private var historyData: HospitalHistoryData? = null

    private var bookingId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_reservation_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookingId = arguments?.getInt("id") ?: bookingId
        Logger.d("booking id : $bookingId")

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnCancel.setOnClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("예약상세_예약취소", "Click Event", "예약상세페이지 내 예약 취소버튼 클릭")
            mApiClient.bookingCancel(
                BOOKING_CANCEL_REQUEST,
                historyData?.bookingData?.centerId!!,
                "HOTEL",
                bookingId
            )
        }

        imageViewMap.setOnClickListener {
            // TODO : 구글 맵 이동
        }

        imageViewCall.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${historyData?.bookingData?.telephone}")))
        }

        mApiClient.getMyHospitalHistoryDetail(BOOKING_DETAIL_REQUEST, "HOTEL", bookingId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(BOOKING_DETAIL_REQUEST)) {
            mApiClient.cancelRequest(BOOKING_DETAIL_REQUEST)
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
            BOOKING_DETAIL_REQUEST -> {
                if (response is HospitalHistoryResponse) {
                    val code = response.code
                    val message = response.messageKey
                    if ("SUCCESS" == code) {
                        historyData = response.resultData

                        setBookingStatus(response.resultData.bookingData.statusType)

                        hospitalName.text = "브이케어 동물병원"
                        petName.text = "심바"
                        category.text = "호텔"
                        standingRoomDate.text = "입실 : 3월 11일 (화) 오후 3시"
                        checkoutDate.text = "퇴실 : 3월 12일 (수) 오후 3시"
                        room.text = "소형(1~5kg)"
                        feedPay.text = "기존 사료 급여"
                        comment.text = "간식은 주지 마세요."
                        guideline.text = "준비물 : 리드줄"

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
            BOOKING_CANCEL_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val mesageKey = JSONObject(event.response)["messageKey"].toString()
                    if ("SUCCESS" == code) {
                        AppToast(requireContext()).showToastMessage(
                            "정상적으로 예약 취소 되었습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )

                        Airbridge.trackEvent("mypage", "click", "book_cancel", null, null, null)
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

    //===============================================================================================

    private fun setBookingStatus(_status: String) {
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