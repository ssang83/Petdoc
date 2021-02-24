package kr.co.petdoc.petdoc.domain
import com.google.gson.annotations.SerializedName


/**
 * Petdoc
 * Class: CommonResponse
 * Created by kimjoonsung on 2/3/21.
 *
 * Description :
 */
data class CommonResponse(
    var code: String,
    var messageKey: String,
    var resultData: Any?
)