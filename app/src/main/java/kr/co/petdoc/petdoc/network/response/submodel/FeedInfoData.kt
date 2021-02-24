package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: FeedInfoData
 * Created by kimjoonsung on 2020/07/15.
 *
 * Description :
 */
data class FeedInfoData(
    var ash: String,
    var calcium: String,
    var code: String,
    var fat: String,
    var fiber: String,
    var foodId: Int,
    var id: Int,
    var ingredients: String,
    var introImgUrl: String,
    var name: String,
    var phosphorus: String,
    var price: Int,
    var prodImgUrl: String,
    var protein: String,
    var water: String,
    var innerFoodList:List<FoodIngredient>
)

data class FoodIngredient(
    var id:Int,
    var name:String,
    var description:String
)