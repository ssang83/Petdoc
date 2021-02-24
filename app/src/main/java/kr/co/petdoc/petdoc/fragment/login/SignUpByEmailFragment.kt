package kr.co.petdoc.petdoc.fragment.login

import android.graphics.Rect
import android.os.Bundle
import android.os.Handler
import android.text.Editable
import android.text.TextWatcher
import android.view.*
import androidx.lifecycle.ViewModelProvider
import co.ab180.airbridge.Airbridge
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.fragment_signup_by_email.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.FirebaseAPI
import kr.co.petdoc.petdoc.event.SoftKeyboardBus
import kr.co.petdoc.petdoc.fragment.BaseFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.UserInfo
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.KeyboardHeightProvider
import kr.co.petdoc.petdoc.utils.KeyboardVisibilityUtils
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.AuthorizationDataModel
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.regex.Pattern

/**
 * petdoc-android
 * Class: SignUpByEmailFragment
 * Created by sungminkim on 2020/04/06.
 *
 * Description : 로그인 액티비티 -> 이메일 회원가입 입력 프래그먼트,  현재는 네비게이터에 등록되어 있음
 */
class SignUpByEmailFragment : BaseFragment() {

    private val TAG = "SignUpByEmailFragment"
    private val CREATE_EMAIL_ACCOUNT = "$TAG.createEmailAccount"
    private val EMAIL_CONFIRM = "$TAG.emailConfirm"
    private val EMAIL_CONFIRM_TO_SERVER = "$TAG.emailConfirmToServer"
    private val EMAIL_CHECK = "$TAG.emailCheck"

    private val passwordPattern = Pattern.compile("^(?=.*[A-Za-z])(?=.*[0-9])(?=.*[\$@\$!%*#?&.,]).{8,16}$") // 영문, 숫자, 특수문자 조합
    private val flagBox = booleanArrayOf(false, false, false, false, false)

    private lateinit var screenSizeListener: ViewTreeObserver.OnGlobalLayoutListener
    private val screenChangeRect = Rect()
    private var availableHeight = 0
    private var emailKey = ""

