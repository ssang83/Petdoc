package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.gms.maps.GoogleMap
import kr.co.petdoc.petdoc.network.response.submodel.*
import org.json.JSONObject

/**
 * Petdoc
 * Class: HospitalInfoDataModel
 * Created by kimjoonsung on 2020/06/10.
 *
 * Description :
 */
class HospitalDataModel : ViewModel() {

    var hospitalImageItems = MutableLiveData<MutableList<Img>>().apply { this.value = mutableListOf() }
    var doctorItems = MutableLiveData<MutableList<Vet>>().apply { this.value = mutableListOf() }
    var currentLat = MutableLiveData<String>().apply { this.value = "" }
    var currentLng = MutableLiveData<String>().apply { this.value = "" }
    var centerId = MutableLiveData<Int>().apply { this.value = 0}
    var telephone = MutableLiveData<String>().apply { this.value = "" }
    var name = MutableLiveData<String>().apply { this.value = "" }
    var map = MutableLiveData<GoogleMap>().apply { this.value = null }
    var hospitalItem = MutableLiveData<HospitalItem>().apply { this.value = null }
    var searchMode = MutableLiveData<Boolean>().apply { this.value = false }
    var workingYn = MutableLiveData<String>().apply { this.value = "" }
    var registerYn = MutableLiveData<String>().apply { this.value = "" }
    var bookingYn = MutableLiveData<String>().apply { this.value = "" }
    var beautyYn = MutableLiveData<String>().apply { this.value = "" }
    var hotelYn = MutableLiveData<String>().apply { this.value = "" }
    var allDayYn = MutableLiveData<String>().apply { this.value = "" }
    var parkingYn = MutableLiveData<String>().apply { this.value = "" }
    var animalIdList = MutableLiveData<MutableList<Int>>().apply { this.value = mutableListOf() }
    var filterValidation = MutableLiveData<BooleanArray>().apply{ value = booleanArrayOf(false,false,false,false,false,false,false) }
    var bookingClinicRoomItems = MutableLiveData<MutableList<Room>>().apply { this.value = mutableListOf() }
    var bookingSetting = MutableLiveData<BookingSetting>().apply { this.value = null }
    var clinicIdItems = MutableLiveData<MutableList<Int>>().apply { this.value = mutableListOf() }
    var petId = MutableLiveData<Int>().apply { this.value = 0 }
    var message = MutableLiveData<String>().apply { this.value = "" }
    var petWeight = MutableLiveData<String>().apply { this.value = "" }
    var enterDate = MutableLiveData<String>().apply { this.value = "" }
    var outDate = MutableLiveData<String>().apply { this.value = "" }
    var enterTime = MutableLiveData<String>().apply { this.value = "" }
    var outTime = MutableLiveData<String>().apply { this.value = "" }
    var bookingId = MutableLiveData<Int>().apply { this.value = -1 }
    var registerId = MutableLiveData<Int>().apply { this.value = -1 }
    var hospitalItems = MutableLiveData<MutableList<HospitalItem>>().apply { this.value = mutableListOf() }
    var healthCareCode = MutableLiveData<String>().apply { this.value = "" }
    var authCodeUpdate = MutableLiveData<Boolean>().apply { this.value = false }
    var petImage = MutableLiveData<String>().apply { this.value = "" }
    var priceItems = MutableLiveData<MutableList<PriceItem>>().apply { this.value = mutableListOf() }
    var keyword = MutableLiveData<String>().apply { this.value = "" }
    var isPurchseBtnShow = MutableLiveData<Boolean>().apply { this.value = false }
}