package kr.co.petdoc.petdoc.scanner

import android.content.Context
import android.graphics.Bitmap
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiNetworkSpecifier
import android.os.Build
import androidx.annotation.RequiresApi
import com.aramhuvis.lite.base.*
import io.reactivex.Single
import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.event.ScannerDisconnectedEvent
import kr.co.petdoc.petdoc.log.Logger
import kr.co.petdoc.petdoc.repository.local.preference.PersistentPrefs
import org.greenrobot.eventbus.EventBus
import java.lang.Exception

class ScannerConnectivityMgr(
    context: Context,
    private val persistentPrefs: PersistentPrefs
) {
    private var listener: ACameraListener? = null

    private var connectivityManager: ConnectivityManager =
        context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    private var bindingNetwork: Network? = null

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {
        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            val lastTryTime = persistentPrefs.getValue(AppConstants.PREF_SCANNER_LAST_CONNECT_TIME, 0L)
            val currentTime = System.currentTimeMillis()
            val diff = currentTime - lastTryTime
            if (diff > 300L || diff < 0) {
                Logger.d("[ScannerConnectivityMgr] liteCamera.tryConnect...")
                PetdocApplication.mLiteCamera?.tryConnect {
                    Logger.d("tryConnect Exception ... $it")
                }
                persistentPrefs.setValue(AppConstants.PREF_SCANNER_LAST_CONNECT_TIME, currentTime)
                bindProcessToNetwork(network)
            }
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            connectivityManager.bindProcessToNetwork(null)
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun tryConnect(ssid: String, password: String) {
        val wifiNetworkSpecifier = WifiNetworkSpecifier.Builder()
            .setSsid(ssid)
            .setWpa2Passphrase(password)
            .build()
        val networkRequest = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .removeCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .setNetworkSpecifier(wifiNetworkSpecifier)
            .build()

        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    fun disconnect() {
        if (persistentPrefs.getValue(AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS, false)) {
            persistentPrefs.setValue(AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS, false)

            releaseNetworkBinding()
            clearCamera()
            try {
                connectivityManager.unregisterNetworkCallback(networkCallback)
            } catch (e: Exception) {}
            EventBus.getDefault().post(ScannerDisconnectedEvent())
        }
    }

    private fun clearCamera() {
        PetdocApplication.mLiteCamera?.setCameraListener(null)
        PetdocApplication.mLiteCamera?.clear()
    }

    fun bindProcessToNetwork(network: Network): Boolean {
        Logger.d("[ScannerConnectivityMgr] bindProcessToNetwork... $network")
        bindingNetwork = network
        return connectivityManager.bindProcessToNetwork(bindingNetwork)
    }

    fun reBindProcessWithScannerNetwork() {
        if (bindingNetwork != null) {
            Logger.d("[ScannerConnectivityMgr] reBindProcessWithScannerNetwork... $bindingNetwork")
            connectivityManager.bindProcessToNetwork(bindingNetwork)
        }
    }

    fun releaseNetworkBinding() {
        Logger.d("[ScannerConnectivityMgr] releaseNetworkBinding")
        connectivityManager.bindProcessToNetwork(null)
    }

    /**
     * 스캐너 충전기 연결 여부 체크
     */
    fun isChargerConnected(): Single<Boolean> {
        return Single.create { emitter ->
            listener = object : ACameraListener {
                override fun onKey(p0: KEY_ID?, p1: KEY_STATE?): Boolean { return false}
                override fun onConnection(p0: CONNECTION_STATE?): Boolean { return false}
                override fun onDisconnection(): Boolean {return false}
                override fun onPreview(p0: Bitmap?): Boolean {return false}
                override fun onPreviewReady(): Boolean {return false}
                override fun onPreviewFailed(): Boolean {return false}
                override fun onShot(p0: ByteArray?): Boolean {return false}
                override fun onHydrationIdle(): Boolean {return false}
                override fun onHydration(p0: Int, p1: Int): Boolean {return false}
                override fun onEvent(p0: String?, p1: Any?): Boolean {return false}
                override fun onGetLenseDetect(p0: String?): Boolean {return false}
                override fun onUsbConnectionEvent(p0: Int): Boolean {return false}

                override fun onMessageReceived(message: String): Boolean {
                    if (message.contains("CH:")) {
                        val state = message.substring(
                            message.indexOf("CH:") + "CH:".length,
                            message.indexOf("CH:") + "CH:".length + 1
                        )
                        if (state.toInt() == 4) {
                            emitter.onSuccess(true)
                        } else {
                            emitter.onSuccess(false)
                        }
                        PetdocApplication.mLiteCamera?.removeCameraListener(listener)
                        return true
                    }
                    return false
                }
            }
        }
    }

}