package kr.co.petdoc.petdoc.fragment.hospital

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.ViewModelProvider
import kotlinx.android.synthetic.main.fragment_beauty_reservation_complete.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MyPageActivity
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.viewmodel.HospitalDataModel
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: BeautyReservationCompleteFragment
 * Created by kimjoonsung on 2020/06/19.
 *
 * Description :
 */
class BeautyReservationCompleteFragment : BaseFragment() {

    private val TAG = "BeautyReservationCompleteFragment"
    private val BEAUTY_BOOKING_DETAIL_REQUEST = "$TAG.beautyBookingDetailRequest"

    private lateinit var dataModel: HospitalDataModel

    private var bookingId = -1
    private var fromHome = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 255, fullscreen = true)
        dataModel = ViewModelProvider(requireActivity()).get(HospitalDataModel::class.java)
        return inflater.inflate(R.layout.fragment_beauty_reservation_complete, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)
        super.onViewCreated(view, savedInstanceState)

        fromHome = arguments?.getBoolean("fromHome", fromHome)!!

        if (!fromHome) {
            bookingId = dataModel.bookingId.value!!
        } else {
            bookingId = arguments?.getInt("bookingId", bookingId)!!
        }

        //==========================================================================================
        btnClose.setOnClickListener { requireActivity().onBackPressed() }

        imageViewMap.setOnClickListener {

        }

        imageViewCall.setOnClickListener {
            startActivity(Intent(Intent.ACTION_DIAL, Uri.parse("tel:$0212345678")))
        }

        btnMyPage.setOnClickListener {
            startActivity(Intent(requireActivity(), MyPageActivity::class.java))
        }

        //==========================================================================================
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(BEAUTY_BOOKING_DETAIL_REQUEST)) {
            mApiClient.cancelRequest(BEAUTY_BOOKING_DETAIL_REQUEST)
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
            BEAUTY_BOOKING_DETAIL_REQUEST -> {

            }
        }
    }
}