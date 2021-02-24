package kr.co.petdoc.petdoc.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import kr.co.petdoc.petdoc.enum.PetAddType
import java.io.Serializable

/**
 * Petdoc
 * Class: PetAddInfoDataModel
 * Created by kimjoonsung on 2020/04/10.
 *
 * Description : 반려동물 등록에서 사용하는 데이터 모델
 */
class PetAddInfoDataModel : ViewModel() {

    var petId = MutableLiveData<Int>().apply{ this.value = -1 }

    var type = MutableLiveData<Int>().apply{ this.value = PetAddType.ADD.ordinal }

    var petName = MutableLiveData<String>().apply{ this.value = "" }

    var petBreed = MutableLiveData<String>().apply{ this.value = "" }

    var petBreedId = MutableLiveData<Int>().apply{ this.value = -1 }

    var petBreedKnow = MutableLiveData<Boolean>().apply{ this.value = false }

    var petBreedNotKnow = MutableLiveData<Boolean>().apply{ this.value = false }

    var petKind = MutableLiveData<String>().apply{ this.value = "" }

    var petKindKey = MutableLiveData<String>().apply{ this.value = "" }

    var petBirth = MutableLiveData<String>().apply { this.value = "" }

    var petAge = MutableLiveData<String>().apply { this.value = "" }

    var petYear = MutableLiveData<String>().apply{ this.value = "" }

    var petMonth = MutableLiveData<String>().apply{ this.value = "" }

    var petAgeType = MutableLiveData<Int>().apply{ this.value = 1 }

    var petSex = MutableLiveData<String>().apply{ this.value = "" }

    var petNeutralization = MutableLiveData<Boolean>()

    var petVaccine = MutableLiveData<String>().apply{ this.value = "" }

    var petProfile = MutableLiveData<String>().apply { this.value = "" }

    var regStep = MutableLiveData<Int>().apply { this.value = 0 }

    var flagBox = MutableLiveData<BooleanArray>().apply{ this.value = booleanArrayOf(false, false) }

    var fromCall = MutableLiveData<Boolean>()
}