    lateinit var authorizationDataModel: AuthorizationDataModel

    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null
    private lateinit var keyboardVisible: KeyboardVisibilityUtils

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)

        authorizationDataModel = ViewModelProvider(requireActivity()).get(AuthorizationDataModel::class.java)

        return inflater.inflate(R.layout.fragment_signup_by_email, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        view.setPadding(0, Helper.getStatusBarHeight(requireActivity()), 0, 0)

        availableHeight = Helper.screenSize(requireActivity())[1] / 100 * 85
        screenSizeListener = ViewTreeObserver.OnGlobalLayoutListener {
            sign_up_by_email_canvas.getWindowVisibleDisplayFrame(screenChangeRect)
            permssion_guide_confirm_button_area.apply {
                layoutParams = layoutParams.apply {
                    height = if (screenChangeRect.bottom - screenChangeRect.top < availableHeight) 0 else Helper.convertDPResourceToPx(requireContext(), R.dimen.button_height_default)
                }
            }
        }

        emptyTouch.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    if (sign_up_by_email_input_email_text.isFocused) {
                        hideKeyboard(sign_up_by_email_input_email_text)
                    }

                    if (sign_up_by_email_input_nick_name_text.isFocused) {
                        hideKeyboard(sign_up_by_email_input_nick_name_text)
                    }

                    if (sign_up_by_email_password_text.isFocused) {
                        hideKeyboard(sign_up_by_email_password_text)
                    }

                    if (sign_up_by_email_password_confirm_text.isFocused) {
                        hideKeyboard(sign_up_by_email_password_confirm_text)
                    }
                }

                return false
            }
        })

        //=========================================================================================
        keyboardVisible = KeyboardVisibilityUtils(requireActivity().window,
                onShowKeyboard = { keyboardHeight ->
                    sign_up_by_email_scroll_area.run {
                        smoothScrollTo(scrollX, scrollY + keyboardHeight)
                    }
                })

        readyUIandEvent()

        permssion_guide_confirm_button.isEnabled = false
        btnEmailAuth.isEnabled = false
    }

    override fun onResume() {
        super.onResume()
        // 소프트 키보드가 변경 될 경우 Bus 로 data 가 온다.
        mKeyboardHeightProvider = KeyboardHeightProvider(requireActivity())
        Handler().postDelayed({ mKeyboardHeightProvider?.start() }, 1000)

        sign_up_by_email_canvas.viewTreeObserver.addOnGlobalLayoutListener(screenSizeListener)
    }

    override fun onPause() {
        super.onPause()
        sign_up_by_email_canvas.viewTreeObserver.removeOnGlobalLayoutListener(screenSizeListener)
    }

    override fun onDestroyView() {
        if (mApiClient.isRequestRunning(CREATE_EMAIL_ACCOUNT)) {
            mApiClient.cancelRequest(CREATE_EMAIL_ACCOUNT)
        }

        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider!!.close()
        }

        keyboardVisible.detachKeyboardListeners()

        super.onDestroyView()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            CREATE_EMAIL_ACCOUNT -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val mesageKey = JSONObject(event.response)["messageKey"].toString()
                    if ("SUCCESS" == code) {
                        val userInformation: UserInfo = Gson().fromJson(event.response, object : TypeToken<UserInfo>() {}.type)
                        // 로그인 유저 정보가 UserInfo에 객체로 담김
                        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_ACCESS_TOKEN, userInformation.resultData.accessToken)
                        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_REFRESH_TOKEN, userInformation.resultData.refreshToken)
                        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_OLD_TOKEN, userInformation.resultData.oldToken)
                        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_OLD_USER_ID, userInformation.resultData.userInfo.old_userid.toString())
                        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_ID, userInformation.resultData.userInfo.user_id.toString())
                        StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_MOBILE_CONFIRM, userInformation.resultData.userInfo.mobile_confirm)
                        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_EMAIL, userInformation.resultData.userInfo.email)

                        if (userInformation.resultData.userInfo.nickname != null) {
                            StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_NICK_NAME, userInformation.resultData.userInfo.nickname)
                        }

                        if (userInformation.resultData.userInfo.name != null) {
                            StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_NAME, userInformation.resultData.userInfo.name)
                        }

                        if(userInformation.resultData.userInfo.auth_id != null) {
                            StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_AUTH_ID, userInformation.resultData.userInfo.auth_id)
                        }

                        StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_ID_GODO, userInformation.resultData.userInfo.id_godo)
                        StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_LOGIN_TYPE, userInformation.resultData.userInfo.social_type.toInt())
                        StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS, true)

                        PetdocApplication.mUserInfo = userInformation.resultData.userInfo

                        Airbridge.trackEvent("signup", "click", "이메일", null, null, null)
                        Airbridge.trackEvent("signup", "click", "signup_done", null, null, null)
                        FirebaseAPI(requireActivity()).logEventFirebase("가입_완료", "Click Event", "가입 완료 버튼 클릭")

                        requireActivity().finish()
                    } else {
                        AppToast(requireContext()).showToastMessage(
                                loginErrorType(mesageKey),
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM)
                    }
                }
            }

            EMAIL_CONFIRM -> {
                if (event.status == "ok") {
                    try {
                        val code = JSONObject(event.response)["code"]
                        val mesageKey = JSONObject(event.response)["messageKey"].toString()
                        val resultData = JSONObject(event.response)["resultData"] as JSONObject
                        if ("SUCCESS" == code) {
                            flagBox[4] = true
                            emailKey = resultData["emailKey"].toString()
                            btnEmailAuth.text = "재발송"

                            OneBtnDialog(requireContext()).apply {
                                setTitle("")
                                setMessage("인증 메일이 발송되었습니다.\n5분이내 이메일의 인증버튼을\n선택하시면 인증이 완료됩니다.")
                                setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                                    dismiss()
                                })
                                show()
                            }

                            checkConfirmState()
                        } else {
                            Logger.d("error : $mesageKey")
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
            }

            EMAIL_CONFIRM_TO_SERVER -> {
                if (event.status == "ok") {
                    try {
                        val code = JSONObject(event.response)["code"]
                        val mesageKey = JSONObject(event.response)["messageKey"].toString()
                        if ("SUCCESS" == code) {
                            mApiClient.emailSignUp(
                                    CREATE_EMAIL_ACCOUNT,
                                    sign_up_by_email_input_email_text.text.toString(),
                                    sign_up_by_email_password_text.text.toString(),
                                    sign_up_by_email_input_nick_name_text.text.toString()
                            )
                        } else {
                            Logger.d("error : $mesageKey")
                            OneBtnDialog(requireContext()).apply {
                                setTitle("")
                                setMessage("인증이 완료되지 않았습니다.\n이메일 인증을 진행해주세요.")
                                setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                                    dismiss()
                                })
                                show()
                            }
                        }
                    } catch (e: Exception) {
                        Logger.p(e)
                    }
                }
            }

            EMAIL_CHECK -> {
                if (event.status == "ok") {
                    if (event.response.toInt() == 0) {
                        mApiClient.postEmailConfirm(EMAIL_CONFIRM, sign_up_by_email_input_email_text.text.toString())
                    } else {
                        AppToast(requireContext()).showToastMessage(
                            "중복된 이메일입니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )

                        flagBox[4] = false
                        checkConfirmState()
                    }
                }
            }
        }
    }

    /**
     * 소프트 키보드가 올라오면 이벤트가 넘어온다.
     *
     * @param event
     */
    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onMessageEvent(bus: SoftKeyboardBus) {
        Logger.d("height:" + bus.height)

        // 키보드가 올라가면 리스트 제일 끝으로 스크롤
        if (bus.height > 100) {
            emptyTouch.visibility = View.VISIBLE
        } else {
            emptyTouch.visibility = View.GONE
        }
    }

    // ---------------------------------------------------------------------------------------------
    private fun readyUIandEvent() {

        // back button -----------------------------------------------------
        register_by_email_back_button.setOnClickListener { requireActivity().onBackPressed() }

        permssion_guide_confirm_button.setOnClickListener {
            mApiClient.postEmailConfirmToServer(EMAIL_CONFIRM_TO_SERVER, emailKey, sign_up_by_email_input_email_text.text.toString())
        }

        btnEmailAuth.setOnClickListener {
            mApiClient.getUserCountByEmail(EMAIL_CHECK, sign_up_by_email_input_email_text.text.toString())
        }

        // text watchers ---------------------------------------------------
        sign_up_by_email_input_email_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sign_up_by_email_title.apply {
                    when {
                        s?.isBlank() == true -> {
                            setTextColor(Helper.readColorRes(R.color.light_grey))
                            text = Helper.readStringRes(R.string.email)
                            flagBox[0] = false

                            btnEmailAuth.apply {
                                isEnabled = false
                                setTextColor(Helper.readColorRes(R.color.light_grey3))
                            }
                        }
                        android.util.Patterns.EMAIL_ADDRESS.matcher(s ?: "").matches() -> {
                            setTextColor(Helper.readColorRes(R.color.light_grey))
                            text = Helper.readStringRes(R.string.email)
                            flagBox[0] = true
                            btnEmailAuth.apply {
                                isEnabled = true
                                setTextColor(Helper.readColorRes(R.color.slateGrey))
                            }
                        }
                        else -> {
                            setTextColor(Helper.readColorRes(R.color.orange))
                            text = Helper.readStringRes(R.string.sign_up_by_email_wrong_pattern1)
                            flagBox[0] = false
                        }
                    }
                    checkConfirmState()
                }
            }
        })

        sign_up_by_email_password_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sign_up_by_email_password_title.apply {
                    when {
                        s?.isBlank() == true -> {
                            setTextColor(Helper.readColorRes(R.color.light_grey))
                            text = Helper.readStringRes(R.string.password)
                            flagBox[1] = false
                        }
                        passwordPattern.matcher(s ?: "").matches() -> {
                            setTextColor(Helper.readColorRes(R.color.light_grey))
                            text = Helper.readStringRes(R.string.password)
                            flagBox[1] = true
                        }
                        else -> {
                            setTextColor(Helper.readColorRes(R.color.orange))
                            text = Helper.readStringRes(R.string.sign_up_by_email_wrong_pattern2)
                            flagBox[1] = false
                        }
                    }
                    checkConfirmState()
                }
            }
        })

        sign_up_by_email_password_confirm_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sign_up_by_email_password_confirm_title.apply {

                    when {
                        s?.isBlank() == true -> {
                            setTextColor(Helper.readColorRes(R.color.light_grey))
                            text = Helper.readStringRes(R.string.password)
                            flagBox[2] = false
                        }
                        sign_up_by_email_password_text.text.toString() == s.toString() -> {
                            setTextColor(Helper.readColorRes(R.color.light_grey))
                            text = Helper.readStringRes(R.string.password)
                            flagBox[2] = true
                        }
                        else -> {
                            setTextColor(Helper.readColorRes(R.color.orange))
                            text = Helper.readStringRes(R.string.sign_up_by_email_wrong_pattern3)
                            flagBox[2] = false
                        }
                    }

                    checkConfirmState()
                }
            }
        })

        sign_up_by_email_input_nick_name_text.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                sign_up_by_nick_name_title.apply {
                    when {
                        s?.isBlank() == true -> {
                            setTextColor(Helper.readColorRes(R.color.light_grey))
                            text = Helper.readStringRes(R.string.nickName)
                            flagBox[3] = false
                        }
                        s?.length!! > 0 -> {
                            setTextColor(Helper.readColorRes(R.color.light_grey))
                            text = Helper.readStringRes(R.string.nickName)
                            flagBox[3] = true
                        }
                        else -> {
                            flagBox[3] = false
                        }
                    }

                    checkConfirmState()
                }
            }
        })
    }

    private fun checkConfirmState() {
        permssion_guide_confirm_button.apply {
            if (flagBox[0] && flagBox[1] && flagBox[2] && flagBox[3] && flagBox[4]) {
                permssion_guide_confirm_button.isEnabled = true
                setTextColor(Helper.readColorRes(R.color.orange))
                setBackgroundResource(R.drawable.orange_round_rect_25)
            } else {
                permssion_guide_confirm_button.isEnabled = false
                setTextColor(Helper.readColorRes(R.color.light_grey3))
                setBackgroundResource(R.drawable.palegrey_round_rect)
            }

        }
    }

    private fun saveSnsLoginType(type: String) {
        when (type) {
            "2" -> StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE, "kakao")
            "3" -> StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE, "facebook")
            "4" -> StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE, "naver")
            "5" -> StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE, "google")
            "6" -> StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE, "apple")
            else -> StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE, "email")
        }
    }

    private fun loginErrorType(code: String) =
            when (code) {
                "101" -> "입력하신 아이디가 존재하지 않습니다."
                "102" -> "입력하신 아이디가 존재하지 않습니다."
                "103" -> "입력하신 비밀번호가 맞지 않습니다."
                "104" -> "user ID 에 대한 토큰이 존재하지 않습니다."
                "105" -> "user ID 에 대한 토큰이 만료되었습니다."
                "106" -> "user ID 에 대한 토큰이 만료되었습니다."
                "107" -> "입력하신 비밀번호 변경 코드가 맞지 않습니다."
                "108" -> "소셜 계정으로 가입하신 경우, 비밀번호 찾기를 이용할 수 없습니다."
                "109" -> "브이케어 유료회원의 탈퇴는 브이케어 매장을 통해 가능합니다."
                "110" -> "이미 확인이 완료된 email 정보입니다."
                "201" -> "이미 동일한 Email이 존재합니다."
                "202" -> "이미 동일한 휴대폰 번호가 존재합니다."
                "203" -> "Social 로그인이 실패하였습니다. 다시 시도해주세요."
                "204" -> "로그인을 지원하지 않는 Social 서비스입니다."
                "205" -> "입력하신 정보가 포함된 복수의 계정이 존재합니다. 계정 통합 절차를 진행해주세요."
                "206" -> "통합회원으로 이전하여야 합니다."
                "207" -> "이미 동일한 닉네임이 존재합니다."
                "501" -> "조회 조건이 존재하지 않습니다."
                "999" -> "기타 에러"
                "err" -> "알수 없는 에러가 발생했습니다. 잠시 후 다시 시도해주세요."
                "cancel" -> "로그인이 취소되었습니다."
                "err-인증 실패" -> "간편 로그인 인증에 실패했습니다."
                else -> ""
            }
}