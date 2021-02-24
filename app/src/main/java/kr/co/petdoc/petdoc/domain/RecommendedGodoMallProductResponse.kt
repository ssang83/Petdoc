package kr.co.petdoc.petdoc.domain

import com.google.gson.annotations.SerializedName

data class RecommendedGodoMallProductResponse(
    var code: String,
    var messageKey: Any,
    var resultData: List<GodoMallProduct>
)

data class GodoMallProduct(
    var addition: String,
    var code: Int,
    var cost: Int,
    var createdAt: String,
    var originCost: Int,
    var pk: Int,
    var sale: Int,
    var squalImageUrl: String,
    var title: String,
    var wideImageUrl: String
)