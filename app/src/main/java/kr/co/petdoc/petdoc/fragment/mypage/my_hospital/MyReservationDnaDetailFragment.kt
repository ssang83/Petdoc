package kr.co.petdoc.petdoc.fragment.mypage.my_hospital

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_my_reservation_dna_detail.*
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
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: MyReservationDnaDetailFragment
 * Created by kimjoonsung on 2020/09/18.
 *
 * Description :
 */
class MyReservationDnaDetailFragment : BaseFragment() {

    private val TAG = "MyReservationDnaDetailFragment"
    private val BOOKING_CANCEL_REQUEST = "$TAG.bookingCancelRequest"
    private val BOOKING_DETAIL_REQUEST = "$TAG.bookingDetailRequest"

    private var historyData: HospitalHistoryData? = null

    private var bookingId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_reservation_dna_detail, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookingId = arguments?.getInt("id") ?: bookingId
        Logger.d("booking id : $bookingId")

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnCancel.setOnClickListener {
            FirebaseAPI(requireActivity()).logEventFirebase("예약상세_예약취소", "Click Event", "예약상세페이지 내 예약 취소버튼 클릭")
            mApiClient.cancelHealthCare(
                BOOKING_CANCEL_REQUEST,
                historyData?.bookingData?.centerId!!,
                bookingId
            )
        }

        //==========================================================================================

        mApiClient.getMyHospitalHistoryDetail(BOOKING_DETAIL_REQUEST, "CLINIC", bookingId)
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(BOOKING_DETAIL_REQUEST)) {
            mApiClient.cancelRequest(BOOKING_DETAIL_REQUEST)
        }

        if (mApiClient.isRequestRunning(BOOKING_CANCEL_REQUEST)) {
            mApiClient.cancelRequest(BOOKING_CANCEL_REQUEST)
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
    fun onEventMainThread(response: AbstractApiResponse) {
        when(response.requestTag) {
            BOOKING_DETAIL_REQUEST -> {
                if (response is HospitalHistoryResponse) {
                    val code = response.code
                    val message = response.messageKey
                    if ("SUCCESS" == code) {
                        historyData = response.resultData

                        setBookingStatus(response.resultData.bookingData.statusType)

                        hospitalName.text = response.resultData.bookingData.centerName
                        petName.text = response.resultData.bookingData.petName
                        category.text = response.resultData.bookingData.clinicName
                        description.text = "예약된 시간에 방문하여 혈액을 채취해주세요. 혈액검사소에 혈액이 접수되면 3~5일 후 검사결과가 등록됩니다."

                        val vetName = response.resultData.bookingData.vetName
                        val vetPosition = response.resultData.bookingData.vetPosition
                        val roomNumber = response.resultData.bookingData.roomOrder
                        doctorOffice.text = "${vetName}${vetPosition}, ${roomNumber}번 진료실"

                        val time = response.resultData.bookingData.bookingTime.split("T")
                        bookingDate.text = "${calculateDate(time[0])} ${calculateTime(time[1])}"

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
                            "예약이 취소되었습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )

                        Airbridge.trackEvent("mypage", "click", "book_cancel", null, null, null)
                        requireActivity().onBackPressed()
                    } else {
                        if (mesageKey == "no.cancel.use.inspection.ClinicBooking") {
                            AppToast(requireContext()).showToastMessage(
                                "요청하신 검사취소는 이미 진행중이므로, 취소가 불가능 합니다.",
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM
                            )
                        }
                    }
                }
            }
        }
    }

    //==============================================================================================

    private fun calculateDate(date: String) : String {
        val format1 = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val format = SimpleDateFormat("yyyy년 MM월 dd일 (E)", Locale.KOREA)
        val date = format1.parse(date.replace("-", ""))

        return format.format(date)
    }

    private fun calculateTime(time: String) : String {
        val format1 = SimpleDateFormat("HH:mm:ss", Locale.KOREA)
        val format = SimpleDateFormat("a kk:mm", Locale.KOREA)

        val time = format1.parse(time)

        return format.format(time)
    }

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