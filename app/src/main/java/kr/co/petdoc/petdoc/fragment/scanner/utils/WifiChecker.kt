package kr.co.petdoc.petdoc.fragment.scanner.utils

import android.annotation.SuppressLint
import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.SupplicantState
import android.net.wifi.WifiConfiguration
import android.net.wifi.WifiManager
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import android.text.TextUtils
import androidx.annotation.RequiresApi
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.scanner.Scanner
import org.koin.core.context.GlobalContext
import java.util.*

/**
 * Petdoc
 * Class: WifiChecker
 * Created by kimjoonsung on 2020/05/04.
 *
 * Description :
 */
class WifiChecker(
    context: Context,
    ssid: String?,
    password: String?,
    encrypt: String?
) {
    private var mSSID = ssid
    private var mPassword = password
    private var mEncrypt = encrypt
    private var mContext = context

    private var mWifiManager: WifiManager? = null

    private val isRunning = true
    private var mIsSuccess = false
    private var enableResult = false

    fun startCheck() = startCheckOld()

    private fun doCompare() {
        var checkCount = 0
        var checkStartTime = Calendar.getInstance().timeInMillis

        while (isRunning) {
            val currentName = getCurrentWifiName()
            Logger.d("doCompare, currentName : $currentName, mSSID : $mSSID, checkCount : $checkCount")
            if (mSSID == currentName) {
                checkCount += 1
            } else {
                checkCount = 0
            }
            try {
                Thread.sleep(200)
            } catch (e: InterruptedException) {
                e.printStackTrace()
            }
            if (checkCount > 10) {
                mIsSuccess = true
                break
            }
            val current = Calendar.getInstance().timeInMillis
            if (current - checkStartTime > 10 * 1000) {
                if (checkCount == 0) {
                    Logger.d("break in time")
                    break
                } else {
                    checkStartTime += 5000
                    Logger.d("continue, checkCOunt : $checkCount")
                }
            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun startCheckOld(): Boolean {
        val isOpen = mEncrypt == "none"
        val isWEP = mEncrypt == "wep"

        if (isOpen) {
            val conf = WifiConfiguration()
            conf.SSID = ScanResultUtil.createQuotedSSID(mSSID!!)
            conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
            val list = mWifiManager!!.configuredNetworks
            for (i in list) {
                if (i.SSID == mSSID) {
                    enableResult = mWifiManager!!.enableNetwork(i.networkId, true)
                    break
                }
            }
            Logger.d("startCheck, enableResult 1 : $enableResult")
            if (!enableResult) {
                val addResult = mWifiManager!!.addNetwork(conf)
                if (addResult >= 0) {
                    enableResult = mWifiManager!!.enableNetwork(addResult, true)
                }
            }
            Logger.d("startCheck, enableResult 2 : $enableResult")
        } else {
            val conf = WifiConfiguration()
            conf.SSID = ScanResultUtil.createQuotedSSID(mSSID!!)
            conf.status = WifiConfiguration.Status.DISABLED
            conf.priority = 40
            if (isWEP) {
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE)
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN)
                conf.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED)
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104)
                conf.wepKeys[0] = ScanResultUtil.createQuotedSSID(mPassword!!)
                conf.wepTxKeyIndex = 0
            } else {
                conf.allowedProtocols.set(WifiConfiguration.Protocol.RSN)
                conf.allowedProtocols.set(WifiConfiguration.Protocol.WPA)
                conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK)
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP)
                conf.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP)
                conf.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP)
                conf.preSharedKey = ScanResultUtil.createQuotedSSID(mPassword!!)
            }

            val list = mWifiManager!!.configuredNetworks
            for (i in list) {
                Logger.d("i.SSID : ${i.SSID} , mSSID : $mSSID, mPassword : $mPassword, mEncrypt : $mEncrypt")
                if (i.SSID == ScanResultUtil.createQuotedSSID(mSSID!!)) {
                    val updateResult = mWifiManager!!.updateNetwork(conf)
                    if (updateResult >= 0) {
                        enableResult = mWifiManager!!.enableNetwork(i.networkId, true)
                    }
                    Logger.d("updateResult : $updateResult, enableResult : $enableResult")
                }
            }

            if (!enableResult) {
                val addResult = mWifiManager!!.addNetwork(conf)
                if (addResult >= 0) {
                    enableResult = mWifiManager!!.enableNetwork(addResult, true)
                }
                Logger.d("addResult : $addResult, enableResult : $enableResult")
            }
        }

        val thread = Thread(Runnable { doCompare() })
        thread.start()

        try {
            thread.join()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }

        Logger.d("return : $mIsSuccess")
        return mIsSuccess
    }

    private fun getCurrentWifiName(): String? {
        val wifiInfo = mWifiManager!!.connectionInfo
        var wifi_name: String? = null
        if (wifiInfo != null && wifiInfo.ssid != null && wifiInfo.supplicantState == SupplicantState.COMPLETED) {
            wifi_name = wifiInfo.ssid
            if (!TextUtils.isEmpty(wifi_name)) {
                wifi_name = wifi_name.replace("\"", "")
            }
        }
        return wifi_name
    }

    init {
        mWifiManager = mContext.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        Logger.d("mSSID  $mSSID, mPassword : $mPassword, mEncrypt : $mEncrypt")
    }
}