package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: SpeciesData
 * Created by kimjoonsung on 2020/07/15.
 *
 * Description :
 */
data class SpeciesInfoData(
    var diseaseFirst: String,
    var diseaseList: List<String>,
    var diseaseSecond: String,
    var id: Int,
    var kind: String,
    var majorDisease: String,
    var maxWeight: Double,
    var minWeight: Double,
    var monthAdult: Int,
    var monthYoung: Int,
    var name: String,
    var size: String
)