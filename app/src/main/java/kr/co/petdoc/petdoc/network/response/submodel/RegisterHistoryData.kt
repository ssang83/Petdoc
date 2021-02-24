package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: RegisterHistoryData
 * Created by kimjoonsung on 2020/08/03.
 *
 * Description :
 */
data class RegisterHistoryData(
    var bookingData: RegisterData,
    var bookingTime: String,
    var facilityType: String,
    var petdocUserId: Int,
    var requestTime: String
)

data class RegisterData(
    var animalId: Int,
    var bookingTime: String,
    var bookingType: String,
    var centerId: Int,
    var centerName: String,
    var clinicName: String,
    var clinicRoomId: Int,
    var createTime: String,
    var id: Int,
    var inputType: String,
    var petName: String,
    var petdocPetId: Int,
    var petdocUserId: Int,
    var roomOrder: Int,
    var statusType: String,
    var telephone: String,
    var userName: String,
    var vetName: String,
    var vetPosition: String
)