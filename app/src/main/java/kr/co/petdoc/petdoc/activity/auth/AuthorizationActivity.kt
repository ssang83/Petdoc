package kr.co.petdoc.petdoc.activity.auth

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import co.ab180.airbridge.Airbridge
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_authorization.*
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.event.LogoutEvent
import kr.co.petdoc.petdoc.fragment.login.AuthorizationFragment
import kr.co.petdoc.petdoc.fragment.login.FindIDResultDialogFragment
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.UserInfoData
import kr.co.petdoc.petdoc.repository.local.cache.AppCache
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.PermissionCallback
import kr.co.petdoc.petdoc.utils.PermissionStatus
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.json.JSONObject
import org.koin.core.context.GlobalContext
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * Petdoc
 * Class: AuthorizationActivity
 * Created by kimjoonsung on 2020/07/29.
 *
 * Description :
 */
class AuthorizationActivity : BaseActivity(), FindIDResultDialogFragment.CallbackListener {

    private val TAG = "AuthorizationActivity"
    private val UPDATE_USER_REQUEST = "$TAG.updateUserRequest"

    private var userName = ""
    private var phone = ""
    private var gender = ""
    private var birthDay = ""
    private var phoneCorp = ""
    private var userId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Helper.statusBarColorChange(this, true, alpha = 0, fullscreen = true)
        setContentView(R.layout.activity_authorization)
        root.setPadding(0, Helper.getStatusBarHeight(this) , 0, 0)

        Helper.permissionCheck(this,
            arrayOf("android.permission.INTERNET","android.permission.ACCESS_NETWORK_STATE"),
            object : PermissionCallback {
                override fun callback(status: PermissionStatus) {
                    when(status){
                        PermissionStatus.ALL_GRANTED -> {}
                        PermissionStatus.DENIED_SOME -> {
                            //finish()
                        }
                    }
                }
            },true )

        val fragmentManager = supportFragmentManager
        val transaction = fragmentManager.beginTransaction().apply {
            replace(R.id.fragment, AuthorizationFragment().apply {
                bundleOf("callback" to "petdoc://app.android.data/auth").let {
                    arguments = it
                }
            })
        }
        
        transaction.commit()

