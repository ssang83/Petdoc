package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: PriceCategory
 * Created by kimjoonsung on 2020/07/20.
 *
 * Description :
 */
data class PriceCategory(
    var child: List<Category>,
    var id: Int,
    var name: String
)

data class Category(
    var child: List<Category>,
    var id: Int,
    var name: String
)

data class PriceCalculate(
    @SerializedName("price_max")
    var priceMax: Int,
    @SerializedName("price_min")
    var priceMin: Int
)