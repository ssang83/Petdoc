package kr.co.petdoc.petdoc.domain

import com.google.gson.annotations.SerializedName

data class AllergyCommentResponse(
    @SerializedName("code") val code: String,
    @SerializedName("messageKey") val messageKey: String,
    @SerializedName("resultData") val resultData: AllergyResultData
)

data class AllergyResultData(
    @SerializedName("allergyComment") val allergyComment: AllergyComment
)

data class AllergyComment(
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("feature") val feature: String?,
    @SerializedName("managementMethod") val managementMethod: String?,
    @SerializedName("allergyComment") val commentDetails: List<AllergyCommentDetail>?
)

data class AllergyCommentDetail(
    @SerializedName("categoryName") val categoryName: String?,
    @SerializedName("feature") val feature: String?,
    @SerializedName("managementMethod") val managementMethod: String?,
    @SerializedName("itemTitle") val itemTitle: String?,
    @SerializedName("itemFeature") val itemFeature: String?,
    @SerializedName("itemManagementMethod") val itemManagementMethod: String?,
    @SerializedName("itemNote") val itemNote: String?,
    @SerializedName("habitat") val habitat: String?
)

