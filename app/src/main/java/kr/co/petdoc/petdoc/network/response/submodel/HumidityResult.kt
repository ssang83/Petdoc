package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: HumidityResult
 * Created by kimjoonsung on 2020/06/30.
 *
 * Description :
 */
data class HumidityResult(
    var careTemperatureHumidityList: List<CareTemperatureHumidity>
)

data class CareTemperatureHumidity(
    var created: String,
    var hvalue: Double,
    var id: Int,
    var petId: Int,
    var tvalue: Double
)