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
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MainActivity
import kr.co.petdoc.petdoc.adapter.mypage.MyHospitalHistoryAdapter
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.enum.BookingStatus
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.BookingHistoryListResponse
import kr.co.petdoc.petdoc.network.response.submodel.ReservationHistoryData
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: MyHospitalBookingFragment
 * Created by kimjoonsung on 2020/08/28.
 *
 * Description :
 */
class MyHospitalBookingFragment : BaseFragment(), MyHospitalHistoryAdapter.AdapterListener {

    private val LOGTAG = "MyHospitalBookingFragment"
    private val BOOKING_HISTORY_LIST_REQUEST = "$LOGTAG.bookingHistoryListRequest"

    private var mAdapter: MyHospitalHistoryAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_my_hospital, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnFindHospital.setOnClickListener {
            startActivity(Intent(requireActivity(), MainActivity::class.java).apply {
                addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
                putExtra("booking", "hospital")
            })

            requireActivity().finish()
        }

        mApiClient.getMyHospitalHistoryBookingList(BOOKING_HISTORY_LIST_REQUEST)
    }

    override fun onItemClicked(item: ReservationHistoryData) {
        Airbridge.trackEvent("mypage", "click", "book_list", null, null, null)
        FirebaseAPI(requireActivity()).logEventFirebase("접수예약내역_예약상세보기", "Click Event", "예약내역 상세보기 클릭")
        when (item.facilityType) {
            "HOTEL" -> {
                bundleOf("id" to item.bookingData.id).let {
                    findNavController().navigate(
                        R.id.action_myHospitalBookingFragment_to_reservationDetailFragment,
                        it
                    )
                }
            }

            "CLINIC" -> {
                if (item.bookingData.inspectionType == "99") {
                    bundleOf("id" to item.bookingData.id).let {
                        findNavController().navigate(
                            R.id.action_myHospitalBookingFragment_to_myReservationDnaDetailFragment,
                            it
                        )
                    }
                } else {
                    bundleOf("id" to item.bookingData.id).let {
                        findNavController().navigate(
                            R.id.action_myHospitalBookingFragment_to_myReservationClinicDetailFragment,
                            it
                        )
                    }
                }
            }

            "BEAUTY" -> {
                bundleOf("id" to item.bookingData.id).let {
                    findNavController().navigate(
                        R.id.action_myHospitalBookingFragment_to_myReservationBeautyDetailFragment,
                        it
                    )
                }
            }
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
            BOOKING_HISTORY_LIST_REQUEST -> {
                if (response is BookingHistoryListResponse) {
                    if ("SUCCESS" == response.code) {
                        if (response.resultData.size > 0) {
                            myHospitalList.visibility = View.VISIBLE
                            layoutEmpty.visibility = View.GONE

                            mAdapter = MyHospitalHistoryAdapter().apply {
                                setListener(this@MyHospitalBookingFragment)
                            }

                            myHospitalList.apply {
                                layoutManager = LinearLayoutManager(requireContext())
                                adapter = mAdapter
                            }

                            val bdItems = response.resultData.filter { it.bookingData.statusType == BookingStatus.BD.name }.toMutableList()
                            val brItems = response.resultData.filter { it.bookingData.statusType == BookingStatus.BR.name }.toMutableList()
                            val etcItems = response.resultData.filter { (it.bookingData.statusType != BookingStatus.BD.name) && (it.bookingData.statusType != BookingStatus.BR.name) }.toMutableList()

                            val joinItems:MutableList<ReservationHistoryData> = mutableListOf()
                            joinItems.addAll(bdItems)
                            joinItems.addAll(brItems)
                            joinItems.addAll(etcItems)

                            mAdapter?.updateData(joinItems)
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