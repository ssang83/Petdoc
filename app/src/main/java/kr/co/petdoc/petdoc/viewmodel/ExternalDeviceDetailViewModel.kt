package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.base.BaseViewModel
import kr.co.petdoc.petdoc.domain.ScannerVersion
import kr.co.petdoc.petdoc.extensions.io
import kr.co.petdoc.petdoc.helper.SingleLiveEvent
import kr.co.petdoc.petdoc.scanner.FirmwareVersionState
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.YEAR_MONTH_DAY_HYPHEN_FORMATTER
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import java.lang.Exception
import java.util.*

class ExternalDeviceDetailViewModel(
    private val scanner: Scanner
) : BaseViewModel() {
    private val scannerVersion = MutableLiveData<ScannerVersion>()
    val downLoadSubTitle = MutableLiveData<String>()
    val updateSubTitle = MutableLiveData<String>()
    val firmwareVersionState = MutableLiveData<FirmwareVersionState>().apply { value = FirmwareVersionState.LATEST_VERSION }
    val isDownloadProgressShowing = MutableLiveData<Boolean>().apply { value = false }

    val firmwareDownloadCompleted = SingleLiveEvent<Unit>()
    val startFirmwareDetailScreen = SingleLiveEvent<Unit>()
    val startUpdateDetailScreen = SingleLiveEvent<Unit>()
    val startInstallScreen = SingleLiveEvent<Unit>()
    val showFirmwareDownloadDialog = SingleLiveEvent<FirmwareVersionState>()
    val showDeleteDeviceDialog = SingleLiveEvent<Unit>()

    fun start(downLoadFirmwareWhenStart: Boolean = false) {
        viewModelScope.launch {
            scannerVersion.postValue(scanner.getScannerVersion())
            firmwareVersionState.postValue(scanner.getFirmwareState())
        }

        resetDownloadUiSubTitle()
        resetUpdateUiSubTitle()
        if (downLoadFirmwareWhenStart) {
            onDownloadFirmwareClicked()
        }
    }

    private fun resetDownloadUiSubTitle() {
        val lastVerCheckedDate = scanner.getLastFirmVerCheckedDate()
        val subTitle = if (lastVerCheckedDate.isEmpty()) {
            "최신 펫닥 스캐너 펌웨어를 설치 할 수 있습니다."
        } else {
            val formattedDate = try {
                val date = LocalDate.parse(lastVerCheckedDate, YEAR_MONTH_DAY_HYPHEN_FORMATTER)
                date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.KOREAN))
            } catch (e: Exception) {
                ""
            }
            "마지막 업데이트 확인 : $formattedDate"
        }
        downLoadSubTitle.postValue(subTitle)
    }

    private fun resetUpdateUiSubTitle() {
        val lastUpdateDate = scanner.getLastFirmUpdatedDate()
        val subTitle = if (lastUpdateDate.isEmpty()) {
            "펫닥 스캐너 최신 펌웨어 정보를 확인할 수 있습니다."
        } else {
            val formattedDate = try {
                val date = LocalDate.parse(lastUpdateDate, YEAR_MONTH_DAY_HYPHEN_FORMATTER)
                date.format(DateTimeFormatter.ofPattern("yyyy년 MM월 dd일", Locale.KOREAN))
            } catch (e: Exception) {
                ""
            }
            "최신 펌웨어를 ${formattedDate}에 설치 하였습니다."
        }
        updateSubTitle.postValue(subTitle)
    }

    fun onDownLoadLayoutClicked() {
        if (isDownloadProgressShowing.value == true) {
            return
        }
        val firmwareState = firmwareVersionState.value
        if (firmwareState == FirmwareVersionState.LATEST_VERSION) {
            startFirmwareDetailScreen.call()
        } else {
            showFirmwareDownloadDialog.value = firmwareState
        }
    }

    fun onUpdateDetailClicked() {
        startUpdateDetailScreen.call()
    }

    fun onDeleteDeviceClicked() {
        showDeleteDeviceDialog.call()
    }

    fun onDownloadFirmwareClicked() {
        launchWithRx {
            scanner.startDownloadFirmware().io()
                .doOnSubscribe { isDownloadProgressShowing.value = true }
                .doOnComplete { isDownloadProgressShowing.value = false }
                .subscribe {
                    firmwareVersionState.postValue(FirmwareVersionState.DOWNLOADED)
                    firmwareDownloadCompleted.call()
                }
        }
    }

    fun onInstallFirmwareClicked() {
        startInstallScreen.call()
    }
}