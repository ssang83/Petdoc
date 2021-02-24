package kr.co.petdoc.petdoc.nicepay

data class NicePayCreditCardResponse(
    val header: NicePayCreditCardResponseHeader,
    val body: NicePayCreditCardResponseBody
)

data class NicePayCreditCardResponseHeader(
    val sid: String,
    val resCode: String,
    val resMsg: String
)

data class NicePayCreditCardResponseBody(
    val mid: String,
    val targetDt: String,
    val dataCnt: Int,
    val data: List<CreditCardData>
)

data class CreditCardData(
    val fnCd: String,
    val fnNm: String,
    val instmntMon: String,
    val instmntType: String,
    val minAmt: Int
)