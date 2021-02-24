package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Petdoc
 * Class: EarScanDataModel
 * Created by kimjoonsung on 2020/05/18.
 *
 * Description :
 */
class CareScanDataModel : ViewModel() {

    var leftEarImgPath = MutableLiveData<String>().apply{ this.value = "" }
    var rightEarImgPath = MutableLiveData<String>().apply{ this.value = "" }
    var leftEarImgByte = MutableLiveData<ByteArray>().apply{ this.value = null }
    var rightEarImgByte = MutableLiveData<ByteArray>().apply{ this.value = null }
    var earType = MutableLiveData<String>().apply{ this.value = "" }
    var petId = MutableLiveData<Int>().apply{ this.value = 0 }
    var petImage = MutableLiveData<String>().apply{ this.value = "" }
}