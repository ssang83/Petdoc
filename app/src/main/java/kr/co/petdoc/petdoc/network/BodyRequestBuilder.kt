package kr.co.petdoc.petdoc.network

import okhttp3.MediaType
import okhttp3.RequestBody
import org.json.JSONObject

/**
 * Petdoc
 * Class: BodyRequestBuilder
 * Created by kimjoonsung on 2/3/21.
 *
 * Description :
 */
fun makeOrderCancelRequestBody(
    message:String,
    orderId:Int,
    userId:Int
): RequestBody {
    return RequestBody.create(MediaType.parse("application/json"), JSONObject().apply {
        put("cancelMsg", message)
        put("orderId", orderId)
        put("petdocUserId", userId)
    }.toString())
}

fun makeMarketingAgreeRequestBody(): RequestBody {
    return RequestBody.create(MediaType.parse("application/json"), JSONObject().apply {
        put("marketing", "true")
    }.toString())
}

fun makePurchaseCancelRequestBody(orderId: String, userId: String, message: String): RequestBody {
    return RequestBody.create(MediaType.parse("application/json"), JSONObject().apply {
        put("cancelMsg", message)
        put("orderId", orderId)
        put("petdocUserId", userId)
    }.toString())
}