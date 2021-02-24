package kr.co.petdoc.petdoc.fragment.care

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_health_care_visit_complete.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.HospitalHistoryResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.DnaVisitData
import kr.co.petdoc.petdoc.network.response.submodel.HospitalHistoryData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: HealthCareVisitCompleteFragment
 * Created by kimjoonsung on 2020/09/18.
 *
 * Description :
 */
class HealthCareVisitCompleteFragment : BaseFragment() {

    private val TAG = "HealthCareVisitCompleteFragment"
    private val DETAIL_REQUEST = "$TAG.detailRequest"
    private val BOOKING_DETAIL_REQUEST = "$TAG.bookingDetailRequest"

    private lateinit var dataModel: HospitalDataModel
    private var visitData: DnaVisitData? = null
    private var historyData: HospitalHistoryData? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_health_care_visit_complete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnClose.setOnClickListener { requireActivity().onBackPressed() }
        btnConfirm.setOnClickListener { requireActivity().onBackPressed() }

        mApiClient.getMyHospitalHistoryDetail(BOOKING_DETAIL_REQUEST, "CLINIC", dataModel.bookingId.value!!)
        mApiClient.getDnaVisitStatusRequest(DETAIL_REQUEST, dataModel.bookingId.value!!)
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(DETAIL_REQUEST)) {
            mApiClient.cancelRequest(DETAIL_REQUEST)
        }

        if (mApiClient.isRequestRunning(BOOKING_DETAIL_REQUEST)) {
            mApiClient.cancelRequest(BOOKING_DETAIL_REQUEST)
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

                        hospitalName.text = response.resultData.bookingData.centerName
                        petName.text = response.resultData.bookingData.petName

                        val vetName = response.resultData.bookingData.vetName
                        val vetPosition = response.resultData.bookingData.vetPosition
                        val roomNumber = response.resultData.bookingData.roomOrder
                        doctorOffice.text = "${vetName}${vetPosition}, ${roomNumber}번 진료실"
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
            DETAIL_REQUEST -> {
                if (event.status == "ok") {
                    val code = JSONObject(event.response)["code"]
                    val mesageKey = JSONObject(event.response)["messageKey"].toString()
                    val resultData = JSONObject(event.response)["resultData"] as JSONObject
                    if ("SUCCESS" == code) {
                        val json = resultData["dnaVisitInfo"] as JSONObject
                        visitData = Gson().fromJson(json.toString(), object : TypeToken<DnaVisitData>() {}.type)

                        val time = visitData?.collectReqDate?.split(" ")!!
                        bookingDate.text = "${calculateDate(time[0])} ${calculateTime(time[1])}"
                    } else {
                        Logger.d("error : $mesageKey")
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
}