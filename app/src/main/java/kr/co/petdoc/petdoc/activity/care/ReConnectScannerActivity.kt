package kr.co.petdoc.petdoc.activity.care

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.AnimationDrawable
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.SupplicantState
import android.net.wifi.WifiManager
import android.os.*
import android.provider.Settings
import android.text.TextUtils
import android.view.View
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Lifecycle
import com.aramhuvis.lite.base.*
import kotlinx.android.synthetic.main.activity_re_connect_scanner.*
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.fragment.scanner.event.ScannerEvent
import kr.co.petdoc.petdoc.fragment.scanner.utils.WifiChecker
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.Helper
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import kr.co.petdoc.petdoc.widget.OneBtnDialog
import kr.co.petdoc.petdoc.widget.toast.AppToast
import org.greenrobot.eventbus.EventBus
import org.koin.android.ext.android.inject
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.util.*

/**
 * Petdoc
 * Class: ReConnectScannerActivity
 * Created by kimjoonsung on 2020/08/21.
 *
 * Description :
 */
class ReConnectScannerActivity : AppCompatActivity() {

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

        const val MSG_SCANNER_CONNECTED = 100
        const val MSG_SCANNER_CONNECTION_FAIL = 101
    }

    private var mWifiNameCheckTimer: Timer? = null
    private var mDeviceAutoFindTask: AsyncTask<Void, Void, String>? = null
    private var cm: ConnectivityManager? = null

    private val scanner: Scanner by inject()

    private lateinit var mWifiManager: WifiManager
    private lateinit var ani:AnimationDrawable

    private var mConnectedWifiName = ""
    private var mDeviceSavedWifiName = ""

    private var mIsPowerOffCauseBattery = false
    private var mIsPowerOffCausePowerKey = false

    private var btInt = -1
    private var prevBtInt = -1
    private var scannerLenseType = ""

    private var uiHandler = Handler {
        when (it.what) {
            MSG_SCANNER_CONNECTED -> {
                if (!StorageUtils.loadBooleanValueFromPreference(applicationContext, AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS)) {
                    textViewConnecting.text = "펫닥 스캐너 연결 완료"
                    btnConfirm.visibility = View.VISIBLE
                    progressScanner.setBackgroundResource(R.drawable.ic_connect_scanner_compeletion)

                    StorageUtils.writeBooleanValueInPreference(applicationContext, AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS, true)

                    if(scannerLenseType.isNotEmpty()) {
                        EventBus.getDefault().post(ScannerEvent(true, false, btInt, scannerLenseType))
                    }
                }
            }
            MSG_SCANNER_CONNECTION_FAIL -> {
                AppToast(this).showToastMessage(
                    "진단기 연결이 실패하였습니다. 다시 시도해 주세요.",
                    AppToast.DURATION_MILLISECONDS_DEFAULT,
                    AppToast.GRAVITY_BOTTOM
                )
            }
        }
        true
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        Helper.statusBarColorChange(this, true, alpha = 0)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_re_connect_scanner)

        LiteCamera.initialize(
            applicationContext,
            "${externalCacheDir!!.absolutePath}/Petdoc/",
            null
        )

        PetdocApplication.mLiteCamera = LiteCamera.getInstance(mACameraListener)
        mWifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager

        btnClose.setOnClickListener { finish() }

        btnConfirm.setOnClickListener {
            AppToast(applicationContext).showToastMessage(
                "펫닥 스캐너와 연결이 완료되었습니다.",
                AppToast.DURATION_MILLISECONDS_DEFAULT,
                AppToast.GRAVITY_BOTTOM
            )

            finish()
        }

        ani = progressScanner.background as AnimationDrawable
        ani.start()

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            stayOnWifi(applicationContext)
            startNameChecker(true)
        } else {
            val ssid = StorageUtils.loadValueFromPreference(applicationContext, AppConstants.PREF_KEY_PETDOC_SCAN_NAME, "")
            val passPhrase = StorageUtils.loadValueFromPreference(applicationContext, AppConstants.PREF_KEY_PETDOC_SCAN_PASSWORD, "")
            scanner.tryConnect(ssid, passPhrase)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        cm?.unregisterNetworkCallback(callback)

        stopNameChecker()
    }

    override fun onPause() {
        super.onPause()
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

                            StorageUtils.writeIntValueInPreference(this@ReConnectScannerActivity, AppConstants.PREF_KEY_SCANNER_ADAPTER_STATUS, chInt)

                            val lqInt = lq.toInt()
                            if (lqInt <= 70 && !TextUtils.isEmpty(mDeviceSavedWifiName)) {
                                mConnectedWifiName = mDeviceSavedWifiName
                            }

                            if (!TextUtils.isEmpty(mDeviceSavedWifiName) && lqInt == 99) {
                                when (mQueryHiddenSanStatus) {
                                    QUERY_STATUS.QUERY_STATUS_IDLE -> {
//                                        queryScan(QUERY_STATUS.QUERY_STATUS_HIDDEN_AP_START)
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

        override fun onShot(p0: ByteArray?): Boolean {
            return true
        }

        override fun onUsbConnectionEvent(state: Int): Boolean {
            return false
        }

        override fun onGetLenseDetect(lenseType: String): Boolean {
            scannerLenseType = lenseType
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
                Logger.d("mQueryHiddenSanStatus : ${mQueryHiddenSanStatus}")
                when (mQueryHiddenSanStatus) {
                    QUERY_STATUS.QUERY_STATUS_AP_LIST -> {
                        if (args != null) {
                            val apList: MutableList<ApInfo> = args as MutableList<ApInfo>
                            Logger.d("apList : ${apList}")
                        }
                    }

                    QUERY_STATUS.QUERY_STATUS_HIDDEN_AP_START -> {
                        if (args != null) {
                            val apList: MutableList<ApInfo> = args as MutableList<ApInfo>
                            Logger.d("apList : ${apList}")
                        }
                    }
                }
            } else if("AP_START".equals(event)) {
                mConnectedWifiName = ""
                when (mQueryHiddenSanStatus) {
                    QUERY_STATUS.QUERY_STATUS_AP_LIST -> {
                        Logger.d("주변 공유기를 검색하는중입니다.")
                    }

                    QUERY_STATUS.QUERY_STATUS_HIDDEN_AP_START -> {}
                }
            } else if ("GET_SERIAL".equals(event)) {
                Logger.d("Serial : $args")
            } else if ("GET_VERSION".equals(event)) {
                Logger.d("Version : $args")
                StorageUtils.writeValueInPreference(this@ReConnectScannerActivity, AppConstants.PREF_KEY_SCANNER_FIRMWARE_VER, args.toString())
                StorageUtils.writeValueInPreference(
                    this@ReConnectScannerActivity,
                    AppConstants.PREF_KEY_SCANNER_LAST_VERSION_CHECK_DATE,
                    LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"))
                )
            }

            return true
        }

        override fun onConnection(connectionState: CONNECTION_STATE): Boolean {
            Logger.d("connection state : $connectionState")
            if (connectionState == CONNECTION_STATE.CONNECTION_STATE_WIFI) {
                uiHandler.removeMessages(MSG_SCANNER_CONNECTION_FAIL)
                uiHandler.sendEmptyMessageDelayed(MSG_SCANNER_CONNECTED, 1000L)
            }
            else if (connectionState == CONNECTION_STATE.CONNECTION_STATE_DISCONNECTED) {
                if (this@ReConnectScannerActivity.lifecycle.currentState != Lifecycle.State.STARTED) {
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
            } else if (connectionState == CONNECTION_STATE.CONNECTION_STATE_CONNECT_FAILED) {
                uiHandler.removeMessages(MSG_SCANNER_CONNECTED)
                uiHandler.sendEmptyMessageDelayed(MSG_SCANNER_CONNECTION_FAIL, 1000L)

                mAPConnectStatus = AP_CONNECT_STATUS.AP_CONNECT_NONE
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

    private fun startNameChecker(isFirstCall: Boolean) {
        mWifiNameCheckTimer = Timer()
        mWifiNameCheckTimer!!.scheduleAtFixedRate(object : TimerTask() {
            override fun run() {
                val wifi_name = getCurrentWifiName()
                val prevPetdocScanName = StorageUtils.loadValueFromPreference(
                    applicationContext,
                    AppConstants.PREF_KEY_PETDOC_SCAN_NAME,
                    ""
                )
                Logger.d("wifi_name : $wifi_name, prevPetdocScanName : $prevPetdocScanName")
                if (prevPetdocScanName.isNotEmpty()) {
                    if (mDeviceAutoFindTask == null) {
                        mDeviceAutoFindTask = object : AsyncTask<Void, Void, String>() {
                            override fun doInBackground(vararg params: Void?): String {
                                while (true) {
                                    try {
                                        Logger.d("while loop")
                                        if (!mWifiManager.isWifiEnabled) {
                                            mWifiManager.isWifiEnabled = true
                                            Thread.sleep(500)
                                        }

                                        val prevPetdocScanName =
                                            StorageUtils.loadValueFromPreference(
                                                applicationContext,
                                                AppConstants.PREF_KEY_PETDOC_SCAN_NAME,
                                                ""
                                            )
                                        val prevPetdocScanPasswd =
                                            StorageUtils.loadValueFromPreference(
                                                applicationContext,
                                                AppConstants.PREF_KEY_PETDOC_SCAN_PASSWORD,
                                                ""
                                            )
                                        val encrypt = if (prevPetdocScanPasswd.isEmpty()) {
                                            "none"
                                        } else {
                                            "wpa2"
                                        }

                                        var checkResult = false
                                        for (loop in 0 until 3) {
                                            val checker = WifiChecker(
                                                applicationContext,
                                                prevPetdocScanName,
                                                prevPetdocScanPasswd,
                                                encrypt
                                            )
                                            checkResult = checker.startCheck()
                                            Logger.d("checkResult : $checkResult")
                                            if (checkResult) {
                                                break
                                            }
                                        }

                                        if (checkResult) {
                                            return prevPetdocScanName
                                        } else {
                                            return ""
                                        }
                                    } catch (e: Exception) {
                                        Logger.p(e)
                                    }
                                }

                                Logger.d("exit loop")
                                return ""
                            }

                            override fun onPostExecute(connectedDevices: String) {
                                mDeviceAutoFindTask = null
                                Logger.d("connectedDevice : $connectedDevices")
                                ani.stop()

                                if (connectedDevices.isNotEmpty()) {
                                    if (PetdocApplication.mLiteCamera != null) {
                                        PetdocApplication.mLiteCamera!!.tryConnect {}
                                    }
                                } else {
                                    if (mDeviceAutoFindTask != null) {
                                        mDeviceAutoFindTask!!.cancel(true)
                                        mDeviceAutoFindTask = null
                                    }

                                    if (mWifiNameCheckTimer != null) {
                                        mWifiNameCheckTimer!!.cancel()
                                        mWifiNameCheckTimer = null
                                    }

                                    OneBtnDialog(applicationContext).apply {
                                        setTitle("WIFI SSID 인증 실패")
                                        setMessage("WIFI SSID 인증 실패하였습니다. 아이디와 비밀번호를 확인하세요.")
                                        setConfirmButton(Helper.readStringRes(R.string.confirm), View.OnClickListener {
                                            dismiss()
                                            finish()
                                        })
                                        show()
                                    }
                                }
                            }
                        }

                        runOnUiThread { mDeviceAutoFindTask!!.execute() }
                    }
                }

            }
        }, 500, 1000)
    }

    private fun stopNameChecker() {
        if (mWifiNameCheckTimer != null) {
            mWifiNameCheckTimer!!.cancel()
            mWifiNameCheckTimer = null
        }
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

    private fun setConnectionState(state: CONNECTION_STATE) {
        when (state) {
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
            }
        }
    }

    private fun stayOnWifi(context: Context) {
        cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)

        cm?.registerNetworkCallback(request.build(), callback)
    }

    private val callback = object : ConnectivityManager.NetworkCallback() {
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