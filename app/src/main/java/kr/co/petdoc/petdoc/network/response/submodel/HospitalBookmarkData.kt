package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: HospitalBookmarkData
 * Created by kimjoonsung on 2020/06/30.
 *
 * Description :
 */
data class HospitalBookmarkData(
    var allDayTime: Boolean,
    var allDayYn: String,
    var beautyYn: String,
    var bookingType: String,
    var centerId: Int,
    var clinicYn: String?,
    var distance: Int,
    var endTime: String,
    var hotelYn: String,
    var isAllDay: Boolean,
    var latitude: Double,
    var location: String,
    var longitude: Double,
    var mainImgUrl: String,
    var name: String,
    var parkingYn: String,
    var runStatus: String,
    var startTime: String,
    var telephone: String,
    var keywordList:List<String>
)