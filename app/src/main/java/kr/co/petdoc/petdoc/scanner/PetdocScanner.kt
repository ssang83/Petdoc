package kr.co.petdoc.petdoc.scanner

import android.content.Context
import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi
import com.aramhuvis.lite.base.ACamera
import com.downloader.Error
import com.downloader.OnDownloadListener
import com.downloader.PRDownloader
import io.reactivex.Completable
import io.reactivex.Single
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.common.AppConstants.PREF_KEY_SCANNER_FIRMWARE_VER
import kr.co.petdoc.petdoc.domain.ScannerVersion
import kr.co.petdoc.petdoc.repository.local.preference.PersistentPrefs
import kr.co.petdoc.petdoc.repository.network.PetdocApiService
import java.io.File

class PetdocScanner private constructor(
    private val context: Context,
    private val apiService: PetdocApiService,
    private val persistentPrefs: PersistentPrefs
) : Scanner {
    private var scannerVersion: ScannerVersion? = null
    private val firmwareDownloadDir = "${context.externalCacheDir}"
    private val targetFilePath = firmwareDownloadDir + "/${FIRMWARE_FILE_NAME}"

    private var curLenseType: String = ACamera.LENSE_TYPE.NONE

    private val scannerConnectivityMgr = ScannerConnectivityMgr(context, persistentPrefs)

    override suspend fun getScannerVersion(): ScannerVersion? =
        if (scannerVersion == null) {
            try {
                val response = apiService.getScannerVersion()
                if (response.code == "SUCCESS") {
                    scannerVersion = response.resultData.result
                }
            } catch (e: Exception) {}
            scannerVersion
        } else {
            scannerVersion
        }

    override suspend fun getFirmwareState(): FirmwareVersionState = withContext(Dispatchers.IO) {
        if (isUpdateAvailable()) {
            if (hasDownloadedFirmwareBinary().not()) {
              return@withContext FirmwareVersionState.UPDATE_NEEDED
            }
            return@withContext FirmwareVersionState.DOWNLOADED
        }
        FirmwareVersionState.LATEST_VERSION
    }

    private suspend fun isUpdateAvailable(): Boolean {
        val installedFirmVer = persistentPrefs.getValue(PREF_KEY_SCANNER_FIRMWARE_VER, "")
        if (installedFirmVer.isEmpty()) return false
        scannerVersion?.let {
            return compareVersionWithCache(installedFirmVer)
        }
        try {
            val response = apiService.getScannerVersion()
            if (response.code == "SUCCESS") {
                scannerVersion = response.resultData.result
                return compareVersionWithCache(installedFirmVer)
            }
        } catch (e: Exception) {}
        return false
    }

    private fun compareVersionWithCache(installedFirmVer: String): Boolean {
        scannerVersion?.let {
            val latestVersion = it.version
            return latestVersion.compareScannerVersion(installedFirmVer) > 0
        }
        return false
    }

    private fun hasDownloadedFirmwareBinary() = File(targetFilePath).exists()

    override fun getLastFirmVerCheckedDate(): String {
        return persistentPrefs.getValue(AppConstants.PREF_KEY_SCANNER_LAST_VERSION_CHECK_DATE, "")
    }

    override fun getLastFirmUpdatedDate(): String {
        return persistentPrefs.getValue(AppConstants.PREF_KEY_SCANNER_LAST_FIRMWARE_UPDATE_DATE, "")
    }

    override fun startDownloadFirmware(): Completable {
        return Completable.create { emitter ->
            PRDownloader.download(scannerVersion?.path, firmwareDownloadDir, FIRMWARE_FILE_NAME)
                .build()
                .start(object : OnDownloadListener {
                    override fun onDownloadComplete() {
                        emitter.onComplete()
                    }

                    override fun onError(error: Error) {
                        emitter.onError(error.connectionException)
                    }
                })
        }
    }

    override fun isChargerConnected(): Single<Boolean> {
        return scannerConnectivityMgr.isChargerConnected()
    }

    override suspend fun startUpgradeFirmware(): Int {
        val uploadSuccess = uploadFirmware(targetFilePath) > 0
        if (uploadSuccess) {
            return startUpgrade()
        }
        return -1
    }

    override fun isConnected(): Boolean {
        return persistentPrefs.getValue(AppConstants.PREF_KEY_PETDOC_SCAN_CONNECT_STATUS, false)
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    override fun tryConnect(ssid: String, password: String) {
        scannerConnectivityMgr.tryConnect(ssid, password)
    }

    override fun disconnect() {
        scannerConnectivityMgr.disconnect()
    }

    override fun bindProcessToNetwork(network: Network): Boolean {
        return scannerConnectivityMgr.bindProcessToNetwork(network)
    }

    override fun releaseNetworkBinding() {
        scannerConnectivityMgr.releaseNetworkBinding()
    }

    override fun reBindProcessWithScannerNetwork() {
        scannerConnectivityMgr.reBindProcessWithScannerNetwork()
    }

    override fun destroy() {
        /*clearCamera()
        releaseNetworkBinding()*/
    }

    override fun setCurrentLenseType(lenseType: String) {
        this.curLenseType = lenseType
    }

    override fun getCurrentLenseType(): String {
        return this.curLenseType
    }

    companion object {
        const val FIRMWARE_FILE_NAME = "firmware.bin"
        @Volatile private var instance: PetdocScanner? = null

        @JvmStatic
        fun getInstance(
            context: Context,
            apiService: PetdocApiService,
            persistentPrefs: PersistentPrefs
        ): PetdocScanner {
            return instance ?: synchronized(this) {
                instance ?: PetdocScanner(context, apiService, persistentPrefs).also {
                    instance = it
                }
            }
        }
    }
}