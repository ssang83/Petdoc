package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: NutritionData
 * Created by kimjoonsung on 2020/07/15.
 *
 * Description :
 */
data class NutritionData(
    var ash: String,
    var calcium: String,
    var code: String,
    var fat: String,
    var fiber: String,
    var id: Int,
    var ingredients: String,
    var innerNutrList: List<InnerNutr>,
    var introImgUrl: String,
    var name: String,
    var nutritionId: Int,
    var phosphorus: String,
    var price: Int,
    var prodImgUrl: String,
    var protein: String,
    var water: String
)

data class InnerNutr(
    var description: String,
    var id: Int,
    var name: String
)