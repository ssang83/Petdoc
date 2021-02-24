package kr.co.petdoc.petdoc.fragment.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import co.ab180.airbridge.Airbridge
import kotlinx.android.synthetic.main.fragment_find_password.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.event.SoftKeyboardBus
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.regex.Pattern


/**
 * petdoc-android
 * Class: FindPassWordFragment
 * Created by sungminkim on 2020/04/07.
 *
 * Description : 로그인 액티비티 -> 비밀번호찾기(이메일),  현재는 네비게이터에 등록되어 있음
 */

private const val UI_STATE_DEFAULT = 0
private const val UI_STATE_AUTH_IN_PROGRESS = 1
private const val UI_STATE_AUTH_COMPLETE = 2

class FindPassWordFragment : BaseFragment() {

    private val TAG = "FindPassWordFragment"
    private val AUTH_CODE_SEND_REQUEST = "$TAG.sendAuthCodeSendRequest"
    private val AUTH_CODE_RESEND_REQUEST = "$TAG.sendAuthCodeReSendRequest"
    private val AUTH_CODE_CONFIRM_REQUEST = "$TAG.sendAuthCodeConfirmRequest"
    private val NEW_PASSWORD_SEND_REQUEST = "$TAG.newPasswordSendRequest"

    private var emailAvailable = false
    private var authCodeFlag = false

//    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null

    private var passwordChangable = booleanArrayOf(false,false)
    private val passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&.]).{8,16}$") // 영문, 숫자, 특수문자 조합

    private var step = UI_STATE_DEFAULT
    private var userEmail = ""
    private var confirmCode = ""

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_find_password, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        Airbridge.trackEvent("find", "click", "find_pw", null, null, null)

        btnResend.setOnClickListener {
            mApiClient.sendAuthCode(AUTH_CODE_RESEND_REQUEST, userEmail)
        }

        btnFind.isEnabled = false
        readyUIandEvent()
        updateUI()

        // 소프트 키보드가 변경 될 경우 Bus 로 data 가 온다.
