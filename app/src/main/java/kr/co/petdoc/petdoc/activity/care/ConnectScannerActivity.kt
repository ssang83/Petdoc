package kr.co.petdoc.petdoc.activity.care

import android.Manifest
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import androidx.annotation.RequiresApi
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.aramhuvis.lite.base.*
import kotlinx.android.synthetic.main.activity_connect_scanner.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.activity.BaseActivity
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.fragment.scanner.event.ScannerEvent
import kr.co.petdoc.petdoc.fragment.scanner.utils.WifiChecker
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.PermissionCallback
import kr.co.petdoc.petdoc.utils.PermissionStatus
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.viewmodel.ConnectScannerDataModel
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.EventBus
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/**
 * Petdoc
 * Class: ConnectScannerActivity
 * Created by kimjoonsung on 2020/04/29.
 *
 * Description :
 */
class ConnectScannerActivity : BaseActivity() {

    enum class QUERY_STATUS {
        QUERY_STATUS_IDLE,
        QUERY_STATUS_HIDDEN_AP_START,
        QUERY_STATUS_AP_LIST,
        QUERY_STATUS_DO_NOT
    }

    enum class AP_CONNECT_STATUS {
        AP_CONNECT_NONE,
        AP_CONNECT_SEND_WIFI_INFO_WAIT,
        AP_CONNECT_SEND_WIFI_INFO
    }

    companion object {
        var mQueryHiddenSanStatus = QUERY_STATUS.QUERY_STATUS_DO_NOT
        var mAPConnectStatus = AP_CONNECT_STATUS.AP_CONNECT_NONE

        lateinit var instance:ConnectScannerActivity
        lateinit var mWifiManager: WifiManager

    }

    private lateinit var dataModel:ConnectScannerDataModel
    private var mWifiNameCheckTimer: Timer? = null
    private var mApListFoundDialogCanceler: Timer? = null
    private var mDeviceAutoFindTask:AsyncTask<Void, Void, String>? = null

    private var mConnectedWifiName = ""
    private var mDeviceSavedWifiName = ""

    private var mIsPowerOffCauseBattery = false
    private var mIsPowerOffCausePowerKey = false

    private val mIsFirst = true
    private var exceptionCount = 10
    private var isScannerConnect = true

    private var connectivityManager: ConnectivityManager? = null

