package kr.co.petdoc.petdoc.domain
import com.google.gson.annotations.SerializedName
import kr.co.petdoc.petdoc.network.response.submodel.TemperatureInfo


/**
 * Petdoc
 * Class: TemperatureResponse
 * Created by kimjoonsung on 2/5/21.
 *
 * Description :
 */
data class TemperatureResponse(
    var info: List<TemperatureInfo>
)