package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: MatchFoodData
 * Created by kimjoonsung on 2020/07/14.
 *
 * Description :
 */
data class MatchFoodData(
    var displaySeq: Int,
    var id: Int,
    var name: String,
    var type: String
)