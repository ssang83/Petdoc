package kr.co.petdoc.petdoc.fragment.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: PetTalkFragment
 * Created by kimjoonsung on 2020/04/02.
 *
 * Description : 홈 펫톡 화면
 */
class PetTalkFragment : BaseFragment() {

    private val LOGTAG = "PetTalkFragment"
    private val REQUEST_TAG = "$LOGTAG.request"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_pet_talk, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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