package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: BodyTemperatureResponse
 * Created by kimjoonsung on 2020/06/30.
 *
 * Description :
 */
data class BodyTemperatureResult(
    var careTemperatureList: List<CareTemperature>
)

data class CareTemperature(
    var created: String,
    var id: Int,
    var inject: Boolean,
    var petId: Int,
    var useYn: Boolean,
    var value: Double
)