package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: HospitaDetailData
 * Created by kimjoonsung on 2020/06/09.
 *
 * Description :
 */
data class HospitaDetailData(
    var allDayYn: String,
    var animalList: List<Animal>,
    var beautyBookingType: String,
    var beautyYn: String,
    var bookingType: String,
    var centerId: Int,
    var centerTime: CenterTime?,
    var clinicBookingType: String,
    var clinicList: List<Clinic>,
    var clinicYn: String,
    var description: String?,
    var expenseList: List<PriceItem>,
    var hotelBookingType: String,
    var hotelYn: String,
    var imgList: List<Img>,
    var latitude: Double,
    var location: String,
    var longitude: Double,
    var mainImgUrl: String,
    var name: String,
    var parkingYn: String,
    var telephone: String,
    var vetList:List<Vet>,
    var keywordList:List<String>
)

data class Animal(
    var animalId: Int,
    var centerId: Int,
    var name: String,
    var useYn: String
)

data class CenterTime(
    var centerId: Int,
    var friAllDayYn: String,
    var friEnd: String,
    var friStart: String,
    var friYn: String,
    var monAllDayYn: String,
    var monEnd: String,
    var monStart: String,
    var monYn: String,
    var restYn: String,
    var restEnd: String,
    var restStart: String,
    var satAllDayYn: String,
    var satEnd: String,
    var satStart: String,
    var satYn: String,
    var sunAllDayYn: String,
    var sunYn: String,
    var thuAllDayYn: String,
    var thuEnd: String,
    var thuStart: String,
    var thuYn: String,
    var tueAllDayYn: String,
    var tueEnd: String,
    var tueStart: String,
    var tueYn: String,
    var wedAllDayYn: String,
    var wedEnd: String,
    var wedStart: String,
    var wedYn: String
)

data class Clinic(
    var activeYn: String,
    var centerId: Int,
    var clinicId: Int,
    var defaultYn: String,
    var name: String,
    var useYn: String
)

data class Img(
    var activeYn: String,
    var centerId: Int,
    var id: Int,
    var imgUrl: String,
    var videoYn:String,
    var uploadFileKey:String,
    var videoStatusYn:String,
    var videoUrl:String
)

data class Vet(
    var id: Int = 0,
    var centerId: Int = 0,
    var name: String = "",
    var position:String = "",
    var imgUrl:String = "",
    var desc:String = "",
    var keyword:List<String>
)