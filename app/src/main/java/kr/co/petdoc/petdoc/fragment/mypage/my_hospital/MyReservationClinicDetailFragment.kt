package kr.co.petdoc.petdoc.fragment.mypage.my_hospital

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.dialog_one_button.*
import kotlinx.android.synthetic.main.fragment_my_reservation_clinic_detail.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MainActivity
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.BookingStatus
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.HospitalHistoryResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.HospitalHistoryData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.TwoBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: MyReservationClinicDetailFragment
 * Created by kimjoonsung on 2020/08/03.
 *
 * Description :
 */
class MyReservationClinicDetailFragment : BaseFragment() {
    private val LOGTAG = "MyReservationClinicDetailFragment"
    private val BOOKING_DETAIL_REQUEST = "$LOGTAG.bookingDetailRequest"
    private val BOOKING_CANCEL_REQUEST = "$LOGTAG.bookingCancelRequest"

    private var historyData: HospitalHistoryData? = null

    private var bookingId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_reservation_clinic_detail, container, false)
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
                "CLINIC",
                bookingId
            )
        }

        imageViewMap.setOnClickListener {
            startActivity(Intent(requireActivity(), MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("booking", "hospital")
            })

            requireActivity().finish()
        }

        imageViewCall.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${historyData?.bookingData?.telephone}")))
        }

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

                        val vetName = response.resultData.bookingData.vetName
                        val vetPosition = response.resultData.bookingData.vetPosition
                        val roomNumber = response.resultData.bookingData.roomOrder
                        doctorOffice.text = "${vetName}${vetPosition}, ${roomNumber}번 진료실"

                        comment.text = response.resultData.bookingData.memo

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
    fun onEventMainThread(event:NetworkBusResponse) {
        when (event.tag) {
            BOOKING_CANCEL_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    if ("SUCCESS" == code) {
                        AppToast(requireContext()).showToastMessage(
                            "예약이 취소되었습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )

                        Airbridge.trackEvent("mypage", "click", "book_cancel", null, null, null)
                        requireActivity().onBackPressed()
                    } else {
                        TwoBtnDialog(requireContext()).apply {
                            setTitle("예약취소")
                            setMessage("동물병원 예약이 확정되어,\n예약취소가 불가능합니다.\n동물병원에 문의하세요.")
                            setConfirmButton("전화하기", View.OnClickListener {
                                startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${historyData?.bookingData?.telephone}")))
                                dismiss()
                            })
                            setCancelButton("닫기", View.OnClickListener {
                                dismiss()
                            })
                        }.show()
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