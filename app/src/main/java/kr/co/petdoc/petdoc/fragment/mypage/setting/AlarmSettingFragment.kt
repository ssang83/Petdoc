package kr.co.petdoc.petdoc.fragment.mypage.setting

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import kotlinx.android.synthetic.main.fragment_alarm_setting.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.event.ApiErrorEvent
import kr.co.petdoc.petdoc.network.event.ApiErrorWithMessageEvent
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode

/**
 * Petdoc
 * Class: AlarmSettingFragment
 * Created by kimjoonsung on 2020/04/13.
 *
 * Description : 알림 설정 화면
 */
class AlarmSettingFragment : BaseFragment() {

    private val LOGTAG = "AlarmSettingFragment"
    private val ALARM_STATUS_REQUEST = "$LOGTAG.alramStatusRequest"

    private val flagBox = booleanArrayOf(false, false, false)

    private var isChat = "false"
    private var isComment = "false"
    private var isGrade = "false"
    private var isAllChecked = false

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_alarm_setting, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        btnAlarmAll.setOnClickListener {
            if (btnAlarmAll.isChecked) {
                btnChattingAlarm.isChecked = true
                btnReplyAlarm.isChecked = true
                btnLevelAlarm.isChecked = true

                isAllChecked = true
            } else {
                btnChattingAlarm.isChecked = false
                btnReplyAlarm.isChecked = false
                btnLevelAlarm.isChecked = false

                isAllChecked = false
            }

            isChat = isAllChecked.toString()
            isComment = isAllChecked.toString()
            isGrade = isAllChecked.toString()

            flagBox[0] = isAllChecked
            flagBox[1] = isAllChecked
            flagBox[2] = isAllChecked

            checkAllStatus()
            sendPushStatus()

            StorageUtils.writeBooleanValueInPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_CHAT_NOTI_STATUS,
                    isAllChecked
            )

            StorageUtils.writeBooleanValueInPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_COMMENT_NOTI_STATUS,
                    isAllChecked
            )

            StorageUtils.writeBooleanValueInPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_GRADE_NOTI_STATUS,
                    isAllChecked
            )
        }

        btnChattingAlarm.setOnCheckedChangeListener { buttonView, isChecked ->
            Logger.d("chatting : $isChecked")
            isChat = isChecked.toString()

            if(isChecked) {
                flagBox[0] = true
            } else {
                flagBox[0] = false
            }

            checkAllStatus()
            sendPushStatus()

            StorageUtils.writeBooleanValueInPreference(
                requireContext(),
                AppConstants.PREF_KEY_CHAT_NOTI_STATUS,
                isChecked)
        }

        btnReplyAlarm.setOnCheckedChangeListener { buttonView, isChecked ->
            Logger.d("reply : $isChecked")
            isComment = isChecked.toString()

            if (isChecked) {
                flagBox[1] = true
            } else {
                flagBox[1] = false
            }

            checkAllStatus()
            sendPushStatus()

            StorageUtils.writeBooleanValueInPreference(
                requireContext(),
                AppConstants.PREF_KEY_COMMENT_NOTI_STATUS,
                isChecked
            )
        }

        btnLevelAlarm.setOnCheckedChangeListener { buttonView, isChecked ->
            Logger.d("level : $isChecked")
            isGrade = isChecked.toString()

            if (isChecked) {
                flagBox[2] = true
            } else {
                flagBox[2] = false
            }

            checkAllStatus()
            sendPushStatus()

            StorageUtils.writeBooleanValueInPreference(
                requireContext(),
                AppConstants.PREF_KEY_GRADE_NOTI_STATUS,
                isChecked
            )
        }

        btnBack.setOnClickListener { requireActivity().onBackPressed() }

        btnChattingAlarm.isChecked = StorageUtils.loadBooleanValueFromPreference(
            requireContext(),
            AppConstants.PREF_KEY_CHAT_NOTI_STATUS
        )

        btnReplyAlarm.isChecked = StorageUtils.loadBooleanValueFromPreference(
            requireContext(),
            AppConstants.PREF_KEY_COMMENT_NOTI_STATUS
        )

        btnLevelAlarm.isChecked = StorageUtils.loadBooleanValueFromPreference(
            requireContext(),
            AppConstants.PREF_KEY_GRADE_NOTI_STATUS
        )
    }

    override fun onDestroyView() {
        super.onDestroyView()
        if (mApiClient.isRequestRunning(ALARM_STATUS_REQUEST)) {
            mApiClient.cancelRequest(ALARM_STATUS_REQUEST)
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
            ALARM_STATUS_REQUEST -> {
                if (event.status == "ok") {
                    Logger.d("${event.response}")
                }
            }
        }
    }

    //==============================================================================================

    private fun setAllBtn(status: Boolean) {
        btnChattingAlarm.isChecked = status
        btnReplyAlarm.isChecked = status
        btnLevelAlarm.isChecked = status

        isChat = status.toString()
        isComment = status.toString()
        isGrade = status.toString()

        StorageUtils.writeBooleanValueInPreference(
            requireContext(),
            AppConstants.PREF_KEY_CHAT_NOTI_STATUS,
            status
        )

        StorageUtils.writeBooleanValueInPreference(
            requireContext(),
            AppConstants.PREF_KEY_COMMENT_NOTI_STATUS,
            status
        )

        StorageUtils.writeBooleanValueInPreference(
            requireContext(),
            AppConstants.PREF_KEY_GRADE_NOTI_STATUS,
            status
        )

        sendPushStatus()
    }

    private fun checkAllStatus() {
        if (flagBox[0] && flagBox[1] && flagBox[2]) {
            btnAlarmAll.isChecked = true
            btnChattingAlarm.isChecked = true
            btnReplyAlarm.isChecked = true
            btnLevelAlarm.isChecked = true
        } else {
            btnAlarmAll.isChecked = false
            if (flagBox[0]) {
                btnChattingAlarm.isChecked = true
            } else {
                btnChattingAlarm.isChecked = false
            }

            if(flagBox[1]) {
                btnReplyAlarm.isChecked = true
            } else {
                btnReplyAlarm.isChecked = false
            }

            if (flagBox[2]) {
                btnLevelAlarm.isChecked = true
            } else {
                btnLevelAlarm.isChecked = false
            }
        }
    }

    private fun sendPushStatus() {
        Logger.d("isChat : $isChat, isComment : $isComment, isGrade : $isGrade")
        mApiClient.putPushStatus(ALARM_STATUS_REQUEST, isChat, isComment, isGrade)
    }

}