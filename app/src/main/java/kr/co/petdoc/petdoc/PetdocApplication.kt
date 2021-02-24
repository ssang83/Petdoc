@file:JvmName("PetdocApplication")

package kr.co.petdoc.petdoc

import android.app.Application
import android.content.Context
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import co.ab180.airbridge.Airbridge
import co.ab180.airbridge.AirbridgeConfig
import com.aramhuvis.lite.base.LiteCamera
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import com.jakewharton.threetenabp.AndroidThreeTen
import com.kakao.auth.*
import com.sendbird.android.SendBird
import com.sendbird.desk.android.SendBirdDesk
import com.toast.android.push.PushType
import com.toast.android.push.ToastPush
import com.toast.android.push.ToastPushConfiguration
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.AppForegroundDetector
import kr.co.petdoc.petdoc.common.ToastAPI
import kr.co.petdoc.petdoc.di.appModule
import kr.co.petdoc.petdoc.di.viewModelModule
import kr.co.petdoc.petdoc.fragment.chat.v2.desk.DeskManager
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.ApiClient
import kr.co.petdoc.petdoc.network.response.submodel.*
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.core.context.startKoin
import java.io.IOException
import java.io.InputStream
import java.util.*

/**
 * Petdoc
 * Class: PetdocApplication
 * Created by kimjoonsung on 2020/04/01.
 *
 * Description: Initialization of {@link ApiClient} etc.
 */
class PetdocApplication : Application(), LifecycleObserver {

    private var mApiClient:ApiClient? = null

    companion object {
        lateinit var application: PetdocApplication
            private set

        private var foregroundDetector:AppForegroundDetector? = null

        var mCategoryItem: MutableList<CategoryItem> = mutableListOf()
        var mUserInfo:UserInfoData? = null
        var mTemperatureInfoList:List<TemperatureInfo> = listOf()
        var mPetInfoItem:PetInfoItem? = null            // 상담하기에서 사용하는  펫 정보
        var mChatCategoryItem:ChatCategoryItem? = null  // 상담하기에서 사용하는 카테고리 정보
        var mSearchCategoryList:MutableList<ChatCategoryItem> = mutableListOf() // 검색에서 사용하는 카테고리 리스트
        var currentLat = 0.0
        var currentLng = 0.0
        var mHospitalMarkerItems:MutableList<HospitalItem> = mutableListOf()    // 병원찾기에서 마커 클릭 시 좌표가 동일한 병원 리스트
        var mLiteCamera: LiteCamera? = null     // 스캐너 카메라 인스턴스
        var mPetInfoList:MutableList<PetInfoItem> = mutableListOf()     // 펫 정보 리스트

        @JvmStatic
        fun addActivityLifeCycleListener(listener: AppForegroundDetector.Listener) =
            foregroundDetector?.addListener(listener)

        @JvmStatic
        fun removeActivityLifeCycleListener(listener: AppForegroundDetector.Listener) =
            foregroundDetector?.removeListener(listener)
    }

    val apiClient:ApiClient
        get() = if(mApiClient != null) mApiClient!! else initApiClient()

    override fun onCreate() {
        super.onCreate()
        application = this

        startKoin {
            androidLogger()
            androidContext(this@PetdocApplication)

            modules(modules =
                appModule +
                viewModelModule
            )
        }

        // Kakao SDK 초기화
        KakaoSDK.init(KakaoSDKAdapter())

        // Airebridge 초기화
        val config = AirbridgeConfig.Builder("kr_petdoc", AppConstants.AIR_BRIDGE_TOKEN)
            .build()

        Airbridge.init(this, config)

        AndroidThreeTen.init(this)

        // Toast Push 초기화전
        val configuration = ToastPushConfiguration.newBuilder(this, AppConstants.TOAST_PUSH_APP_KEY).build()
        ToastPush.initialize(PushType.FCM, configuration)

        if (BuildConfig.DEBUG) {
            Logger.setLevel(Logger.ALL)
            Logger.setTag("Petdoc")
        }

        if(StorageUtils.loadValueFromPreference(applicationContext, AppConstants.PREF_KEY_APP_SETUP_UUID_KEY, "").isNullOrBlank()){
            StorageUtils.writeValueInPreference(applicationContext, AppConstants.PREF_KEY_APP_SETUP_UUID_KEY, UUID.randomUUID().toString())
        }

        // SendBird init
        /*SendBird.init(AppConstants.SEND_BIRD_ID, this)
        SendBirdDesk.init()
        DeskManager.init(this)*/

        val categoryJson = inputStreamToString(resources.assets.open("category.json"))
        mCategoryItem = Gson().fromJson(categoryJson, object : TypeToken<MutableList<CategoryItem>>() {}.type)

        ToastAPI.registerToken(this, true, true, true)

        foregroundDetector = AppForegroundDetector()
        registerActivityLifecycleCallbacks(foregroundDetector)

        ProcessLifecycleOwner.get().lifecycle.addObserver(this)

        FirebaseCrashlytics.getInstance().setCrashlyticsCollectionEnabled(!BuildConfig.DEBUG)
    }

    override fun onTerminate() {
        Logger.d("onTerminate")
        unregisterActivityLifecycleCallbacks(foregroundDetector)
        ProcessLifecycleOwner.get().lifecycle.removeObserver(this)

        foregroundDetector = null

        super.onTerminate()
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    private fun onAppForegrounded() {
        Logger.d("App in foreground")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    private fun onAppBackgrounded() {
        Logger.d("App in background")
    }

    /**
     * Initialize the [ApiClient]
     */
    private fun initApiClient(): ApiClient {
        mApiClient = ApiClient(this)
        return mApiClient!!
    }

    /**
     * 홈 카테고리 json 파일 파싱
     */
    fun inputStreamToString(io: InputStream) : String {
        return try {
            val bytes = ByteArray(io.available())
            io.read(bytes, 0, bytes.size)
            val json = String(bytes)
            return json
        } catch (e: IOException) {
            return ""
        }
    }

    //======================================================================================================
    inner class KakaoSDKAdapter : KakaoAdapter() {
        override fun getSessionConfig(): ISessionConfig {
            return object : ISessionConfig {
                override fun isSaveFormData(): Boolean {
                    return true
                }

                override fun getAuthTypes(): Array<AuthType> {
                    return arrayOf(AuthType.KAKAO_LOGIN_ALL)
                }

                override fun isSecureMode(): Boolean {
                    return false
                }

                override fun getApprovalType(): ApprovalType? {
                    return ApprovalType.INDIVIDUAL
                }

                override fun isUsingWebviewTimer(): Boolean {
                    return false
                }
            }
        }

        override fun getApplicationConfig(): IApplicationConfig {
            return object : IApplicationConfig {
                override fun getApplicationContext(): Context {
                    return application
                }
            }
        }
    }
}