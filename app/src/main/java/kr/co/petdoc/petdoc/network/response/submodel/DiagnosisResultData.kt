package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: DiagnosiResultData
 * Created by kimjoonsung on 2020/07/14.
 *
 * Description :
 */
data class DiagnosisResultData(
    var ageMonth: Int,
    var agePeriod: String?,
    var ageYear: Int,
    var birthday: String,
    var bodyType: Int,
    var calorie: Int,
    var delYn: String,
    var foodAmount: Int,
    var gender: String,
    var id: Int,
    var infoId: Int,
    var neutralizedYn: String,
    var nutritionAmount: Int,
    var petId: Int,
    var pfId: Int,
    var pnId1: Int,
    var pnId2: Int,
    var pregnancy: Int,
    var regDate: String,
    var speciesId: Int,
    var updateDate: String,
    var userId: Int,
    var walkTime: Int,
    var weight: Double?
)