//        mKeyboardHeightProvider = KeyboardHeightProvider(requireActivity())
//        Handler().postDelayed( { mKeyboardHeightProvider!!.start() }, 1000)
    }

    override fun onPause() {
        super.onPause()

        if (email.isFocusable) {
            hideKeyboard(email)
        }

    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(AUTH_CODE_SEND_REQUEST)) {
            mApiClient.cancelRequest(AUTH_CODE_SEND_REQUEST)
        }

        if (mApiClient.isRequestRunning(AUTH_CODE_RESEND_REQUEST)) {
            mApiClient.cancelRequest(AUTH_CODE_RESEND_REQUEST)
        }

        if (mApiClient.isRequestRunning(AUTH_CODE_CONFIRM_REQUEST)) {
            mApiClient.cancelRequest(AUTH_CODE_CONFIRM_REQUEST)
        }

        if (mApiClient.isRequestRunning(NEW_PASSWORD_SEND_REQUEST)) {
            mApiClient.cancelRequest(NEW_PASSWORD_SEND_REQUEST)
        }
        super.onDestroyView()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            AUTH_CODE_SEND_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val mesageKey = JSONObject(event.response)["messageKey"].toString()
                    if ("SUCCESS" == code) {
                        val obj = JSONObject(event.response)["resultData"] as JSONObject
                        confirmCode = obj["confirmCode"].toString()

                        hideKeyboard(email)
                        step = UI_STATE_AUTH_IN_PROGRESS
                        updateUI()
                    } else {
                        AppToast(requireContext()).showToastMessage(
                            getFailReasonByMessageKey(mesageKey),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }
                }
            }

            AUTH_CODE_RESEND_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val mesageKey = JSONObject(event.response)["messageKey"].toString()
                    if ("SUCCESS" == code) {
                        step = UI_STATE_AUTH_IN_PROGRESS
                        AppToast(requireContext()).showToastMessage(
                            "인증번호가 재전송 되었습니다. 이메일을 확인해 주세요.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    } else {
                        AppToast(requireContext()).showToastMessage(
                            getFailReasonByMessageKey(mesageKey),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }
                }
            }

            AUTH_CODE_CONFIRM_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val mesageKey = JSONObject(event.response)["messageKey"].toString()
                    if ("SUCCESS" == code) {
                        hideKeyboard(editAuthCode)
                        step = UI_STATE_AUTH_COMPLETE
                        updateUI()
                    } else {
                        AppToast(requireContext()).showToastMessage(
                            getFailReasonByMessageKey(mesageKey),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }
                }
            }

            NEW_PASSWORD_SEND_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val mesageKey = JSONObject(event.response)["messageKey"].toString()
                    if ("SUCCESS" == code) {
                        requireActivity().onBackPressed()
                    } else {
                        AppToast(requireContext()).showToastMessage(
                                getFailReasonByMessageKey(mesageKey),
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM
                        )
                    }
                }
            }
        }
    }

    private fun getFailReasonByMessageKey(messageKey: String) =
            when (messageKey) {
                "102" -> getString(R.string.err_msg_email_does_not_exist)
                "107" -> getString(R.string.err_msg_auth_code_not_matched)
                else -> messageKey
            }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(bus: SoftKeyboardBus) {
        Logger.e("height:" + bus.height)

        if (bus.height > 100) {
            emptyView?.layoutParams?.height = bus.height
            emptyView?.invalidate()
            emptyView?.requestLayout()
        } else {
            emptyView?.layoutParams?.height = 1
            emptyView?.invalidate()
            emptyView?.requestLayout()
        }
    }

    private fun readyUIandEvent() {
        btnFind.setOnClickListener {
            when (step) {
                UI_STATE_DEFAULT -> {
                    userEmail = email.text.toString()
                    mApiClient.sendAuthCode(AUTH_CODE_SEND_REQUEST, userEmail)
                }
                UI_STATE_AUTH_IN_PROGRESS -> {
                    mApiClient.confirmAuthCode(AUTH_CODE_CONFIRM_REQUEST, userEmail, editAuthCode.text.toString())
                }
                UI_STATE_AUTH_COMPLETE -> {
                    val password = newPassword.text.toString()
                    mApiClient.newPassword(NEW_PASSWORD_SEND_REQUEST, userEmail, password, confirmCode)
                }
            }
        }

        // text watchers ---------------------------------------------------
        email.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                emailError.apply {
                    when {
                        s?.isBlank() == true -> {
                            emailAvailable = false
                        }
                        android.util.Patterns.EMAIL_ADDRESS.matcher(s ?: "").matches() -> {
                            visibility = View.GONE
                            emailAvailable = true
                        }
                        else -> {
                            visibility = View.VISIBLE
                            emailAvailable = false
                        }
                    }
                    checkConfirmState()
                }
            }
        })

        editAuthCode.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! == 6) {
                    authCodeFlag = true
                } else {
                    authCodeFlag = false
                }

                checkAuthCode()
            }
        })

        newPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (s?.length!! == 0) {
                    passwordChangable[0] = false
                } else {
                    passwordChangable[0] = passwordPattern.matcher(s ?: "").matches()
                    checkPassword()
                }
            }
        })

        newPasswordConfirm.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if(s?.length!! == 0) {
                    passwordChangable[1] = false
                } else {
                    passwordChangable[1] = s.toString() == newPassword.text.toString()
                    checkPassword()
                }
            }
        })

    }

    private fun updateUI() {
        when (step) {
            UI_STATE_DEFAULT -> {
                layoutInputAccount.visibility = View.VISIBLE
                layoutAuthCode.visibility = View.GONE
                layoutInputPassword.visibility = View.GONE
            }

            UI_STATE_AUTH_IN_PROGRESS -> {
                layoutInputAccount.visibility = View.GONE
                layoutAuthCode.visibility = View.VISIBLE
                layoutInputPassword.visibility = View.GONE

                btnFind.apply {
                    text = "확인"
                    isEnabled = false
                    setTextColor(Helper.readColorRes(R.color.light_grey3))
                    setBackgroundResource(R.drawable.palegrey_round_rect)
                }
            }

            UI_STATE_AUTH_COMPLETE -> {
                layoutInputAccount.visibility = View.GONE
                layoutAuthCode.visibility = View.GONE
                layoutInputPassword.visibility = View.VISIBLE

                btnFind.apply {
                    text = "확인"
                    isEnabled = false
                    setTextColor(Helper.readColorRes(R.color.light_grey3))
                    setBackgroundResource(R.drawable.palegrey_round_rect)
                }
            }
        }
    }

    private fun checkConfirmState() {

        btnFind.apply {
            if (emailAvailable) {
                btnFind.isEnabled = true
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.orange_round_rect_25)
            } else {
                btnFind.isEnabled = false
                setTextColor(Helper.readColorRes(R.color.light_grey3))
                setBackgroundResource(R.drawable.palegrey_round_rect)
            }

        }
    }

    private fun checkAuthCode() {
        btnFind.apply {
            if (authCodeFlag) {
                btnFind.isEnabled = true
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.orange_round_rect_25)
            } else {
                btnFind.isEnabled = false
                setTextColor(Helper.readColorRes(R.color.light_grey3))
                setBackgroundResource(R.drawable.palegrey_round_rect)
            }

        }
    }

    private fun checkPassword() {
        btnFind.apply {
            if (passwordChangable[0] && passwordChangable[1]) {
                btnFind.isEnabled = true
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.orange_round_rect_25)
            } else {
                btnFind.isEnabled = false
                setTextColor(Helper.readColorRes(R.color.light_grey3))
                setBackgroundResource(R.drawable.palegrey_round_rect)
            }
        }
    }
}