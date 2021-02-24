package kr.co.petdoc.petdoc.domain

import com.google.gson.annotations.SerializedName

data class ScannerVersionResponse(
    @SerializedName("code") val code: String,
    @SerializedName("messageKey") val messageKey: String?,
    @SerializedName("resultData") val resultData: ScannerVersionResponseResultData
)

data class ScannerVersionResponseResultData(
    @SerializedName("result") val result: ScannerVersion
)

data class ScannerVersion(
    @SerializedName("deviceOs") val os: String,
    @SerializedName("version") val version: String,
    @SerializedName("description") val description: String,
    @SerializedName("path") val path: String
)