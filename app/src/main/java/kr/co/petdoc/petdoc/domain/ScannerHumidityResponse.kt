package kr.co.petdoc.petdoc.domain
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: ScannerHumidityResponse
 * Created by kimjoonsung on 1/19/21.
 *
 * Description :
 */data class CareHumidityResponse(
    var code: String,
    var messageKey: String,
    var resultData: CareHumidityResultData
)

data class CareHumidityResultData(
    var careTemperatureHumidityList: List<CareTemperatureHumidity>
)

data class CareTemperatureHumidity(
    var created: String,
    var regDate: String,
    var hvalue: Double,
    var id: Int,
    var petId: Int,
    var tvalue: Double
)