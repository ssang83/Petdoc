package kr.co.petdoc.petdoc.fragment.mypage

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_inquiry_write.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: InquiryPostFragment
 * Created by kimjoonsung on 2020/04/16.
 *
 * Description : 앱 불편사항 글쓰기 화면
 */
class InquiryPostFragment : BaseFragment() {

    private val LOGTAG = "InquiryPostFragment"
    private val QUESTION_UPLOAD_REQUEST = "$LOGTAG.questionUploadRequest"

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_inquiry_write, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnBack.setOnClickListener { requireActivity().onBackPressed() }
        btnPost.setOnClickListener {
            if(replyTitle.text.toString().isNotEmpty()
                && replySubject.text.toString().isNotEmpty()) {
                mApiClient.uploadQuestion(QUESTION_UPLOAD_REQUEST, replyTitle.text.toString(), replySubject.text.toString())
            } else {
                AppToast(requireActivity()).showToastMessage(
                    Helper.readStringRes(R.string.mypage_inquiry_write_post_error),
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(QUESTION_UPLOAD_REQUEST)) {
            mApiClient.cancelRequest(QUESTION_UPLOAD_REQUEST)
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
    fun onEventMainThread(event:NetworkBusResponse) {
        when(event.tag) {
            QUESTION_UPLOAD_REQUEST -> {
                if (event.status == "ok") {
                    replyTitle.setText("")
                    replySubject.setText("")

                    AppToast(requireContext()).showToastMessage(
                        "불편사항이 접수 되었습니다.",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                }
            }
        }
    }
}