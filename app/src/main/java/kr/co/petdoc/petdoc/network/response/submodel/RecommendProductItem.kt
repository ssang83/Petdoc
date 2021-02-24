package kr.co.petdoc.petdoc.network.response.submodel
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: RecommendProductItem
 * Created by kimjoonsung on 2020/05/26.
 *
 * Description :
 */
data class RecommendProductItem(
    val addition: String,
    val code: String,
    val cost: Int,
    @SerializedName("created_at")
    val createdAt: String,
    @SerializedName("origin_cost")
    val originCost: Int,
    val pk: Int,
    val sale: Int,
    @SerializedName("squal_image_url")
    val squalImageUrl: String,
    val title: String,
    @SerializedName("wide_image_url")
    val wideImageUrl: String
)