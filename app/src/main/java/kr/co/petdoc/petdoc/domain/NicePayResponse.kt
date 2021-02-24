package kr.co.petdoc.petdoc.domain
import com.google.gson.annotations.SerializedName
import kr.co.petdoc.petdoc.nicepay.NicePayCreditCardResponse


/**
 * Petdoc
 * Class: NicePayResponse
 * Created by kimjoonsung on 2/18/21.
 *
 * Description :
 */
data class NicePayResponse(
    var code: String,
    var messageKey: String,
    var resultData: ResultData
)

data class ResultData(
    var cardInfo:NicePayCreditCardResponse
)