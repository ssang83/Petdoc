package kr.co.petdoc.petdoc.fragment.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import kotlinx.android.synthetic.main.fragment_pet_point.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.adapter.mypage.PetPointAdapter
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.UserPointLogListResponse
import kr.co.petdoc.petdoc.network.response.UserPointResponse
import kr.co.petdoc.petdoc.utils.Helper
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: PetPointFragment
 * Created by kimjoonsung on 2020/04/13.
 *
 * Description : 펫 포인트 화면
 */
class PetPointFragment : BaseFragment() {

    private val LOGTAG = "PetPointFragment"
    private val POINT_REQUEST = "$LOGTAG.pointRequest"
    private val POINT_LOG_LIST_REQUEST = "$LOGTAG.pointLogListRequest"

    private lateinit var mAdapter:PetPointAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pet_point, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mAdapter = PetPointAdapter()
        pointList.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = mAdapter
        }

        btnTop.setOnClickListener { pointList.scrollToPosition(0) }
        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        layoutHistoryType.setOnClickListener { Logger.d("point history type change") }

        mApiClient.getUserPoint(POINT_REQUEST)
        mApiClient.getUserPointLogList(POINT_LOG_LIST_REQUEST)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(POINT_REQUEST)) {
            mApiClient.cancelRequest(POINT_REQUEST)
        }

        if (mApiClient.isRequestRunning(POINT_LOG_LIST_REQUEST)) {
            mApiClient.cancelRequest(POINT_LOG_LIST_REQUEST)
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
            POINT_REQUEST -> {
                if (response is UserPointResponse) {
                    if ("SUCCESS" == response.code) {
                        usedPoint.text = "${Helper.ToPriceFormat(response.resultData.eNABPOAMT.toInt())}P"
                    } else {
                        usedPoint.text = "0P"
                    }
                }
            }

            POINT_LOG_LIST_REQUEST -> {
                if (response is UserPointLogListResponse) {
                    if ("SUCCESS" == response.code) {
                        if (response.resultData.poList.size > 0) {
                            pointList.visibility = View.VISIBLE
                            textViewEmpty.visibility = View.GONE
                            btnTop.visibility = View.VISIBLE

                            savedPoint.text = "${Helper.ToPriceFormat(response.resultData.poList[0].bALANCEAMT)}P"

                            mAdapter.upateData(response.resultData.poList)
                        } else {
                            pointList.visibility = View.GONE
                            textViewEmpty.visibility = View.VISIBLE
                            btnTop.visibility = View.GONE

                            savedPoint.text = "0P"
                        }
                    } else {
                        savedPoint.text = "0P"
                    }
                }
            }
        }
    }
}