package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

/**
 * Petdoc
 * Class: ClinicPriceDataModel
 * Created by kimjoonsung on 2020/07/20.
 *
 * Description :
 */
class ClinicPriceDataModel : ViewModel() {

    var petKind = MutableLiveData<String>().apply{ value = "" } // dog, cat
    var petGender = MutableLiveData<String>().apply{ value = "" } // M, F
    var petYear = MutableLiveData<Int>()                     // year
    var petMonth = MutableLiveData<Int>()                    // month
    var petWeight = MutableLiveData<String>().apply{ value = "" }
    var priceMin = MutableLiveData<Int>().apply{ value = 0 } // price min
    var priceMax = MutableLiveData<Int>().apply{ value = 0 } // price max
}