        userId = if(StorageUtils.loadValueFromPreference(this, AppConstants.PREF_KEY_USER_ID, "").isNotEmpty()) {
            StorageUtils.loadValueFromPreference(this, AppConstants.PREF_KEY_USER_ID, "").toInt()
        } else {
            0
        }
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if(Intent.ACTION_VIEW == intent?.action) {
            val appLinkInformation = intent.data

            //petdoc://app.android.data/auth?userName=%EA%B9%80%EC%84%B1%EB%AF%BC&phone=01085394523&phoneCorp=SKT&gender=M&birthday=19851116&nation=0
            if (appLinkInformation?.scheme == "petdoc" && appLinkInformation.host == "app.android.data") {
                when (appLinkInformation.lastPathSegment) {
                    "auth" -> {
                        userName = appLinkInformation.getQueryParameter("userName") ?: ""
                        phone = appLinkInformation.getQueryParameter("phone") ?: ""
                        gender =  appLinkInformation.getQueryParameter("gender") ?: ""
                        birthDay = appLinkInformation.getQueryParameter("birthday") ?: ""
                        phoneCorp = appLinkInformation.getQueryParameter("phoneCorp") ?: ""

                        Logger.d("userName : $userName, phone : $phone, gender : $gender, birthDay : $birthDay, phoneCorp : $phoneCorp")

                        mApiClient.updateUser(
                                UPDATE_USER_REQUEST,
                                userId,
                                userName,
                                changeDateFormat(birthDay),
                                gender,
                                phone
                        )
                    }
                    else -> {
                    }
                }
            }
        }
    }

    override fun onDestroy() {
        if (mApiClient.isRequestRunning(UPDATE_USER_REQUEST)) {
            mApiClient.cancelRequest(UPDATE_USER_REQUEST)
        }

        super.onDestroy()
    }

    override fun onBackPressed() {
        setResult(Activity.RESULT_CANCELED)
        finish()
    }

    override fun onDismiss() {
        setResult(Activity.RESULT_OK)
        finish()
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(event: NetworkBusResponse) {
        when (event.tag) {
            UPDATE_USER_REQUEST -> {
                if ("ok" == event.status) {
                    val code = JSONObject(event.response)["code"]
                    val mesageKey = JSONObject(event.response)["messageKey"].toString()
                    if ("SUCCESS" == code) {
                        try {
                            val obj = JSONObject(event.response)["resultData"]
                            val userInfo: UserInfoData = Gson().fromJson(obj.toString(), object : TypeToken<UserInfoData>() {}.type)

                            val userId = StorageUtils.loadValueFromPreference(this, AppConstants.PREF_KEY_USER_ID, "").toInt()
                            if (userId == userInfo.user_id) {
                                StorageUtils.writeIntValueInPreference(this, AppConstants.PREF_KEY_MOBILE_CONFIRM, 1)

                                AppToast(this).showToastMessage(
                                        "본인인증이 완료 되었습니다.",
                                        AppToast.DURATION_MILLISECONDS_DEFAULT,
                                        AppToast.GRAVITY_BOTTOM
                                )

                                Airbridge.trackEvent("identify", "click", "identify_done", null, null, null)

                                setResult(Activity.RESULT_OK)
                                finish()
                            } else {
                                OneBtnDialog(this).apply {
                                    setTitle("본인인증")
                                    setMessage("고객님의 인증수단이 변경되어, 로그아웃 됩니다.\n다시 로그인을 통해 서비스를 이용해 주세요.")
                                    setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                                        logout()
                                        setResult(Activity.RESULT_OK)
                                        finish()
                                    })
                                    setCancelable(false)
                                    show()
                                }
                            }
                        } catch (e: Exception) {
                            Logger.p(e)
                        }
                    } else {
                        AppToast(this).showToastMessage(
                                mesageKey,
                                AppToast.DURATION_MILLISECONDS_DEFAULT,
                                AppToast.GRAVITY_BOTTOM
                        )
                        setResult(Activity.RESULT_OK)
                        finish()
                    }
                }
            }
        }
    }

    private fun changeDateFormat(birthday:String) : String {
        val sdf = SimpleDateFormat("yyyyMMdd", Locale.KOREAN)
        val sdf1 = SimpleDateFormat("yyyy-MM-dd", Locale.KOREAN)
        try {
            val date = sdf.parse(birthday)
            return sdf1.format(date)
        } catch (e: ParseException) {
            Logger.p(e)
            return ""
        }
    }

    private fun logout() {
        StorageUtils.writeBooleanValueInPreference(this, AppConstants.PREF_KEY_LOGIN_STATUS, false)
        StorageUtils.writeBooleanValueInPreference(this, AppConstants.PREF_KEY_MARKETING_POPUP_NO_SHOW, false)
        StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_ACCESS_TOKEN, "")
        StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_REFRESH_TOKEN, "")
        StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_OLD_TOKEN, "")
        StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_OLD_USER_ID, "")
        StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_USER_ID, "")
        StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_USER_NICK_NAME, "")
        StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_USER_NAME, "")
        StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_USER_EMAIL, "")
        StorageUtils.writeValueInPreference(this, AppConstants.PREF_KEY_ID_GODO, "")
        StorageUtils.writeIntValueInPreference(this, AppConstants.PREF_KEY_USER_LOGIN_TYPE, 0)
        StorageUtils.writeIntValueInPreference(this, AppConstants.PREF_KEY_AUTH_ID, 0)

        val appCache by lazy { GlobalContext.get().koin.get<AppCache>() }
        appCache.clear()
        EventBus.getDefault().post(LogoutEvent())
    }
}