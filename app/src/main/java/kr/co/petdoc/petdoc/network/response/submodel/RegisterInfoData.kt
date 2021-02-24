package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: RegisterInfoData
 * Created by kimjoonsung on 2020/06/15.
 *
 * Description :
 */
data class RegisterInfoData(
    var bookingSetting: BookingSetting,
    var centerClinicList: List<CenterClinic>,
    var centerId: Int,
    var roomClinicMap: RoomClinicMap,
    var roomList: List<Room>,
    var roomStatusMap: RoomStatusMap
)

data class BookingSetting(
    var activeYn: String,
    var bookingCnt: Int,
    var bookingPeriod: Int,
    var bookingTodayHour: Int,
    var bookingTodayYn: String,
    var bookingYn: String,
    var centerId: Int,
    var registerYn: String
)

data class CenterClinic(
    var centerId: Int,
    var clinicId: Int,
    var name: String,
    var useYn: String,
    var frontYn:String
)

data class RoomClinicMap(
    @SerializedName("1")
    var x1: List<Int>,
    @SerializedName("3")
    var x3: List<Int>
)

data class Room(
    var animalList: List<RoomInfoAnimal>,
    var centerId: Int,
    var clinicList: List<RoomInfoClinic>,
    var clinicRoomId: Int,
    var imgUrl: String?,
    var roomOrder: Int,
    var vetName: String?,
    var vetPosition: String?,
    var vetShowYn: String
)

data class RoomStatusMap(
    @SerializedName("1")
    var x1: String,
    @SerializedName("3")
    var x3: String
)

data class RoomInfoAnimal(
    var animalId: Int,
    var centerId: Int,
    var clinicRoomId: Int,
    var name: String,
    var useYn: String
)

data class RoomInfoClinic(
    var activeYn: String,
    var centerId: Int,
    var clinicId: Int,
    var clinicRoomId: Int,
    var defaultYn: String,
    var name: String,
    var useYn: String
)