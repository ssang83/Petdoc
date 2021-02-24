package kr.co.petdoc.petdoc.fragment.hospital

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.adapter_clinic_room_item.view.*
import kotlinx.android.synthetic.main.adapter_hospital_item.view.*
import kotlinx.android.synthetic.main.fragment_register_complete.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MyPageActivity
import kr.co.petdoc.petdoc.enum.BookingStatus
import kr.co.petdoc.petdoc.enum.RoomStatus
import kr.co.petdoc.petdoc.enum.RunStatus
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.HospitalHistoryResponse
import kr.co.petdoc.petdoc.network.response.submodel.HospitalHistoryData
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject

/**
 * Petdoc
 * Class: RegisterCompleteFragment
 * Created by kimjoonsung on 2020/06/19.
 *
 * Description :
 */
class RegisterCompleteFragment : BaseFragment() {

    private val TAG = "RegisterCompleteFragment"
    private val REGISTER_DETAIL_REQUEST = "$TAG.registerDetailRequest"

    private lateinit var dataModel: HospitalDataModel

    private var historyData:HospitalHistoryData? = null

    private var registerId = -1
    private var fromHome = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 255, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_register_complete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        Airbridge.trackEvent("booking", "click", "receipt_done", null, null, null)

        fromHome = arguments?.getBoolean("fromHome", fromHome)!!

        if (!fromHome) {
            registerId = dataModel.registerId.value!!
        } else {
            registerId = arguments?.getInt("registerId", registerId)!!
        }

        //==========================================================================================
        btnClose.setOnClickListener {
            if (!fromHome) {
                requireActivity().onBackPressed()
            } else {
                requireActivity().finish()
            }
        }

        imageViewMap.setOnClickListener {

        }

        imageViewCall.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:${historyData?.bookingData?.telephone}")))
        }

        btnMyPage.setOnClickListener {
            startActivity(Intent(requireActivity(), MyPageActivity::class.java))
        }

        //==========================================================================================

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
        when (response.requestTag) {
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
                        officeStatus.text = "원활"

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