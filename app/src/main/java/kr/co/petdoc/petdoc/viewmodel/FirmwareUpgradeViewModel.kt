package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import kr.co.petdoc.petdoc.base.BaseViewModel
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.extensions.io
import kr.co.petdoc.petdoc.helper.SingleLiveEvent
import kr.co.petdoc.petdoc.repository.local.preference.PersistentPrefs
import kr.co.petdoc.petdoc.scanner.Scanner
import kr.co.petdoc.petdoc.utils.YEAR_MONTH_DAY_HYPHEN_FORMATTER
import org.threeten.bp.LocalDate

class FirmwareUpgradeViewModel(
    private val scanner: Scanner,
    private val persistentPrefs: PersistentPrefs
) : BaseViewModel() {
    val isChargerConnected = MutableLiveData<Boolean>()

    val showUpgradeProgressDialog = SingleLiveEvent<String>()
    val showUpgradeCompleteDialog = SingleLiveEvent<Boolean>()

    fun restart() {
        launchWithRx {
            scanner.isChargerConnected().io()
                .subscribe({ isConnected ->
                    isChargerConnected.postValue(isConnected)
                    if (isConnected) {
                        startUpgrade()
                    }
                }, {

                })
        }
    }

    private fun startUpgrade() {
        viewModelScope.launch {
            showUpgradeProgressDialog.postValue(scanner.getScannerVersion()?.version)
            if (scanner.startUpgradeFirmware() > 0) {
                persistentPrefs.setValue(AppConstants.PREF_KEY_SCANNER_LAST_FIRMWARE_UPDATE_DATE, LocalDate.now().format(YEAR_MONTH_DAY_HYPHEN_FORMATTER))
                persistentPrefs.setValue(AppConstants.PREF_KEY_SCANNER_FIRMWARE_VER, scanner.getScannerVersion()?.version?:"")
                showUpgradeCompleteDialog.postValue(true)
            } else {
                showUpgradeCompleteDialog.postValue(false)
            }
        }
    }
}