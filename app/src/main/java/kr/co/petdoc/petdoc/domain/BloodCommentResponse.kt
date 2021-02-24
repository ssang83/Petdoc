package kr.co.petdoc.petdoc.domain

import com.google.gson.annotations.SerializedName

/**
 * Petdoc
 * Class: BloodCommentResponse
 * Created by kimjoonsung on 1/26/21.
 *
 * Description :
 */
data class BloodCommentResponse(
    @SerializedName("code") val code: String,
    @SerializedName("messageKey") val messageKey: String?,
    @SerializedName("resultData") val resultData: BloodResultData
)

data class BloodResultData(
    @SerializedName("bloodComment") val bloodComment: List<BloodComment>
)

data class BloodComment(
    @SerializedName("category") val category: String?,
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("categoryNameKor") val categoryNameKor: String?,
    @SerializedName("upComment") val upComment: String?,
    @SerializedName("downComment") val downComment: String?
)