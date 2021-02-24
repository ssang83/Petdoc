package kr.co.petdoc.petdoc.activity

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.Window
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.aramhuvis.lite.base.*
import com.google.android.gms.location.*
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.BuildConfig
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.care.HealthCareActivity
import kr.co.petdoc.petdoc.activity.chat.ChatHomeActivity
import kr.co.petdoc.petdoc.activity.chat.ChatMessageActivity
import kr.co.petdoc.petdoc.activity.chat.ChatQuitActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.config.RemoteConfigManager
import kr.co.petdoc.petdoc.fragment.*
import kr.co.petdoc.petdoc.fragment.scanner.event.ScannerEvent
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.network.ApiClient
import kr.co.petdoc.petdoc.network.response.AbstractApiResponse
import kr.co.petdoc.petdoc.network.response.ChatListResponse
import kr.co.petdoc.petdoc.network.response.NetworkBusResponse
import kr.co.petdoc.petdoc.network.response.submodel.PetInfoItem
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe
import org.greenrobot.eventbus.ThreadMode
import org.koin.android.ext.android.inject
import java.util.*

class MainActivity : AppCompatActivity(), LevelCheckDialogFragment.CallbackListener {

    companion object {
        const val TAB_HOME = 0
        const val TAB_HOSPITAL = 1
        const val TAB_CONSULTING = 2
        const val TAB_CARE = 3
        const val TAB_SHOPPING = 4

        private const val REQUEST_HEALTH_CARE = 0x1001

        lateinit var instance:MainActivity
    }

    private val LOGTAG = "MainActivity"
    private val CHAT_LIST_REQUEST_TAG = "$LOGTAG.chatListRequest"
    private val PET_INFO_LIST_REQUEST_TAG = "$LOGTAG.petInfoListRequest"

    private lateinit var mEventBus: EventBus
    protected lateinit var mApiClient: ApiClient

    private var connectivityManager: ConnectivityManager? = null

    private var mTimer: Timer? = null
    private var mPressFirstBackKey = false

    private var latestVersionCode = 0
    private var notiId = ""
    private var notiChatStatus = ""
    private var notiChatPartner = ""
    private var deepLinkType = ""
    private var deepLinkId = ""
    private var deepLinkBookingId = ""
    private var booking = ""

    private var btInt = -1

    private val apiService: PetdocApiService by inject()
    private val exceptionHandler = CoroutineExceptionHandler { _, t ->
        Logger.p(t)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)

        if (intent != null) { // 푸시로 넘어오는 경우 처리(Foreground)
            val notiType = intent.getStringExtra("noti_type") ?: ""
            notiId = intent.getStringExtra("noti_id") ?: ""
            booking = intent.getStringExtra("booking") ?: booking
            Logger.d("notiType : $notiType, booking : $booking")
            handlePush(notiType)
            handleBookingCompleted(booking)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestWindowFeature(Window.FEATURE_NO_TITLE)
        Helper.statusBarColorChange(this, true, alpha=0)
        setContentView(R.layout.activity_main)
        instance = this

        mEventBus = EventBus.getDefault()
        mApiClient = PetdocApplication.application.apiClient

        if(!mEventBus.isRegistered(this)) {
            mEventBus.register(this)
        }

        if (intent != null) {   // 푸시로 넘어오는 경우 처리(background)
            val notiType = intent.getStringExtra("noti_type") ?: ""
            notiId = intent.getStringExtra("noti_id") ?: ""
            deepLinkType = intent.getStringExtra("deepLinkType") ?: deepLinkType
            deepLinkId = intent.getStringExtra("deepLinkId") ?: deepLinkId
            deepLinkBookingId = intent.getStringExtra("deepLinkBookingId") ?: deepLinkBookingId

            Logger.d("notiType : $notiType, deepLinkType : $deepLinkType, deepLinkId : $deepLinkId, deepLinkBookingId : $deepLinkBookingId")
            handlePush(notiType)
            handleDeepLink(deepLinkType)
        }

        viewPager.apply {
            adapter = MainPagerAdapter()
            isUserInputEnabled = false
            offscreenPageLimit = 4
        }

        btnHome.setOnClickListener(clickListener)
        btnCare.setOnClickListener(clickListener)
        btnChat.setOnClickListener(clickListener)
        btnHospital.setOnClickListener(clickListener)
        btnShopping.setOnClickListener(clickListener)

        btnHome.isSelected = true

        StorageUtils.writeBooleanValueInPreference(this, AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS, false)

        // App Version Check
        RemoteConfigManager.getInstance().init()
        RemoteConfigManager.getInstance().fetchAndActivate(this,
            object : OnCompleteListener<Boolean> {
                override fun onComplete(task: Task<Boolean>) {
                    if (task.isSuccessful) {
                        val updated = task.result
                        try {
                            latestVersionCode = RemoteConfigManager.getInstance().getString(AppConstants.LATEST_VERSION_CODE).toInt()
                            Logger.d("Config Params updated : $updated, versionCode : $latestVersionCode")
                            if (latestVersionCode > BuildConfig.VERSION_CODE) {
                                showUpdatePopup()
                            }
                        } catch (e: NumberFormatException) {
                            Logger.p(e)
                            latestVersionCode = 0
                        }
                    } else {
                        Logger.d("Fail to load remote config")
                    }
                }
            })

        FirebaseMessaging.getInstance().token.addOnCompleteListener(OnCompleteListener { task ->
            if (!task.isSuccessful) {
                Logger.e("Fetching FCM registration token failed : ${task.exception}")
                return@OnCompleteListener
            }

            val token = task.result
            Logger.e("newToken : $token")
            if (token.isNotEmpty()) {
                StorageUtils.writeValueInPreference(this@MainActivity, AppConstants.PREF_KEY_FCM_PUSH_TOKEN, token)
            }
        })

        connectivityManager = getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        loadData()
    }

