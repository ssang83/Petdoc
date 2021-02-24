package kr.co.petdoc.petdoc.fragment.hospital

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_clinic_reservation_complete.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MyPageActivity
import kr.co.petdoc.petdoc.activity.hospital.ClinicReservationCompleteActivity
import kr.co.petdoc.petdoc.enum.BookingStatus
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.HospitalHistoryResponse
import kr.co.petdoc.petdoc.network.response.submodel.HospitalHistoryData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import kr.co.petdoc.petdoc.widget.OnSingleClickListener
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: ClinicReservationCompleteFragment
 * Created by kimjoonsung on 2020/06/19.
 *
 * Description :
 */
class ClinicReservationCompleteFragment : BaseFragment() {

    private val TAG = "ClinicReservationCompleteFragment"
    private val CLINIC_BOOKING_DETAIL_REQUEST = "$TAG.clinicBookingDetailRequest"

    private lateinit var dataModel: HospitalDataModel
    private var historyData: HospitalHistoryData? = null

    private var bookingId = -1
    private var fromHome = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 255, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_clinic_reservation_complete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        Airbridge.trackEvent("booking", "click", "book_done", null, null, null)

        fromHome = arguments?.getBoolean("fromHome", fromHome)!!

        if (!fromHome) {
            bookingId = dataModel.bookingId.value!!
        } else {
            bookingId = arguments?.getInt("bookingId", bookingId)!!
        }

        //==========================================================================================
        btnClose.setOnSingleClickListener {
            if (!fromHome) {
                requireActivity().finish()
            } else {
                requireActivity().onBackPressed()
            }
        }

        imageViewMap.setOnSingleClickListener {
            ClinicReservationCompleteActivity.instance.clickedOnMap()
        }

        imageViewCall.setOnSingleClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${historyData?.bookingData?.telephone}")))
        }

        btnMyPage.setOnSingleClickListener {
            ClinicReservationCompleteActivity.instance.finishAllActivity()
            startActivity(Intent(requireActivity(), MyPageActivity::class.java).apply {
                putExtra("fromBooking", true)
            })
        }

        //==========================================================================================

        mApiClient.getMyHospitalHistoryDetail(CLINIC_BOOKING_DETAIL_REQUEST, "CLINIC", bookingId)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(CLINIC_BOOKING_DETAIL_REQUEST)) {
            mApiClient.cancelRequest(CLINIC_BOOKING_DETAIL_REQUEST)
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
        when (response.requestTag) {
            CLINIC_BOOKING_DETAIL_REQUEST -> {
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

    private fun calculateDate(date: String) : String {
        val format1 = SimpleDateFormat("yyyyMMdd", Locale.KOREA)
        val format = SimpleDateFormat("MM월dd일", Locale.KOREA)
        val date = format1.parse(date.replace("-", ""))

        return format.format(date)
    }

    private fun calculateTime(time: String) : String {
        val format1 = SimpleDateFormat("HH:mm:ss", Locale.KOREA)
        val format = SimpleDateFormat("HH시mm분", Locale.KOREA)

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