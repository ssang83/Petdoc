package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: AllergyTotalData
 * Created by kimjoonsung on 2020/09/17.
 *
 * Description :
 */
data class AllergyTotalData(
    var itemNameEng: String,
    var itemNameKor: String,
    var largeCategory: String,
    var level: String,
    var maxRate: String,
    var minRate: String,
    var percent: Int,
    var resultRate: String,
    var smallCategory: String,
    var specimenId: Int,
    var unit: String
)