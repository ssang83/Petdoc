package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: TemperatureInfo
 * Created by kimjoonsung on 2020/05/15.
 *
 * Description :
 */
data class TemperatureInfo(
    @SerializedName("comment")
    val comment: String,
    @SerializedName("from_degree")
    val fromDegree: String,
    @SerializedName("status")
    val status: String,
    @SerializedName("to_degree")
    val toDegree: String
)
