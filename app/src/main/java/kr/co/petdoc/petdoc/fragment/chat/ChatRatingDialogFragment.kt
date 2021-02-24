package kr.co.petdoc.petdoc.fragment.chat

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import androidx.fragment.app.DialogFragment
import kotlinx.android.synthetic.main.dialog_chat_rating.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: ChatRatingDialogFragment
 * Created by kimjoonsung on 2020/06/02.
 *
 * Description :
 */
class ChatRatingDialogFragment : DialogFragment() {

    private val LOGTAG = "ChatRatingDialogFragment"
    private val CHAT_RATING_REQUEST = "${LOGTAG}.chatRatingRequest"

    private lateinit var mEventBus: EventBus

    private var roomId:Int = -1
    private var rate = ""
    private var displayName = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        if (dialog != null && dialog!!.window != null) {
            dialog!!.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
            dialog!!.window!!.requestFeature(Window.FEATURE_NO_TITLE)
            dialog!!.window!!.setGravity(Gravity.BOTTOM)
        }
        return inflater.inflate(R.layout.dialog_chat_rating, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mEventBus = EventBus.getDefault()
        if(!mEventBus.isRegistered(this)) {
            mEventBus.register(this)
        }

        btnCancel.setOnClickListener { dismiss() }
        btnComplete.setOnClickListener {
            PetdocApplication.application.apiClient.putChatReview(CHAT_RATING_REQUEST, roomId, rate)
        }

        //=========================================================================================
        roomId = arguments?.getInt("roomId") ?: roomId
        displayName = arguments?.getString("displayName") ?: displayName
        Logger.d("roomId : $roomId, displayName : $displayName")

        rating.setOnRatingChangeListener { ratingBar, rating, fromUser ->
            Logger.d("Rating : ${rating.toInt()}")
            rate = rating.toInt().toString()
        }

        desc.text = "${displayName}과 상담이 종료되었습니다. 상담해준 원장님께 감사한 마음을 표현해주세요!"
    }

    override fun dismiss() {
        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this)
        }

        if (PetdocApplication.application.apiClient.isRequestRunning(CHAT_RATING_REQUEST)) {
            PetdocApplication.application.apiClient.cancelRequest(CHAT_RATING_REQUEST)
        }

        super.dismiss()
    }

    // ============================================================================================
    // EventBus callbacks
    // ============================================================================================
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            CHAT_RATING_REQUEST -> {
                if ("ok" == event.status) {
                    dismiss()
                }
            }
        }
    }
}