package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

/**
 * Petdoc
 * Class: PushRequest
 * Created by kimjoonsung on 2020/09/07.
 *
 * Description :
 */
class FCMRegisterPushToken(apiService: ApiService, tag: String, var token:String) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        var content = JSONObject().apply{
            put("device_os", "aos")
            put("device_token", token)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.putPushToken(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class PushTokenRegisterRequest(apiService: ApiService, tag: String, var token:String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val userId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "")
        val authId = StorageUtils.loadIntValueFromPreference(context, AppConstants.PREF_KEY_AUTH_ID)

        var content = JSONObject().apply{
            put("user_id", userId)
            put("auth_id", authId)
            put("token_type", "2")
            put("device_token", token)
            put("device_os", "aos")
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postPushToken(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}