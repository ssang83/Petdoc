package kr.co.petdoc.petdoc.fragment.mypage.my_hospital

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_my_hospital.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.hospital.HospitalActivity
import kr.co.petdoc.petdoc.adapter.mypage.MyHospitalHistoryAdapter
import kr.co.petdoc.petdoc.adapter.mypage.MyHospitalHistoryRegisterAdapter
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.BookingStatus
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.BookingHistoryListResponse
import kr.co.petdoc.petdoc.network.response.RegisterHistoryListResponse
import kr.co.petdoc.petdoc.network.response.submodel.RegisterHistoryData
import kr.co.petdoc.petdoc.network.response.submodel.ReservationHistoryData
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: MyHospitalFragment
 * Created by kimjoonsung on 2020/04/14.
 *
 * Description : 접수/예약 탭 화면
 */
class MyHospitalRegisterFragment : BaseFragment(),
    MyHospitalHistoryRegisterAdapter.AdapterListener {

    private val LOGTAG = "MyHospitalRegisterFragment"
    private val REGISTER_HISTORY_LIST_REQUEST = "$LOGTAG.registerHistoryListRequest"

    private var mRegisterAdapter: MyHospitalHistoryRegisterAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_hospital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnFindHospital.setOnClickListener {
            startActivity(Intent(requireActivity(), HospitalActivity::class.java).apply {
                putExtra("latitude", PetdocApplication.currentLat.toString())
                putExtra("longitude", PetdocApplication.currentLng.toString())
            })
        }

        emptyText.text = Helper.readStringRes(R.string.mypage_reception_empty)

        mApiClient.getMyHospitalHistoryRegisterList(REGISTER_HISTORY_LIST_REQUEST)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(REGISTER_HISTORY_LIST_REQUEST)) {
            mApiClient.cancelRequest(REGISTER_HISTORY_LIST_REQUEST)
        }
    }

    override fun onRegisterItemClicked(item: RegisterHistoryData) {
        Airbridge.trackEvent("mypage", "click", "receipt_list", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("접수예약내역_접수상세보기", "Click Event", "접수내역 상세보기 클릭")
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
            REGISTER_HISTORY_LIST_REQUEST -> {
                if (response is RegisterHistoryListResponse) {
                    if ("SUCCESS" == response.code) {
                        if (response.resultData.size > 0) {
                            myHospitalList.visibility = View.VISIBLE
                            layoutEmpty.visibility = View.GONE

                            mRegisterAdapter = MyHospitalHistoryRegisterAdapter().apply {
                                setListener(this@MyHospitalRegisterFragment)
                            }

                            myHospitalList.apply {
                                layoutManager = LinearLayoutManager(requireContext())
                                adapter = mRegisterAdapter
                            }

                            val rdItems = response.resultData.filter { it.bookingData.statusType == BookingStatus.RD.name }.toMutableList()
                            val etcItems = response.resultData.filter { it.bookingData.statusType != BookingStatus.RD.name }.toMutableList()

                            val joinItems:MutableList<RegisterHistoryData> = mutableListOf()
                            joinItems.addAll(rdItems)
                            joinItems.addAll(etcItems)

                            mRegisterAdapter?.updateData(joinItems)
                        } else {
                            myHospitalList.visibility = View.GONE
                            layoutEmpty.visibility = View.VISIBLE
                        }
                    } else {
                        Logger.d("error : ${response.messageKey}")
                    }
                }
            }
        }
    }
}