package kr.co.petdoc.petdoc.domain
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: CareTemperatureListResponse
 * Created by kimjoonsung on 1/19/21.
 *
 * Description :
 */
data class CareTemperatureResponse(
    var code: String,
    var messageKey: String,
    var resultData: CareTemperatureResultData
)

data class CareTemperatureResultData(
    var careTemperatureList: List<CareTemperature>
)

data class CareTemperature(
    var created: String,
    var regDate: String,
    var id: Int,
    var inject: Boolean,
    var petId: Int,
    var useYn: Boolean,
    var value: Double
)