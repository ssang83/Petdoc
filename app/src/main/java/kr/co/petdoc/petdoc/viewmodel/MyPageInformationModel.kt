package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.co.petdoc.petdoc.network.response.submodel.AllergeResultList
import kr.co.petdoc.petdoc.network.response.submodel.AllergyTotalData
import kr.co.petdoc.petdoc.network.response.submodel.BloodDangerList
import kr.co.petdoc.petdoc.network.response.submodel.BloodResultList

class MyPageInformationModel : ViewModel(){

    var phoneVerification = MutableLiveData<Boolean>().apply{  value = false }
    var emailVerification = MutableLiveData<Boolean>().apply{ value = false }

    var privacyPeroid = MutableLiveData<Int>().apply{ value = -1 }
    var nickname = MutableLiveData<String>().apply{ value = "" }
    var email = MutableLiveData<String>().apply{ value = "" }
    val password = MutableLiveData<String>().apply{ value = "" }
    val phone = MutableLiveData<String>().apply { value = "" }
    val areaCode = MutableLiveData<Int>().apply{ value = 0 }
    val latitude = MutableLiveData<Double>().apply{ value = 0.0 }
    val longitude = MutableLiveData<Double>().apply{ value = 0.0 }
    val address = MutableLiveData<String>().apply { value = "" }

    var profile_image_candidate = MutableLiveData<String>().apply{ value = "" }
    var profile_image = MutableLiveData<String>().apply{ value = "" }

    val loginType = MutableLiveData<Int>().apply{ value = 0 }

    //종합검진 관련 데이터 모델
    var petId = MutableLiveData<Int>().apply{ value = 0 }
    var bookingId = MutableLiveData<Int>().apply{ value = 0 }
    var petName = MutableLiveData<String>().apply{ value = "" }
    var petImage = MutableLiveData<String>().apply { value = "" }
    var bloodResutList = MutableLiveData<BloodResultList>().apply{ value = null }
    var allergeResultList = MutableLiveData<AllergeResultList>().apply { value = null }
    var visitDate = MutableLiveData<String>().apply { value = "" }
    var resultDate = MutableLiveData<String>().apply { value = "" }
    var centerName = MutableLiveData<String>().apply { value = "" }
    var danger4 = MutableLiveData<List<String>>().apply { value = listOf() }
    var danger5 = MutableLiveData<List<String>>().apply { value = listOf() }
    var allergyTotalResult = MutableLiveData<AllergyTotalData>().apply { value = null }
    var bloodDangerList = MutableLiveData<BloodDangerList>().apply { value = null }
    var deepLink = MutableLiveData<Boolean>().apply{ value = false }
}