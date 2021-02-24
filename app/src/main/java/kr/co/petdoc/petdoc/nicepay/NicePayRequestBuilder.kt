package kr.co.petdoc.petdoc.nicepay

import kr.co.petdoc.petdoc.utils.sha256
import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject
import org.threeten.bp.LocalDate
import org.threeten.bp.LocalDateTime
import org.threeten.bp.format.DateTimeFormatter

private const val SID: String = "0201001"
private const val MID: String = "petdoc003m"
private const val MERCHANT_KEY: String = "Zwg3dicvzY2Fak2pROHXPwqOOXIPgjL5K2ib6QC1hJ3nOS100JZEuzE204CnV23elrywibFi7TBeHY0Odv9mCw=="

fun makeNicePayCreditCardInfoRequestBody(): RequestBody {
    val trDtm = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"))
    val targetDt = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyyMMdd"))
    return RequestBody.create(MediaType.parse("application/json"), JSONObject().apply {
        put("header", JSONObject().apply {
            put("sid", SID)
            put("trDtm", trDtm)
            put("gubun", "S")
            put("resCode", "")
            put("resMsg", "")
        })
        put("body", JSONObject().apply {
            put("mid", MID)
            put("encKey", sha256("$SID$MID$trDtm$MERCHANT_KEY"))
            put("targetDt", targetDt)
        })
    }.toString())
}
