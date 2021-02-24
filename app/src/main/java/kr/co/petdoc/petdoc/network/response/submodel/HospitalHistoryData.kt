package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: HospitalDetailResponse
 * Created by kimjoonsung on 2020/08/03.
 *
 * Description :
 */
data class HospitalHistoryData(
    var bookingData: BookingData,
    var center: Center,
    var facilityType: String,
    var petdocUserId: Int
)

data class BookingData(
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
    var memo: String,
    var petName: String,
    var petdocPetId: Int,
    var petdocUserId: Int,
    var roomOrder: Int,
    var statusType: String,
    var telephone: String,
    var userName: String,
    var vetName: String,
    var vetPosition: String,
    var inspectionType:String
)

data class Center(
    var allDay: Boolean,
    var allDayYn: String,
    var animalIdList: List<Int>,
    var beautyBookingType: String,
    var beautyYn: String,
    var bookingType: String,
    var centerId: Int,
    var clinicBookingType: String,
    var clinicYn: String,
    var endTime: String,
    var hotelBookingType: String,
    var hotelYn: String,
    var isAllDayTime: Boolean,
    var latitude: Double,
    var location: String,
    var longitude: Double,
    var mainImgUrl: String,
    var name: String,
    var parkingYn: String,
    var runStatus: String,
    var startTime: String,
    var telephone: String
)