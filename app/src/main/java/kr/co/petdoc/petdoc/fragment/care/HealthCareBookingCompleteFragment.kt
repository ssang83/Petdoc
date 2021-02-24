package kr.co.petdoc.petdoc.fragment.care

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import kotlinx.android.synthetic.main.fragment_health_care_booking_complete.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.enum.BookingStatus
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.HospitalHistoryResponse
import kr.co.petdoc.petdoc.network.response.submodel.HospitalHistoryData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: HealthCareBookingCompleteFragment
 * Created by kimjoonsung on 2020/09/09.
 *
 * Description :
 */
class HealthCareBookingCompleteFragment : BaseFragment() {

    private val TAG = "HealthCareBookingCompleteFragment"
    private val CLINIC_BOOKING_DETAIL_REQUEST = "$TAG.clinicBookingDetailRequest"

    private lateinit var dataModel: HospitalDataModel

    private var historyData: HospitalHistoryData? = null

    private var bookingId = -1

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_health_care_booking_complete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        bookingId = dataModel.bookingId.value!!

        btnClose.setOnClickListener {
            findNavController().navigate(R.id.action_healthCareBookingCompleteFragment_to_healthCareFragment)
        }
        btnConfirm.setOnClickListener {
            findNavController().navigate(R.id.action_healthCareBookingCompleteFragment_to_healthCareFragment)
        }

        mApiClient.getMyHospitalHistoryDetail(CLINIC_BOOKING_DETAIL_REQUEST, "CLINIC", bookingId)
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(CLINIC_BOOKING_DETAIL_REQUEST)) {
            mApiClient.cancelRequest(CLINIC_BOOKING_DETAIL_REQUEST)
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