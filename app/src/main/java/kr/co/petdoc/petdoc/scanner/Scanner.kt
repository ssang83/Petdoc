package kr.co.petdoc.petdoc.scanner

import android.net.Network
import android.os.Build
import androidx.annotation.RequiresApi
import io.reactivex.Completable
import io.reactivex.Single
import kr.co.petdoc.petdoc.domain.ScannerVersion

interface Scanner {
    suspend fun getScannerVersion(): ScannerVersion?
    /**
     * 펌웨어 업데이트 필요성 여부 체크
     */
    suspend fun getFirmwareState(): FirmwareVersionState

    /**
     * 스캐너의 펌웨어 버전 체크 마지막 날짜
     * @return yyyy-mm-dd
     */
    fun getLastFirmVerCheckedDate(): String

    /**
     * 스캐너 펌웨어 업데이트 마지막 날짜
     * @return yyyy-mm-dd
     */
    fun getLastFirmUpdatedDate(): String

    /**
     * 최신 펌웨어 다운로드
     */
    fun startDownloadFirmware(): Completable

    /**
     * 충전기 연결 상태 체크
     */
    fun isChargerConnected(): Single<Boolean>

    /**
     * 펨웨어 업그레이드 시작
     * @return 1 펌에워 업그레이드 성공
     *         -1 펌웨어 업그레이드 실패
     */
    suspend fun startUpgradeFirmware(): Int

    /**
     * 현재 스캐너가 연결되어 있는 상태인지 체
     * @return true 스캐너 연결, false 스캐너 해제
     */
    fun isConnected(): Boolean

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun tryConnect(ssid: String, password: String)

    fun disconnect()

    /**
     * 스캐너 Wifi Network 에 process 를 Binding
     * @param network Scanner Wifi Network
     */
    fun bindProcessToNetwork(network: Network): Boolean

    /**
     * 스캐너 Wifi Network Binding 해제
     * bindProcessToNetwork 를 통해 binding 된 network 를 해제
     * 스캐너를 사용하는 화면에서 벋어날 때 호출
     */
    fun releaseNetworkBinding()

    /**
     * 이전 스캐너 Wifi Network 에 다시 Binding
     * 스캐너를 사용하는 화면에서 진입할 때 호출
     */
    fun reBindProcessWithScannerNetwork()

    fun setCurrentLenseType(lenseType: String)

    fun getCurrentLenseType(): String

    fun destroy()
}