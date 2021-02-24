package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: CustomealData
 * Created by kimjoonsung on 2020/07/15.
 *
 * Description :
 */
data class CustomealData(
    var allergyFoodIdList: List<Int>,
    var allergyFoodState: Int,
    var beforeFoodState: Int,
    var bodyType: Int,
    var bodyTypeState: Int,
    var delYn: String,
    var diseaseState: Int,
    var dryFoodName: String,
    var dryFoodYn: String,
    var favoriteFoodList: List<Int>,
    var favoriteFoodState: Int,
    var id: Int,
    var nextStep: Int,
    var nutritionIdList: List<Int>,
    var petId: Int,
    var pregnancy: Int,
    var pregnancyState: Int,
    var regStep: Int,
    var snackName: String,
    var userId: Int,
    var walkTime: Int,
    var walkTimeState: Int,
    var weight: Double,
    var weightState: Int,
    var wetFoodName: String
)