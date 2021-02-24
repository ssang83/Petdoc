package kr.co.petdoc.petdoc.domain

import com.google.gson.annotations.SerializedName

data class HealthCareResultResponse(
    @SerializedName("code") val code: String,
    @SerializedName("messageKey") val messageKey: String,
    @SerializedName("resultData") val resultData: HealthCareResultResponseResultData
)

data class HealthCareResultResponseResultData(
    @SerializedName("dnsInspectionResult") val dnsInspectionResult: DnsInspectionResult
)

data class DnsInspectionResult(
    @SerializedName("bloodResultList") val bloodResultList: List<List<BloodResult>>,
    @SerializedName("allergyResultList") val allergyResultList: List<List<AllergyResult>>,
    @SerializedName("danger2") val danger2: List<String>,
    @SerializedName("danger3") val danger3: List<String>,
    @SerializedName("danger4") val danger4: List<String>,
    @SerializedName("danger5") val danger5: List<String>,
    @SerializedName("danger6") val danger6: List<String>,
    @SerializedName("bloodDangerList") val bloodDangerList: List<List<BloodResult>>
)

data class BloodResult(
    @SerializedName("specimenId") val specimenId: String,
    @SerializedName("largeCategory") val largeCategory: String,
    @SerializedName("smallCategory") val smallCategory: String,
    @SerializedName("itemNameEng") val itemNameEng: String,
    @SerializedName("itemNameKor") val itemNameKor: String,
    @SerializedName("unit") val unit: String,
    @SerializedName("resultRate") val resultRate: String,
    @SerializedName("minRate") val minRate: String,
    @SerializedName("maxRate") val maxRate: String,
    @SerializedName("referMinRate") val referMinRate: String,
    @SerializedName("referMaxRate") val referMaxRate: String,
    @SerializedName("level") val level: String,
    @SerializedName("percent") val percent: Int
)

data class AllergyResult(
    @SerializedName("specimenId") val specimenId: String,
    @SerializedName("largeCategory") val largeCategory: String,
    @SerializedName("smallCategory") val smallCategory: String,
    @SerializedName("smallCategoryNameEng") val smallCategoryNameEng: String,
    @SerializedName("itemNameEng") val itemNameEng: String,
    @SerializedName("itemNameKor") val itemNameKor: String,
    @SerializedName("resultRate") val resultRate: String,
    @SerializedName("minRate") val minRate: String,
    @SerializedName("maxRate") val maxRate: String,
    @SerializedName("level") val level: String,
    @SerializedName("percent") val percent: Int
)