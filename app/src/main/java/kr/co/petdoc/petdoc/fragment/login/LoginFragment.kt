package kr.co.petdoc.petdoc.fragment.login

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.text.InputFilter
import android.text.method.PasswordTransformationMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import co.ab180.airbridge.Airbridge
import com.facebook.*
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.kakao.auth.AuthType
import com.kakao.auth.ISessionCallback
import com.kakao.auth.Session
import com.kakao.network.ErrorResult
import com.kakao.sdk.auth.LoginClient
import com.kakao.sdk.auth.model.OAuthToken
import com.kakao.usermgmt.UserManagement
import com.kakao.usermgmt.callback.LogoutResponseCallback
import com.kakao.usermgmt.callback.MeV2ResponseCallback
import com.kakao.usermgmt.response.MeV2Response
import com.kakao.util.OptionalBoolean
import com.kakao.util.exception.KakaoException
import com.nhn.android.naverlogin.OAuthLogin
import com.nhn.android.naverlogin.OAuthLoginHandler
import kotlinx.android.synthetic.main.fragment_login.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.event.LoggedInEvent
import kr.co.petdoc.petdoc.event.SoftKeyboardBus
import kr.co.petdoc.petdoc.extensions.setOnSingleClickListener
import kr.co.petdoc.petdoc.fragment.login.apple.AppleSignInDialog
import kr.co.petdoc.petdoc.fragment.login.apple.Interaction
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.ApiClient
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.AccountData
import kr.co.petdoc.petdoc.network.response.submodel.UserInfo
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.KeyboardHeightProvider
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.AuthorizationDataModel
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import java.util.*
import java.util.regex.Pattern

/**
 * petdoc-android
 * Class: LoginFragment
 * Created by sungminkim on 2020/04/06.
 *
 * 소셜 로그인 타입
 * 카카오톡 : 2
 * 페이스북 :3
 * 네이버 : 4
 * 구글 : 5
 * 애플 : 6
 *
 * Description : 로그인 액티비티 상의 메인 홈 프래그먼트,  현재는 네비게이터에 등록되어 있음
 */
class LoginFragment : Fragment(), Interaction {

    private val TAG = "LoginFragment"
    private val LOGIN_EMAIL_REQUEST_TAG = "login.requestTag.email"
    private val LOGIN_SNS_REQUEST = "$TAG.snsLoginRequest"

    private val GOOGLE_SIGN_IN = 0x101

    private val passwordPattern = Pattern.compile("[a-zA-Z0-9]{8,16}")         // 비밀번호 최소,최대 길이값 설정 + 패턴 설정
//    private val passwordPattern = Pattern.compile("([a-zA-Z0-9].*[!,@,#,\$,%,^,&,*,?,_,~])|([!,@,#,\$,%,^,&,*,?,_,~].*[a-zA-Z0-9]){8,16}") // 영문, 숫자, 특수문자 조합


    enum class LoginPattern{
        EMAIL_FORMAT_ERROR, PASSWORD_FORMAT_ERROR, SUCCESS
    }

    private lateinit var mEventBus: EventBus
    protected lateinit var mApiClient: ApiClient

    lateinit var authorization : AuthorizationDataModel

    // kakao login session
    private var sessionCallback:SessionCallback? = null
    private var session: Session? = null

    // facebook login
    private var loginCallback:LoginCallback? = null
    private var callbackManager:CallbackManager? = null

    // naver login
    private var authLoginModule:OAuthLogin? =null

    // google login
    private var googleSignInClient:GoogleSignInClient? = null
    private var auth:FirebaseAuth? = null

