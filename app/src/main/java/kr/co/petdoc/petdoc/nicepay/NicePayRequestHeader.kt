package kr.co.petdoc.petdoc.nicepay

import com.google.gson.annotations.SerializedName

data class NicePayRequest(
    @SerializedName("header") val header: NicePayRequestHeaderData,
    @SerializedName("body") val body: NicePayRequestBodyData
)

data class NicePayRequestHeaderData(
    @SerializedName("sid") val sid: String,
    @SerializedName("trDtm") val trDtm: String,
    @SerializedName("gubun") val gubun: String,
    @SerializedName("resCode") val resCode: String,
    @SerializedName("resMsg") val resMsg: String
)

data class NicePayRequestBodyData(
    @SerializedName("mid") val mid: String,
    @SerializedName("encKey") val encKey: String,
    @SerializedName("targetDt") val targetDt: String
)