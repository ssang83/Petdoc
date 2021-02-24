package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: MatchFoodResultData
 * Created by kimjoonsung on 2020/08/05.
 *
 * Description :
 */
data class VLabData(
    var buyDays: Int,
    var buyPossible: Int,
    var foodId: Int,
    var godoURL: String,
    var goodsCnt: Int,
    var goodsNo: String,
    var nutr1Id: Int,
    var nutr2Id: Int,
    var nutrAmt: Int,
    var optionsNo: String,
    var pouchAmt: Int
)