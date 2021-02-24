package kr.co.petdoc.petdoc.domain

import com.google.gson.annotations.SerializedName
import kr.co.petdoc.petdoc.network.response.submodel.HospitalItem

data class HospitalListResponse(
    @SerializedName("code") val code: String,
    @SerializedName("messageKey") val messageKey: String,
    @SerializedName("resultData") val resultData: HospitalList
)

data class HospitalList(
    val center: List<HospitalItem>,
    val totalCount: Int
)