package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Petdoc
 * Class: DeepLinkViewModel
 * Created by kimjoonsung on 2020/09/25.
 *
 * Description :
 */
class DeepLinkViewModel : ViewModel() {
    var deepLinkType = MutableLiveData<String>().apply{ this.value = "" }
    var deepLinkId = MutableLiveData<String>().apply { this.value = "" }
    var deepLinkBookingId = MutableLiveData<String>().apply{ this.value = ""}
}