    override fun onBackPressed() {
        if (!mPressFirstBackKey) {
            showSecondBackkey()
        } else {
            finish()
        }
    }

    override fun onDestroy() {
        if (mApiClient.isRequestRunning(CHAT_LIST_REQUEST_TAG)) {
            mApiClient.cancelRequest(CHAT_LIST_REQUEST_TAG)
        }

        if (mApiClient.isRequestRunning(PET_INFO_LIST_REQUEST_TAG)) {
            mApiClient.cancelRequest(PET_INFO_LIST_REQUEST_TAG)
        }

        if(mEventBus.isRegistered(this)) {
            mEventBus.unregister(this)
        }

        val scanner: Scanner by inject()
        scanner.disconnect()

        super.onDestroy()
    }

    override fun goToRecord() {
        moveToCare()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            REQUEST_HEALTH_CARE -> {
                mApiClient.getPetInfoList(PET_INFO_LIST_REQUEST_TAG)
            }

            else -> {
                super.onActivityResult(requestCode, resultCode, data)
            }
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
    fun onEventMainThread(data: NetworkBusResponse) {
        when(data.tag) {
            PET_INFO_LIST_REQUEST_TAG -> {
                if ("ok" == data.status) {
                    val items: MutableList<PetInfoItem> = Gson().fromJson(
                        data.response,
                        object : TypeToken<MutableList<PetInfoItem>>() {}.type
                    )

                    var result = false
                    var petName = ""
                    var petImage = ""
                    for (i in 0 until items.size) {
                        if (deepLinkId == items[i].id.toString()) {
                            result = true
                            petImage = items[i].imageUrl!!
                            petName = items[i].name!!
                            break
                        }
                    }

                    if (result) {
                        startActivity(Intent(this, MyPageActivity::class.java).apply {
                            putExtra("fromHealthCare", true)
                            putExtra("bookingId", deepLinkBookingId.toInt())
                            putExtra("petName", petName)
                            putExtra("petImage", petImage)
                            putExtra("petId", deepLinkId)
                            putExtra("deepLink", true)
                        })
                    } else {
                        AppToast(this).showToastMessage(
                            "검진 결과에 해당하는 반려동물이 없습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(response: AbstractApiResponse) {
        when (response.requestTag) {
            CHAT_LIST_REQUEST_TAG -> {
                if (response is ChatListResponse) {
                    if (response.chatList.size > 0) {
                        for (item in response.chatList) {
                            if (item.pk == notiId.toInt()) {
                                notiChatStatus = item.status
                                notiChatPartner = item.partner?.name.toString()
                                launchNotiChat(notiChatStatus)
                            }
                        }
                    } else {
                        AppToast(this).showToastMessage(
                            "상담내역이 없습니다.",
                            AppToast.DURATION_MILLISECONDS_DEFAULT,
                            AppToast.GRAVITY_BOTTOM
                        )
                    }
                }
            }
        }
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    fun onEventMainThread(data: String) {
        Logger.d("data : $data")
        when (data) {
            "onMessageReceived" -> badgeStatus.visibility = View.VISIBLE
            "PetTalk" -> startActivity(Intent(this, PetTalkActivity::class.java))
            "MyPage" -> startActivity(Intent(this, MyPageActivity::class.java))
            "ShoppingTap" -> moveToShopping()
            "chatting" -> startActivity(Intent(this, ChatHomeActivity::class.java))
            "CareTap" -> moveToCare()
            "Hospital" -> moveToHospital()
            "Question" -> startActivity(Intent(this, AppReportActivity::class.java))
            "BodyTemp", "Humidity", "Ear"-> {
                PetdocApplication.mLiteCamera = LiteCamera.getInstance(mACameraListener)
            }
        }
    }

    fun moveToChat() {
        viewPager.setCurrentItem(TAB_CONSULTING, false)

        btnHome.isSelected = false
        btnCare.isSelected = false
        btnChat.isSelected = true
        btnHospital.isSelected = false
        btnShopping.isSelected = false
    }

    fun moveToCare() {
        viewPager.setCurrentItem(TAB_CARE, false)

        btnHome.isSelected = false
        btnCare.isSelected = true
        btnChat.isSelected = false
        btnHospital.isSelected = false
        btnShopping.isSelected = false
    }

    fun moveToHospital() {
        viewPager.setCurrentItem(TAB_HOSPITAL, false)

        btnHome.isSelected = false
        btnCare.isSelected = false
        btnChat.isSelected = false
        btnHospital.isSelected = true
        btnShopping.isSelected = false
    }

    fun moveToShopping() {
        viewPager.setCurrentItem(TAB_SHOPPING, false)

        btnHome.isSelected = false
        btnCare.isSelected = false
        btnChat.isSelected = false
        btnHospital.isSelected = false
        btnShopping.isSelected = true
    }

    private fun loadData() {
        lifecycleScope.launch {
            val categoryResponse = withContext(Dispatchers.IO + exceptionHandler) { apiService.getChatCategory() }
            if(categoryResponse.code == "SUCCESS") {
                PetdocApplication.mSearchCategoryList.clear()
                PetdocApplication.mSearchCategoryList.addAll(categoryResponse.resultData)
            }

            val temperatureResponse = withContext(Dispatchers.IO + exceptionHandler) { apiService.getTemperatureInfo() }
            PetdocApplication.mTemperatureInfoList = temperatureResponse.info
            Logger.d("temperature info : ${PetdocApplication.mTemperatureInfoList.size}")
        }
    }

    inner class MainPagerAdapter : FragmentStateAdapter(this) {

        private val mFragmentList: MutableList<Fragment> = mutableListOf()

        init {
            mFragmentList.apply {
                add(PetHomeFragment())
                add(PetHospitalFragment())
                add(PetChatFragment())
                add(PetCareFragment())
                add(PetShoppingFragment())
            }
        }

        override fun getItemCount() = mFragmentList.size
        override fun createFragment(position: Int) = mFragmentList[position]
    }

    private fun handleBottomButtonImage(view: View) {
        btnHome.isSelected = false
        btnCare.isSelected = false
        btnChat.isSelected = false
        btnHospital.isSelected = false
        btnShopping.isSelected = false

        view.isSelected = true
    }

    private val clickListener = View.OnClickListener {
        when (it.id) {
            R.id.btnHome -> {
                viewPager.setCurrentItem(TAB_HOME, false)
                handleBottomButtonImage(it)
                btnHome.isSelected = true
            }

            R.id.btnCare -> {
                viewPager.setCurrentItem(TAB_CARE, false)
                handleBottomButtonImage(it)
            }

            R.id.btnChat -> {
                viewPager.setCurrentItem(TAB_CONSULTING, false)
                handleBottomButtonImage(it)
            }

            R.id.btnHospital -> {
                viewPager.setCurrentItem(TAB_HOSPITAL, false)
                handleBottomButtonImage(it)
            }

            R.id.btnShopping -> {
                viewPager.setCurrentItem(TAB_SHOPPING, false)
                handleBottomButtonImage(it)
            }
        }
    }

    private fun handlePush(type: String) {
        when (type) {
            "banner" -> {
                startActivity(Intent(this, BannerDetailActivity::class.java).apply {
                    putExtra("bannerId", notiId.toInt())
                })
            }

            "chat" -> {
                mApiClient.getChatList(CHAT_LIST_REQUEST_TAG)
            }

            "petTalk" -> {
                startActivity(Intent(this, PetTalkDetailActivity::class.java).apply {
                    putExtra("petTalkId", notiId.toInt())
                })
            }
        }
    }

    private fun handleDeepLink(type: String) {
        when (type) {
            "banner" -> {
                startActivity(Intent(this, BannerDetailActivity::class.java).apply {
                    putExtra("bannerId", deepLinkId.toInt())
                })
            }

            "chat" -> {
                startActivity(Intent(this, ChatQuitActivity::class.java).apply {
                    putExtra("chatPk", deepLinkId.toInt())
                })
            }

            "magazine" -> {
                startActivity(Intent(this, MagazineDetailActivity::class.java).apply {
                    putExtra("id", deepLinkId.toInt())
                })
            }

            "shortcut" -> launchShortCut(deepLinkId)
            "home" -> launchMainMenu(deepLinkId)
            "sign" -> {
                if(!StorageUtils.loadBooleanValueFromPreference(this, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                    startActivity(Intent(this, LoginAndRegisterActivity::class.java))
                }
            }

            "HcResult" -> {
                if(StorageUtils.loadBooleanValueFromPreference(this, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                    mApiClient.getPetInfoList(PET_INFO_LIST_REQUEST_TAG)
                } else {
                    startActivityForResult(Intent(this, LoginAndRegisterActivity::class.java), REQUEST_HEALTH_CARE)
                }
            }
        }
    }

    private fun handleBookingCompleted(type: String) {
        when (type) {
            "hospital" -> moveToHospital()
            else -> btnHome.isSelected = true
        }
    }

    private fun showUpdatePopup() {
        OneBtnDialog(this).apply {
            setTitle("업데이트")
            setMessage("최신버전이 업데이트 되었습니다.\n아래 확인 버튼을 클릭하시기 바랍니다.")
            setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                startActivity(Intent(Intent.ACTION_VIEW).apply {
                    data = Uri.parse("market://details?id=$packageName")
                })
            })
            setCancelable(false)
            show()
        }
    }

    private fun launchNotiChat(status: String) {
        when (status) {
            "end" -> {
                startActivity(Intent(this, ChatQuitActivity::class.java).apply {
                    putExtra("chatPk", notiId.toInt())
                })
            }

            else -> {
                startActivity(Intent(this, ChatMessageActivity::class.java).apply {
                    putExtra("chatPk", notiId.toInt())
                    putExtra("status", notiChatStatus)
                    putExtra("name", notiChatPartner)
                })
            }
        }
    }

    private fun launchShortCut(id: String) {
        when (id) {
            "HealthCheck" -> {
                if(StorageUtils.loadBooleanValueFromPreference(this, AppConstants.PREF_KEY_LOGIN_STATUS)) {
                    startActivity(Intent(this, HealthCareActivity::class.java))
                }else{
                    startActivity(Intent(this, LoginAndRegisterActivity::class.java))
                }
            }
            "Customeal" -> moveToShopping()
            "Booking" -> moveToHospital()
            "Chat" -> moveToChat()
            "Scanner" -> moveToCare()
            else -> {}
        }
    }

    private fun launchMainMenu(id: String) {
        when (id) {
            "home" -> btnHome.isSelected = true
            "center" -> moveToHospital()
            "chat" -> moveToChat()
            "care" -> moveToCare()
            "shop" -> moveToShopping()
            else -> { }
        }
    }

    /**
     * '뒤로가기' 버튼 2회 연속 입력을 통한 종료를 사용자에게 안내하고 처리
     */
    private fun showSecondBackkey() {
        AppToast(this).showToastMessage(
                Helper.readStringRes(R.string.msg_exit_program_double_back),
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM
        )

        mPressFirstBackKey = true
        val cancelTask:TimerTask = object : TimerTask() {
            override fun run() {
                mTimer?.cancel()
                mTimer = null
                mPressFirstBackKey = false
            }
        }

        mTimer?.cancel()
        mTimer = null

        mTimer = Timer()
        mTimer!!.schedule(cancelTask, AppConstants.DOUBLE_BACK_PRESS_EXITING_TIME_LIMIT)
    }

    //==============================================================================================
    private val mACameraListener = object : ACameraListener {
        override fun onHydration(hydration: Int, elasticity: Int): Boolean {
            return true
        }

        override fun onHydrationIdle(): Boolean {
            return true
        }

        override fun onMessageReceived(message: String): Boolean {
            Logger.d("message : $message")
            if (message.isNotEmpty()) {
                runOnUiThread({
                    /*
                     * bt : from 0 to 5, 5 is full battery status.
                     * ch : 4 is charging now, 1 is not charging now.
                     */
                    if (message.contains("BT:")) {
                        try {
                            val bt = message.substring(
                                    message.indexOf("BT:") + "BT:".length,
                                    message.indexOf("BT:") + "BT:".length + 1
                            )
                            val ch = message.substring(
                                    message.indexOf("CH:") + "CH:".length,
                                    message.indexOf("CH:") + "CH:".length + 1
                            )
                            val lq = message.substring(
                                    message.indexOf("LQ:") + "LQ:".length,
                                    message.indexOf("LQ:") + "SSID:".length
                            )

                            btInt = bt.toInt()
                            val chInt = ch.toInt()
                            Logger.d("bt : $btInt, chInt : $chInt, lq : ${lq}")
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    } else if (message.contains("POWEROFF_NOTIFY")) {
                        // It is regarded as disconnection due to network latency, except for disconnection due to explicit key and disconnection due to battery.
                        val bat = message.substring(message.length - 3, message.length - 2)
                        val pow = message.substring(message.length - 1)

                        val badBattery = bat.toInt()
                        val powerKey = pow.toInt()
                        // X : BAD BATTERY 1 or 0
                        // Y : POWER KEY pressed 1 or 0
                    }
                })
            }

            return true
        }

        override fun onPreviewReady(): Boolean {
            return true
        }

        override fun onShot(p0: ByteArray?): Boolean {
            return true
        }

        override fun onUsbConnectionEvent(state: Int): Boolean {
            return false
        }

        override fun onGetLenseDetect(lenseType: String): Boolean {
            Logger.d("lenseType : $lenseType")
            val scanner: Scanner by inject()
            scanner.setCurrentLenseType(lenseType)
            EventBus.getDefault().post(ScannerEvent(true, false, btInt, lenseType))
            return true
        }

        override fun onPreviewFailed(): Boolean {
            return true
        }

        override fun onDisconnection(): Boolean {
            return true
        }

        override fun onEvent(event: String, args: Any?): Boolean {
            return true
        }

        override fun onConnection(arg0: CONNECTION_STATE): Boolean {
            val scanner: Scanner by inject()
            if (arg0 == CONNECTION_STATE.CONNECTION_STATE_DISCONNECTED) {
                scanner.reBindProcessWithScannerNetwork()
                Looper.prepare()
                Handler().postDelayed({
                    if (LiteCamera.getConnectionState() == CONNECTION_STATE.CONNECTION_STATE_DISCONNECTED) {
                        scanner.disconnect()
                    }
                }, 2000L)
                Looper.loop()
            } else if (arg0 == CONNECTION_STATE.CONNECTION_STATE_CONNECTION_REQ_FROM_OTHER_DEVICE) {
                scanner.disconnect()
            }
            return true
        }

        override fun onPreview(arg0: Bitmap): Boolean {
            return true
        }

        override fun onKey(arg0: KEY_ID, arg1 : KEY_STATE?): Boolean {
            return true
        }
    }
}