    private var btInt = -1
    private var prevBtInt = -1
    private var scannerLenseType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_connect_scanner)
        instance = this
        dataModel = ViewModelProvider(this).get(ConnectScannerDataModel::class.java)

        LiteCamera.initialize(
            applicationContext,
            "${externalCacheDir!!.absolutePath}/Petdoc/",
            null
        )

        PetdocApplication.mLiteCamera = LiteCamera.getInstance(mACameraListener)
        mWifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        Helper.permissionCheck(this,
            arrayOf(Manifest.permission.CAMERA,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.ACCESS_COARSE_LOCATION,
                    Manifest.permission.ACCESS_FINE_LOCATION),
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

        stayOnWifi(this)
    }

    override fun onBackPressed() {
        if(connectScannerHost.childFragmentManager?.backStackEntryCount == 0){
            finish()
        }else super.onBackPressed()
    }

    override fun onDestroy() {
        super.onDestroy()
        connectivityManager?.unregisterNetworkCallback(networkCallback)
    }

    override fun onPause() {
        super.onPause()
        if (mWifiNameCheckTimer != null) {
            mWifiNameCheckTimer!!.cancel()
            mWifiNameCheckTimer = null
        }

        if (mApListFoundDialogCanceler != null) {
            mApListFoundDialogCanceler!!.cancel()
            mApListFoundDialogCanceler = null
        }

        stopNameChecker()
    }

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

                            mDeviceSavedWifiName = message.substring(message.indexOf("SSID:") + "SSID:".length)

                            btInt = bt.toInt()
                            val chInt = ch.toInt()

                            if (prevBtInt != btInt) {
                                EventBus.getDefault().post(ScannerEvent(true, false, btInt, scannerLenseType))
                                prevBtInt = btInt
                            }

                            // It is regarded as disconnection due to network latency, except for disconnection due to explicit key and disconnection due to battery.
                            if (chInt == 1 || btInt > 0) {
                                mIsPowerOffCauseBattery = false
                            }

                            StorageUtils.writeIntValueInPreference(this@ConnectScannerActivity, AppConstants.PREF_KEY_SCANNER_ADAPTER_STATUS, chInt)

                            val lqInt = lq.toInt()
                            if (lqInt <= 70 && !TextUtils.isEmpty(mDeviceSavedWifiName)) {
                                mConnectedWifiName = mDeviceSavedWifiName
                            }

                            if (!TextUtils.isEmpty(mDeviceSavedWifiName) && lqInt == 99) {
                                when (mQueryHiddenSanStatus) {
                                    QUERY_STATUS.QUERY_STATUS_IDLE -> {
                                        queryScan(QUERY_STATUS.QUERY_STATUS_HIDDEN_AP_START)
                                    }
                                }
                            }

                            // 펫닥 스캐너 연결 성공 여부 확인
                            if (lqInt == 99 && isScannerConnect) {
                                if (navController().currentDestination?.id == R.id.scannerConnectFragment) {
                                    if(scannerLenseType.isNotEmpty()) {
                                        EventBus.getDefault().post(ScannerEvent(true, false, btInt, scannerLenseType))
                                        isScannerConnect = false
                                    }
                                }
                            }
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
                        if (badBattery == 1) {
                            mIsPowerOffCauseBattery = true
                        }
                        if (powerKey == 1) {
                            mIsPowerOffCausePowerKey = true
                        } else {
                            mIsPowerOffCausePowerKey = false
                        }

                        /*
                         A trick to pass Wifi information to connect after the Socket is fully connected and initialized.
                         */if (mAPConnectStatus == AP_CONNECT_STATUS.AP_CONNECT_SEND_WIFI_INFO_WAIT) {
                            mQueryHiddenSanStatus = QUERY_STATUS.QUERY_STATUS_DO_NOT
                            mAPConnectStatus = AP_CONNECT_STATUS.AP_CONNECT_SEND_WIFI_INFO
                        } else if (mAPConnectStatus == AP_CONNECT_STATUS.AP_CONNECT_SEND_WIFI_INFO) {
                            mQueryHiddenSanStatus = QUERY_STATUS.QUERY_STATUS_DO_NOT
                            mAPConnectStatus = AP_CONNECT_STATUS.AP_CONNECT_NONE
                        } else {
                            mQueryHiddenSanStatus = QUERY_STATUS.QUERY_STATUS_IDLE
                        }
                    }
                })
            }

            return true
        }

        override fun onPreviewReady(): Boolean {
            return true
        }

        override fun onShot(p0: ByteArray): Boolean {
            return true
        }

        override fun onUsbConnectionEvent(state: Int): Boolean {
            return false
        }

        override fun onGetLenseDetect(lenseType: String): Boolean {
            scannerLenseType = lenseType
            Logger.d("lenseType : $lenseType")
            scanner.setCurrentLenseType(lenseType)
            EventBus.getDefault().post(ScannerEvent(true, false, btInt, scannerLenseType))
            return false
        }

        override fun onPreviewFailed(): Boolean {
            return true
        }

        override fun onDisconnection(): Boolean {
            return true
        }

        override fun onEvent(event: String, args: Any?): Boolean {
            Logger.d("onEvent : $event")
            if ("AP_LIST".equals(event)) {
                Logger.d("mQueryHiddenSanStatus : $mQueryHiddenSanStatus")
                when (mQueryHiddenSanStatus) {
                    QUERY_STATUS.QUERY_STATUS_AP_LIST -> {
                        if (args != null) {
                            val apList: MutableList<ApInfo> = args as MutableList<ApInfo>
                            Logger.d("apList : ${apList}")
                            dataModel.apInfoList.value = apList
                            if (navController().currentDestination?.id == R.id.scannerConnectFragment) {
                                EventBus.getDefault().post(ScannerEvent(false, true, btInt, scannerLenseType))
                            }
                        }
                    }

                    QUERY_STATUS.QUERY_STATUS_HIDDEN_AP_START -> {
                        if (args != null) {
                            dataModel.apInfoList.value = args as MutableList<ApInfo>
                            val list = args
                            for (info in list) {
                                if (info.ssid == mDeviceSavedWifiName) {
                                    val sp = getSharedPreferences(
                                            "WIFI_PASSWORD",
                                            Context.MODE_PRIVATE
                                    )
                                    val password = sp.getString(mDeviceSavedWifiName, "")
                                    if (password!!.isNotEmpty()) {
                                        try {
                                            if (info.signalQuality.toInt() > 50) {
                                                wifiCheckAndConnect(info.ssid, password, info.encryption)
                                                break
                                            }
                                        } catch (e: java.lang.Exception) {
                                            e.printStackTrace()
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            } else if("AP_START".equals(event)) {
                mConnectedWifiName = ""
                when (mQueryHiddenSanStatus) {
                    QUERY_STATUS.QUERY_STATUS_AP_LIST -> {
                        Logger.d("주변 공유기를 검색하는중입니다.")
                        startApListCancelTimer()
                    }

                    QUERY_STATUS.QUERY_STATUS_HIDDEN_AP_START -> {}
                }
            } else if ("GET_SERIAL".equals(event)) {
                Logger.d("Serial : $args")
            } else if ("GET_VERSION".equals(event)) {
                Logger.d("Version : $args")
                StorageUtils.writeValueInPreference(this@ConnectScannerActivity, AppConstants.PREF_KEY_SCANNER_FIRMWARE_VER, args.toString())
                StorageUtils.writeValueInPreference(
                    this@ConnectScannerActivity,
                    AppConstants.PREF_KEY_SCANNER_LAST_VERSION_CHECK_DATE,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )
            }

            return true
        }

        override fun onConnection(connectionState: CONNECTION_STATE): Boolean {
            Logger.d("connection state : $connectionState")
            if (connectionState == CONNECTION_STATE.CONNECTION_STATE_DISCONNECTED) {
                if (this@ConnectScannerActivity.lifecycle.currentState != Lifecycle.State.STARTED) {
                    scanner.reBindProcessWithScannerNetwork()
                    Looper.prepare()
                    Handler().postDelayed({
                        if (LiteCamera.getConnectionState() == CONNECTION_STATE.CONNECTION_STATE_DISCONNECTED) {
                            scanner.disconnect()
                        }
                    }, 2000L)
                    Looper.loop()
                }
            } else if (connectionState == CONNECTION_STATE.CONNECTION_STATE_CONNECTION_REQ_FROM_OTHER_DEVICE) {
                scanner.disconnect()
            }

            setConnectionState(connectionState)
            return true
        }

        override fun onPreview(arg0: Bitmap): Boolean {
            return true
        }

        override fun onKey(arg0: KEY_ID, arg1 : KEY_STATE?): Boolean {
            return true
        }
    }

     fun stopNameChecker() {
        if (mWifiNameCheckTimer != null) {
            mWifiNameCheckTimer!!.cancel()
            mWifiNameCheckTimer = null
        }
    }

    fun isAndroidQ() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q

    fun wifiCheckAndConnect(ssid: String, password: String, encryption: String) {
        val info = ApInfo().apply {
            setSsid(ssid)
            setPassword(password)
            setEncryption(encryption)
        }

        object : AsyncTask<Void, String, Boolean?>() {
            private var mAutoConnectApInfo:ApInfo? = null

            override fun onPreExecute() {
                mQueryHiddenSanStatus = mQueryHiddenSanStatus
                stopNameChecker()
            }

            override fun doInBackground(vararg params: Void?): Boolean? {
                PetdocApplication.mLiteCamera?.clear()

                mQueryHiddenSanStatus = QUERY_STATUS.QUERY_STATUS_DO_NOT
                mAPConnectStatus = AP_CONNECT_STATUS.AP_CONNECT_NONE

                var wifiChecker:WifiChecker? = null
                var checkResult = false
                var count = 0
                while (true) {
                    wifiChecker = WifiChecker(applicationContext, info.ssid, info.password, info.encryption)
                    checkResult = wifiChecker!!.startCheck()

                    if (checkResult) {
                        break
                    }

                    if(isAndroidQ()) {
                        break
                    }

                    count = count.inc()

                    if (count > 2) {
                        break
                    }
                }

                Logger.d("checkResult : $checkResult")
                if (checkResult) {
                    savedWifiPassword(info.ssid, info.password)
                    val prevPetdocScanName = StorageUtils.loadValueFromPreference(applicationContext, AppConstants.PREF_KEY_PETDOC_SCAN_NAME, "")
                    val prevPetdocScanPasswd = StorageUtils.loadValueFromPreference(applicationContext, AppConstants.PREF_KEY_PETDOC_SCAN_PASSWORD, "")
                    val encryption = if(prevPetdocScanPasswd.isEmpty()) {
                        "none"
                    } else {
                        "wpa2"
                    }
                    Logger.d("${info.ssid} 와 접속이 가능합니다. ${prevPetdocScanName} 에 정보를 전달하는 중입니다.")

                    for (loop in 0 until 3) {
                        wifiChecker = WifiChecker(applicationContext, prevPetdocScanName, prevPetdocScanPasswd, encryption)
                        checkResult = wifiChecker.startCheck()

                        if (checkResult) {
                            break
                        }

                        if(isAndroidQ()) {
                            break
                        }
                    }

                    if (checkResult) {
                        mAPConnectStatus = AP_CONNECT_STATUS.AP_CONNECT_SEND_WIFI_INFO_WAIT
                        mAutoConnectApInfo = info

                        if (PetdocApplication.mLiteCamera != null) {
                            PetdocApplication.mLiteCamera!!.tryConnect()
                        }

                        while (mAPConnectStatus == AP_CONNECT_STATUS.AP_CONNECT_SEND_WIFI_INFO_WAIT) {
                            try {
                                Logger.d("Wait connect 1")
                                Thread.sleep(200)
                            } catch (e: InterruptedException) {
                                Logger.p(e)
                            }
                        }

                        if (PetdocApplication.mLiteCamera != null) {
                            PetdocApplication.mLiteCamera!!.sendWiFiInfo(mAutoConnectApInfo!!.ssid, mAutoConnectApInfo!!.password, mAutoConnectApInfo!!.encryption)
                        }

                        while (isPetdocScan(getCurrentWifiName())) {
                            try {
                                Thread.sleep(100)
                                Logger.d("Wait disconnect 2")
                            } catch (e: InterruptedException) {
                                Logger.p(e)
                            }
                        }

                        Logger.d("${prevPetdocScanName} 와 다시 연결중입니다.")
                        while (true) {
                            wifiChecker = WifiChecker(applicationContext, prevPetdocScanName, prevPetdocScanPasswd, encryption)
                            val enableResult = wifiChecker!!.startCheck()

                            Logger.d("Wait connect 3, enableResult : $enableResult")
                            if (enableResult) {
                                while (mAPConnectStatus == AP_CONNECT_STATUS.AP_CONNECT_SEND_WIFI_INFO) {
                                    setConnectionState(CONNECTION_STATE.CONNECTION_STATE_WIFI_CONNECTING)
                                    tryConnect()
                                    try {
                                        Logger.d("Wait connect 4")
                                        Thread.sleep(200)
                                    } catch (e: InterruptedException) {
                                        Logger.p(e)
                                    }
                                }
                                break
                            }

                            if(isAndroidQ()) {
                                break
                            }
                        }

                        Logger.d("return 7")
                        return true
                    }
                } else {
                    if (PetdocApplication.mLiteCamera != null) {
                        PetdocApplication.mLiteCamera!!.tryConnect()
                        Logger.d("비밀번호가 다릅니다.")
                    }
                }

                return null
            }

            override fun onPostExecute(result: Boolean?) {
                if (result != null) {
                    Logger.d("set QUERY_STATUS_IDLE 5")
                    mQueryHiddenSanStatus = QUERY_STATUS.QUERY_STATUS_IDLE
                    mAPConnectStatus = AP_CONNECT_STATUS.AP_CONNECT_NONE

//                    startNameChecker(false)
                }
            }

            private fun tryConnect() {
                if(exceptionCount == 0) return

                if (PetdocApplication.mLiteCamera != null) {
                    PetdocApplication.mLiteCamera!!.setConnectionState(CONNECTION_STATE.CONNECTION_STATE_WIFI_CONNECTING)
                    PetdocApplication.mLiteCamera!!.tryConnect {
                        tryConnect()
                        exceptionCount = exceptionCount.dec()
                    }
                }
            }
        }.execute()
    }

    fun queryScan(status: QUERY_STATUS) {
        startApListCancelTimer()

        mQueryHiddenSanStatus = status
        if (PetdocApplication.mLiteCamera != null) {
            PetdocApplication.mLiteCamera!!.queryApScan()
        }
    }

    fun tryConnect() {
        if (exceptionCount == 0) {
            return
        }

        if (PetdocApplication.mLiteCamera != null) {
            PetdocApplication.mLiteCamera!!.tryConnect {
                Logger.p(it)
                exceptionCount = exceptionCount.dec()
                tryConnect()
            }
        }
    }

    fun navController() = Navigation.findNavController(this, R.id.connectScannerHost)

    private fun stopApListCancelTimer() {
        if (mApListFoundDialogCanceler != null) {
            mApListFoundDialogCanceler!!.cancel()
            mApListFoundDialogCanceler = null
        }
    }

    private fun startApListCancelTimer() {
        stopApListCancelTimer()
        mApListFoundDialogCanceler = Timer()
        mApListFoundDialogCanceler!!.schedule(object : TimerTask() {
            override fun run() {
                runOnUiThread {
                    mQueryHiddenSanStatus = QUERY_STATUS.QUERY_STATUS_IDLE
                }
            }
        }, 5000)
    }

    private fun getCurrentWifiName(): String {
        val wifiInfo = mWifiManager.connectionInfo
        var wifi_name = ""
        if (wifiInfo != null && wifiInfo.ssid != null && wifiInfo.supplicantState == SupplicantState.COMPLETED) {
            wifi_name = wifiInfo.ssid
            if (!TextUtils.isEmpty(wifi_name)) {
                wifi_name = wifi_name.replace("\"", "")
            }
        }
        return wifi_name
    }

    fun isPetdocScan(ap_name: String?): Boolean {
        var result = false
        if (ap_name != null) {
            if (ap_name.startsWith("\"PetdocScan_")) {
                result = true
            }
            if (ap_name.startsWith("PetdocScan_")) {
                result = true
            }
            return result
        }
        return result
    }

    fun setConnectionState(state: CONNECTION_STATE) {
        if (LiteCamera.getConnectionState() == CONNECTION_STATE.CONNECTION_STATE_USB) {
            when (state) {
                CONNECTION_STATE.CONNECTION_STATE_DISCONNECTED,
                CONNECTION_STATE.CONNECTION_STATE_DISCONNECTED_BY_OTHER_DEVICE,
                CONNECTION_STATE.CONNECTION_STATE_USB -> {}

                else -> return
            }
        }

        when (state) {
            CONNECTION_STATE.CONNECTION_STATE_DISCONNECTED,
            CONNECTION_STATE.CONNECTION_STATE_DISCONNECTED_BY_OTHER_DEVICE -> {
                /*if (PetdocApplication.mLiteCamera != null) {
                    tryConnect()
                }*/
            }

            CONNECTION_STATE.CONNECTION_STATE_WIFI -> {
                mQueryHiddenSanStatus = QUERY_STATUS.QUERY_STATUS_DO_NOT
            }

            CONNECTION_STATE.CONNECTION_STATE_USB -> {
                if (mDeviceAutoFindTask != null) {
                    mDeviceAutoFindTask!!.cancel(true)
                    mDeviceAutoFindTask = null
                }
            }

            CONNECTION_STATE.CONNECTION_STATE_CONNECT_FAILED -> {
                mAPConnectStatus = AP_CONNECT_STATUS.AP_CONNECT_NONE

                AppToast(this).showToastMessage(
                    "진단기 연결이 실패하였습니다. 다시 시도해 주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            }

            CONNECTION_STATE.CONNECTION_STATE_CONNECTION_REQ_FROM_OTHER_DEVICE -> {
                // If other tablet/phone is want to connect with camera, disconnect this. You can choose disconnectPrevSocket() or disconnectCurrentSocket().
                /*if (PetdocApplication.mLiteCamera != null) {
                    PetdocApplication.mLiteCamera!!.disconnectCurrentSocket()
                    PetdocApplication.mLiteCamera!!.clear()
                    tryConnect()
                }*/
            }
        }
    }

    private fun savedWifiPassword(ssid: String, password: String) {
        val sp = getSharedPreferences("WIFI_PASSWORD", MODE_PRIVATE)
        var et = sp.edit()
        et.clear()
        et.commit()

        et = sp.edit()
        et.putString(ssid, password)
        et.commit()
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private fun stayOnWifi(context: Context) {
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val request = NetworkRequest.Builder()
        request.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

        connectivityManager?.registerNetworkCallback(request.build(), networkCallback)
    }

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onLost(network: Network) {
            super.onLost(network)
            Logger.d("onLost")

        }

        override fun onLosing(network: Network, maxMsToLive: Int) {
            super.onLosing(network, maxMsToLive)
            Logger.d("onLosing")

        }

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            Logger.d("onAvailable")
            scanner.bindProcessToNetwork(network)
        }
    }
}