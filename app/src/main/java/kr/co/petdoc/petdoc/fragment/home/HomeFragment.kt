package kr.co.petdoc.petdoc.fragment.home

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_home.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.MatchFoodActivity
import kr.co.petdoc.petdoc.activity.MyPageActivity
import kr.co.petdoc.petdoc.enum.PetAddType
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: HomeFragment
 * Created by kimjoonsung on 2020/04/02.
 *
 * Description : 홈 전체 화면
 */
class HomeFragment : BaseFragment() {

    private val LOGTAG = "HomeFragment"
    private val REQUEST_TAG = "$LOGTAG.request"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        test.setOnClickListener {
            startActivity(Intent(requireActivity(), MatchFoodActivity::class.java).apply {
                putExtra("type", PetAddType.ADD.ordinal)
            })
        }
        mypage.setOnClickListener { startActivity(Intent(requireActivity(), MyPageActivity::class.java)) }
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
            REQUEST_TAG -> {

            }
        }
    }

    /**
     * EventBus listener. An API call failed. No error message was returned.
     *
     * @param event ApiErrorEvent
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ApiErrorEvent) {
        when(event.requestTag) {
            REQUEST_TAG -> {

            }
        }
    }

    /**
     * EventBus listener. An API call failed. An error message was returned.
     *
     * @param event ApiErrorWithMessageEvent Contains the error message.
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: ApiErrorWithMessageEvent) {
        when(event.requestTag) {
            REQUEST_TAG -> {

            }
        }
    }
}