package kr.co.petdoc.petdoc.viewmodel

import android.net.wifi.ScanResult
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.aramhuvis.lite.base.ApInfo

/**
 * Petdoc
 * Class: ConnectScannerDataModel
 * Created by kimjoonsung on 2020/05/04.
 *
 * Description :
 */
class ConnectScannerDataModel : ViewModel() {

    var scanResult = MutableLiveData<ScanResult>().apply{ this.value = null }
    var password = MutableLiveData<String>().apply{ this.value = "" }
    var ssid = MutableLiveData<String>().apply{ this.value = "" }
    var apInfoList = MutableLiveData<MutableList<ApInfo>>().apply { this.value = null }
    var wifiPassword = MutableLiveData<String>().apply{ this.value = "" }
    var selectedAPInfo = MutableLiveData<ApInfo>().apply{ this.value = null }
    var connectedDevice = MutableLiveData<String>().apply{ this.value = "" }
}