    private var mKeyboardHeightProvider: KeyboardHeightProvider? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle? ): View? {
        Helper.statusBarColorChange(requireActivity(), true, alpha = 0, fullscreen = true)
        authorization = ViewModelProvider(requireActivity()).get(AuthorizationDataModel::class.java)
        return inflater.inflate(R.layout.fragment_login, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mEventBus = EventBus.getDefault()
        mApiClient = PetdocApplication.application.apiClient

        if(!mEventBus.isRegistered(this)) {
            mEventBus.register(this)
        }

        login_close_button.setOnClickListener { requireActivity().onBackPressed() }

        emptyTouch.setOnTouchListener(object : View.OnTouchListener {
            override fun onTouch(v: View?, event: MotionEvent?): Boolean {
                Logger.d("event action : ${event?.action}")
                if (event?.action == MotionEvent.ACTION_DOWN) {
                    if (login_input_email_text.isFocused) {
                        hideKeyboard(login_input_email_text)
                    }

                    if (login_input_password_text.isFocused) {
                        hideKeyboard(login_input_password_text)
                    }
                }

                return false
            }
        })

        login_fragment_root.setPadding(0, Helper.getStatusBarHeight(requireActivity()) , 0, 0)

        readyUIandEvents()
        readySocial()

        checkLastSocialLogin()
    }

    override fun onResume() {
        super.onResume()
        if (authorization.combineFlag.value!!) {
            OneBtnDialog(requireContext()).apply {
                setTitle("계정 통합 완료")
                setMessage("${authorization.combineUserEmail.value.toString()}으로 계정 통합이 완료 되었습니다. 다시 로그인해주세요.")
                setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                    dismiss()
                })
                show()
            }
        }

        // 소프트 키보드가 변경 될 경우 Bus 로 data 가 온다.
        mKeyboardHeightProvider = KeyboardHeightProvider(requireActivity())
        Handler().postDelayed( { mKeyboardHeightProvider?.start() }, 1000)
    }

    override fun onDestroyView() {
        if(mApiClient.isRequestRunning(LOGIN_EMAIL_REQUEST_TAG)){
            mApiClient.cancelRequest(LOGIN_EMAIL_REQUEST_TAG)
        }

        if (mApiClient.isRequestRunning(LOGIN_SNS_REQUEST)) {
            mApiClient.cancelRequest(LOGIN_SNS_REQUEST)
        }

        if (sessionCallback != null) {
            Session.getCurrentSession().removeCallback(sessionCallback)
            sessionCallback = null
        }

        if (mKeyboardHeightProvider != null) {
            mKeyboardHeightProvider!!.close()
        }

        if (mEventBus.isRegistered(this)) {
            mEventBus.unregister(this)
        }

        super.onDestroyView()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if(requestCode == GOOGLE_SIGN_IN) {
            val task = GoogleSignIn.getSignedInAccountFromIntent(data!!)
            handleSignInResult(task)
        } else {
            if(Session.getCurrentSession().handleActivityResult(requestCode, resultCode, data)) {
                Logger.d("callback called kakao")
                return
            } else {
                Logger.d("callback called facebook")
                callbackManager?.onActivityResult(requestCode, resultCode, data)
            }

            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onAppleLoginSuccess(sub: String) {
        Logger.d("token : $sub")

        authorization.type.value = "apple"
        authorization.socialKey.value = sub
        authorization.socialType.value = 6

        if (StorageUtils.loadBooleanValueFromPreference(
                requireContext(),
                AppConstants.PREF_KEY_LOGIN_STATUS
            ).not()
        ) {
            mApiClient.snsLogin(LOGIN_SNS_REQUEST, sub, 6)
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(data: NetworkBusResponse) {
        when(data.tag) {
            LOGIN_EMAIL_REQUEST_TAG -> {
                if (data.status == "ok") {
                    val code = JSONObject(data.response)["code"]
                    val mesageKey = JSONObject(data.response)["messageKey"].toString()
                    if (code == "SUCCESS") {
                        try {
                            if (mesageKey == "205") {
                                val json = JSONObject(data.response)["resultData"]
                                val items:MutableList<AccountData> = Gson().fromJson(json.toString(), object : TypeToken<MutableList<AccountData>>() {}.type)
                                authorization.userInfoData.value = items
                                findNavController().navigate(R.id.action_loginFragment_to_checkAccountFragment)
                            } else {
                                val userInformation:UserInfo = Gson().fromJson(data.response, object : TypeToken<UserInfo>() {}.type)
                                // 로그인 유저 정보가 UserInfo에 객체로 담김
                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_ACCESS_TOKEN, userInformation.resultData.accessToken)
                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_REFRESH_TOKEN, userInformation.resultData.refreshToken)
                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_OLD_TOKEN, userInformation.resultData.oldToken)
                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_OLD_USER_ID, userInformation.resultData.userInfo.old_userid.toString())
                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_ID, userInformation.resultData.userInfo.user_id.toString())
                                StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_MOBILE_CONFIRM, userInformation.resultData.userInfo.mobile_confirm)
                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_EMAIL, userInformation.resultData.userInfo.email)

                                if(userInformation.resultData.userInfo.auth_id != null) {
                                    StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_AUTH_ID, userInformation.resultData.userInfo.auth_id)
                                }

                                if (userInformation.resultData.userInfo.nickname != null) {
                                    StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_NICK_NAME, userInformation.resultData.userInfo.nickname)
                                }

                                if (userInformation.resultData.userInfo.name != null) {
                                    StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_NAME, userInformation.resultData.userInfo.name)
                                }

                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_ID_GODO, userInformation.resultData.userInfo.id_godo)
                                StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_LOGIN_TYPE, userInformation.resultData.userInfo.social_type.toInt())
                                StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS, true)

                                PetdocApplication.mUserInfo = userInformation.resultData.userInfo

                                saveSnsLoginType(userInformation.resultData.userInfo.social_type)

                                EventBus.getDefault().post(LoggedInEvent())
                                requireActivity().finish()
                            }
                        } catch (e: Exception) {
                            Logger.p(e)
                        }
                    } else {
                        AppToast(requireContext()).showToastMessage(
                            loginErrorType(mesageKey),
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM)

                        Airbridge.trackEvent("signin", "click", "login_fail", null, null, null)
                    }
                }
            }

            LOGIN_SNS_REQUEST -> {
                if (data.status == "ok") {
                    val code = JSONObject(data.response)["code"]
                    val mesageKey = JSONObject(data.response)["messageKey"].toString()
                    if (code == "SUCCESS") {
                        try {
                            if (mesageKey == "205") {
                                val json = JSONObject(data.response)["resultData"]
                                val items:MutableList<AccountData> = Gson().fromJson(json.toString(), object : TypeToken<MutableList<AccountData>>() {}.type)
                                authorization.userInfoData.value = items
                                findNavController().navigate(R.id.action_loginFragment_to_checkAccountFragment)
                            } else {
                                val userInformation:UserInfo = Gson().fromJson(data.response, object : TypeToken<UserInfo>() {}.type)
                                // 로그인 유저 정보가 UserInfo에 객체로 담김
                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_ACCESS_TOKEN, userInformation.resultData.accessToken)
                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_REFRESH_TOKEN, userInformation.resultData.refreshToken)
                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_OLD_TOKEN, userInformation.resultData.oldToken)
                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_OLD_USER_ID, userInformation.resultData.userInfo.old_userid.toString())
                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_ID, userInformation.resultData.userInfo.user_id.toString())
                                StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_MOBILE_CONFIRM, userInformation.resultData.userInfo.mobile_confirm)
                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_EMAIL, userInformation.resultData.userInfo.email)

                                if(userInformation.resultData.userInfo.auth_id != null) {
                                    StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_AUTH_ID, userInformation.resultData.userInfo.auth_id)
                                }

                                if (userInformation.resultData.userInfo.nickname != null) {
                                    StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_NICK_NAME, userInformation.resultData.userInfo.nickname)
                                }

                                if (userInformation.resultData.userInfo.name != null) {
                                    StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_NAME, userInformation.resultData.userInfo.name)
                                }

                                StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_ID_GODO, userInformation.resultData.userInfo.id_godo)
                                StorageUtils.writeIntValueInPreference(requireContext(), AppConstants.PREF_KEY_USER_LOGIN_TYPE, userInformation.resultData.userInfo.social_type.toInt())
                                StorageUtils.writeBooleanValueInPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS, true)

                                PetdocApplication.mUserInfo = userInformation.resultData.userInfo

                                saveSnsLoginType(userInformation.resultData.userInfo.social_type)

                                EventBus.getDefault().post(LoggedInEvent())
                                requireActivity().finish()
                            }
                        } catch (e: Exception) {
                            Logger.p(e)
                        }
                    } else {
                        if (mesageKey == "102" || mesageKey == "101") {
                            findNavController().navigate(R.id.action_loginFragment_to_registerPolicyFragment)
                        } else {
                            AppToast(requireContext()).showToastMessage(
                                loginErrorType(mesageKey),
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM
                            )

                            Airbridge.trackEvent("signin", "click", "login_fail", null, null, null)
                        }
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
    private fun readyUIandEvents(){

        // 로그인 버튼 클릭 이벤트
        login_button.setOnClickListener {
            mApiClient.emailLogin(
                LOGIN_EMAIL_REQUEST_TAG,
                login_input_email_text.text.toString().trim(),
                login_input_password_text.text.toString().trim(),
                StorageUtils.loadValueFromPreference(
                    requireContext(),
                    AppConstants.PREF_KEY_APP_SETUP_UUID_KEY,
                    ""
                )
            )
/*            when(checkLoginInputPattern()){
                LoginPattern.EMAIL_FORMAT_ERROR ->{
                    // 이메일 형식 에러
                    AppToast(requireContext()).showToastMessage(
                        "EMAIL PATTERN ERROR",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                }
                LoginPattern.PASSWORD_FORMAT_ERROR -> {
                    // 비밀번호 형식 에러
                    AppToast(requireContext()).showToastMessage(
                        "PASSWORD PATTERN ERROR",
                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                        AppToast.GRAVITY_BOTTOM
                    )
                }
                LoginPattern.SUCCESS ->{
                    // 로그인 네트워크 API 태우기



                    //StorageUtils.writeValueInPreference(requireContext(), lastSocialLoginTag, "email")
                    //checkLastSocialLogin()
                }
            }*/
        }

        // 비밀번호 찾기 클릭
        login_find_password.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_findAccountFragment)
        }

        // 이메일로 회원 가입 클릭
        login_sign_up_email.setOnClickListener {
            authorization.type.value = "email"
            findNavController().navigate(R.id.action_loginFragment_to_registerPolicyFragment)
        }


        // 닫기 버튼
        login_close_button.setOnClickListener {
            requireActivity().finish()
        }


        // 입력 위젯 셋팅 -----------------------------------------------------------------------------
        login_input_email_text.apply{

        }

        login_input_password_text.apply{
            filters = arrayOf(InputFilter.LengthFilter(20))             // 비밀번호 길이 제한
            transformationMethod = PasswordTransformationMethod()            // 비밀번호 입력 형태 변환
        }
        // -----------------------------------------------------------------------------------------


    }


    // ---------------------------------------------------------------------------------------------
    private fun checkLoginInputPattern():LoginPattern{

        login_input_email_text.apply{
            val email = this.text.trim()
            if(email.isBlank() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) return LoginPattern.EMAIL_FORMAT_ERROR
        }

        login_input_password_text.apply{
            val password = this.text.trim()
            if(  password.length < 6 || !passwordPattern.matcher(password).matches())  return LoginPattern.PASSWORD_FORMAT_ERROR
        }

        return LoginPattern.SUCCESS
    }

    // ---------------------------------------------------------------------------------------------
    private fun readySocial(){

        login_social_kakao.setOnSingleClickListener {
            kakaoLogin()
        }
        login_social_facebook.setOnSingleClickListener {
            faceBookLogin()
        }
        login_social_naver.setOnSingleClickListener {
            naverLogin()
        }
        login_social_google.setOnSingleClickListener {
            googleLogin()
        }
        login_social_apple.setOnSingleClickListener {
            appleLogin()
        }
    }


    // ---------------------------------------------------------------------------------------------
    private fun checkLastSocialLogin(){

        login_social_kakao_check.visibility = View.INVISIBLE
        login_social_facebook_check.visibility = View.INVISIBLE
        login_social_naver_check.visibility = View.INVISIBLE
        login_social_google_check.visibility = View.INVISIBLE

        login_last_social_area.visibility = View.INVISIBLE

        when(StorageUtils.loadValueFromPreference(requireContext(), AppConstants.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE, "")){
            "kakao" -> {
                login_social_kakao_check.visibility = View.VISIBLE
                login_last_social_area.visibility = View.VISIBLE
                login_last_social_history_type.text = requireContext().getString(R.string.kakao_account)
            }
            "google" -> {
                login_social_google_check.visibility = View.VISIBLE
                login_last_social_area.visibility = View.VISIBLE
                login_last_social_history_type.text = requireContext().getString(R.string.google_account)
            }
            "facebook" -> {
                login_social_facebook_check.visibility = View.VISIBLE
                login_last_social_area.visibility = View.VISIBLE
                login_last_social_history_type.text = requireContext().getString(R.string.facebook_account)
            }
            "naver" -> {
                login_social_naver_check.visibility = View.VISIBLE
                login_last_social_area.visibility = View.VISIBLE
                login_last_social_history_type.text = requireContext().getString(R.string.naver_account)
            }

            "apple" -> {
                login_social_apple_check.visibility = View.VISIBLE
                login_last_social_area.visibility = View.VISIBLE
                login_last_social_history_type.text = requireContext().getString(R.string.apple_account)
            }
            "email" -> {
                login_last_social_area.visibility = View.VISIBLE
                login_last_social_history_type.text = requireContext().getString(R.string.email_account)
            }
        }
    }

    private fun saveSnsLoginType(type:String) {
        when (type) {
            "2" -> StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE, "kakao")
            "3" -> StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE, "facebook")
            "4" -> StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE, "naver")
            "5" -> StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE, "google")
            "6" -> StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE, "apple")
            else -> StorageUtils.writeValueInPreference(requireContext(), AppConstants.PREF_KEY_LAST_SOCIAL_LOGIN_SERVICE, "email")
        }

        Airbridge.trackEvent("signin", "click", "login_success", null, null, null)
    }

    //===============================================================================================
    val callback:(OAuthToken?, Throwable?) -> Unit = { token, error ->
        if (error != null) {
            Logger.d("kakao login faile, $error")
        } else if (token != null) {
            Logger.d("kakao login success, $token")
        }
    }

    private fun kakaoLogin() {
        UserManagement.getInstance()
            .requestLogout(object : LogoutResponseCallback() {
                override fun onCompleteLogout() {
                    // 로그아웃 성공
                    Logger.d("kakao logout success")
                    session = Session.getCurrentSession()

                    if (session != null) {
                        session!!.close()
                        sessionCallback = SessionCallback()
                        session!!.addCallback(sessionCallback)
                        session!!.open(AuthType.KAKAO_LOGIN_ALL, this@LoginFragment)
                    }
                }
            })
    }

    private fun faceBookLogin() {
        val accessToken = AccessToken.getCurrentAccessToken()
        val isLoggedIn = if (accessToken != null && !accessToken.isExpired) {
            true
        } else {
            false
        }

        Logger.d("isLoggedIn : $isLoggedIn")

        callbackManager = CallbackManager.Factory.create()
        loginCallback = LoginCallback()

        val loginManager = LoginManager.getInstance()
        loginManager.registerCallback(callbackManager, loginCallback)
        loginManager.logInWithReadPermissions(
            this@LoginFragment,
            Arrays.asList("public_profile")
        )

    }

    private fun naverLogin() {
        authLoginModule = OAuthLogin.getInstance()
        if (authLoginModule != null) {
            authLoginModule!!.init(
                requireContext(),
                AppConstants.OAUTH_CLIENT_ID,
                AppConstants.OAUTH_CLIENT_SECRET,
                AppConstants.OAUTH_CLIENT_NAME
            )

            authLoginModule!!.startOauthLoginActivity(requireActivity(), authLogingHandler)
        }
    }

    /**
     * 클라이언트 ID
     * 312781285860-jdkqqsvvkig8smma7ii7o138ubdpr1ea.apps.googleusercontent.com     // 웹 어플리케이션
     *
     */
    private fun googleLogin() {
        FirebaseAuth.getInstance().signOut()

        auth = FirebaseAuth.getInstance()

        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(Helper.readStringRes(R.string.default_web_client_id))
            .requestEmail()
            .build()

        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)

        if (googleSignInClient != null) {
            googleSignInClient!!.signInIntent.let {
                startActivityForResult(it, GOOGLE_SIGN_IN)
            }
        }
    }

    private fun handleSignInResult(task: Task<GoogleSignInAccount>?) {
        try {
            val account = task?.getResult(ApiException::class.java)
            if (account != null) {
                val name = account.displayName
                val email = account.email
                val userId = account.id
                val token = account.idToken

                Logger.d("name : $name, email : $email, userId : $userId, token : $token")
                firebaseWithGoogle(token!!, userId!!)
            }
        } catch (e: ApiException) {
            Logger.d("signInResult fail code : ${e.statusCode} ${e.message}")
        }
    }

    private fun firebaseWithGoogle(token: String, userId:String) {
        val credential = GoogleAuthProvider.getCredential(token, null)
        auth?.signInWithCredential(credential)
            ?.addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Logger.d("signInWithCredental Success")
                    val user = auth?.currentUser
                    Logger.d("user : ${user.toString()}")

                    authorization.type.value = "google"
                    authorization.socialKey.value = userId
                    authorization.socialType.value = 9
                    authorization.googleToken.value = token

                    if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS).not()) {
                        mApiClient.snsLogin(LOGIN_SNS_REQUEST, userId, 9, token)
                    }
                } else {
                    Logger.d("signInWithCredental Failure, ${task.exception}")
                }
            }
    }

    private fun appleLogin() {
        AppleSignInDialog(requireContext(), this@LoginFragment).show()
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
            "111" -> "EMail/Password로 가입되지 않은 유저."
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

    /**
     * Hide KeyBoard
     *
     * @param v
     */
    protected fun hideKeyboard(v: View) {
        Helper.hideKeyboard(requireActivity(), v)
        v.clearFocus()
    }

    //==============================================================================================
    inner class SessionCallback : ISessionCallback {

        // 로그인 실패
        override fun onSessionOpenFailed(exception: KakaoException?) {
            Logger.d("SessionCallback : ${exception?.message}")
        }

        // 로그인 성공
        override fun onSessionOpened() {
            val token = Session.getCurrentSession().tokenInfo.accessToken
            Logger.d("token : $token")
            authorization.type.value = "kakao"
            authorization.socialKey.value = token
            authorization.socialType.value = 2
            requestMe()
        }

        // 사용자 정보 요청
        private fun requestMe() {
            UserManagement.getInstance()
                .me(object : MeV2ResponseCallback() {
                    override fun onSuccess(result: MeV2Response) {
                        Logger.d("user id : ${result.id}")

                        val kakaoAccount = result.kakaoAccount
                        if (kakaoAccount != null) {
                            val email = kakaoAccount.email
                            if (email != null) {
                                Logger.d("kakao email : $email")
                                authorization.kakaoEmail.value = email
                            } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                                //TODO : 동의 요청 후 이메일 획득 가능
                                // 단, 선택 동의로 설정되어 있다면 서비스 이용 시나리오 상에서 반드시 필요한 경우에만 요청해야 합니다.
                            } else {
                                // 이메일 획득 불가
                            }
                        }

                        // profile
                        val profile = kakaoAccount.profile
                        if (profile != null) {
                            Logger.d("nickName : ${profile.nickname}")
                            Logger.d("profleImg : ${profile.profileImageUrl}")
                            Logger.d("thumbnailImg : ${profile.thumbnailImageUrl}")
                        } else if (kakaoAccount.emailNeedsAgreement() == OptionalBoolean.TRUE) {
                            //TODO : 동의 요청 후 프로필 획득 가능
                            // 단, 선택 동의로 설정되어 있다면 서비스 이용 시나리오 상에서 반드시 필요한 경우에만 요청해야 합니다.
                        } else {
                            // 프로필 획득 불가
                        }

                        if(isAdded) {
                            if(StorageUtils.loadBooleanValueFromPreference(requireContext(), AppConstants.PREF_KEY_LOGIN_STATUS).not()) {
                                mApiClient.snsLogin(LOGIN_SNS_REQUEST, authorization.socialKey.value.toString(), 2)
                            }
                        }
                    }

                    override fun onFailure(errorResult: ErrorResult?) {
                        Logger.d("onFailure , $errorResult")
                    }

                    override fun onSessionClosed(errorResult: ErrorResult?) {
                        Logger.d("onSessionClosed, $errorResult")
                    }
                })
        }
    }

    //==============================================================================================
    inner class LoginCallback : FacebookCallback<LoginResult> {

        // 로그인 성공 시 호출
        override fun onSuccess(result: LoginResult?) {
            Logger.d("onSuccess")
            requestMe(result?.accessToken!!)
        }

        // 로그인 창을 닫을 경우 호출
        override fun onCancel() {
            Logger.d("onCancel")
        }

        // 로그인 실패 시 호출
        override fun onError(error: FacebookException?) {
            Logger.d("onError, error : ${error?.message}")
        }

        // 사용자 정보 요청
        fun requestMe(token: AccessToken) {
            val graphRequest = GraphRequest.newMeRequest(token,
                object : GraphRequest.GraphJSONObjectCallback {
                    override fun onCompleted(`object`: JSONObject?, response: GraphResponse?) {
                        Logger.d("result : ${`object`.toString()}")
                        authorization.type.value = "facebook"
                        authorization.socialKey.value = token.token
                        authorization.socialType.value = 3

                        if (StorageUtils.loadBooleanValueFromPreference(
                                requireContext(),
                                AppConstants.PREF_KEY_LOGIN_STATUS
                            ).not()
                        ) {
                            mApiClient.snsLogin(LOGIN_SNS_REQUEST, token.token, 3)
                        }
                    }
                })

            Bundle().apply { putString("fields", "id,name,email,gender,birthday") }.let {
                graphRequest.parameters = it
                graphRequest.executeAsync()
            }
        }
    }

    //===============================================================================================
    /**
     * OAuthLoginHandler를 startOAuthLoginActivity() 메서드 호출 시 파라미터로 전달하거나 OAuthLoginButton
    객체에 등록하면 인증이 종료되는 것을 확인할 수 있습니다.
     */
    private val authLogingHandler = object : OAuthLoginHandler() {
        override fun run(success: Boolean) {
            if (isAdded()) {
                if (success) {
                    val accessToken = authLoginModule?.getAccessToken(requireContext())
                    val refreshToken = authLoginModule?.getRefreshToken(requireContext())
                    val expireAt = authLoginModule?.getExpiresAt(requireContext())
                    val tokenType = authLoginModule?.getTokenType(requireContext())

                    Logger.d("accessToken : $accessToken, refreshToken : $refreshToken, expireAt : $expireAt, tokenType : $tokenType")

                    authorization.type.value = "naver"
                    authorization.socialKey.value = accessToken
                    authorization.socialType.value = 4

                    if (StorageUtils.loadBooleanValueFromPreference(
                            requireContext(),
                            AppConstants.PREF_KEY_LOGIN_STATUS
                        ).not()
                    ) {
                        mApiClient.snsLogin(LOGIN_SNS_REQUEST, accessToken.toString(), 4)
                    }
                } else {
                    val errorCode = authLoginModule?.getLastErrorCode(requireContext())
                    val errorDesc = authLoginModule?.getLastErrorDesc(requireContext())
                    Logger.d("errorCode : $errorCode, errorDesc : $errorDesc")
                }
            }
        }
    }
}