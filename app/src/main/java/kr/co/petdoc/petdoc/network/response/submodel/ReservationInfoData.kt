package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: ReservationInfoData
 * Created by kimjoonsung on 2020/06/16.
 *
 * Description :
 */
data class ReservationInfoData(
    var beautyAnimalList: List<BeautyAnimal>,
    var beautyBookingType: String,
    var centerId: Int,
    var clinicAnimalList: List<ClinicAnimal>,
    var clinicBookingType: String,
    var hotelAnimalList: List<HotelAnimal>,
    var hotelBookingType: String,
    var location: String,
    var mainImgUrl: String,
    var name: String,
    var telephone: String
)

data class ClinicAnimal(
    var animalId: Int,
    var centerId: Int,
    var name: String,
    var useYn: String
)

data class BeautyAnimal(
    var animalId: Int,
    var centerId: Int,
    var name: String,
    var useYn: String
)

data class HotelAnimal(
    var animalId: Int,
    var centerId: Int,
    var name: String,
    var useYn: String
)

data class TimeTable(
    var startTime:String,
    var possible:Boolean
)