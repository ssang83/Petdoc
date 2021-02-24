package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import okhttp3.MediaType
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call

/**
 * Petdoc
 * Class: RegisterPushRequest
 * Created by kimjoonsung on 2020/06/02.
 *
 * Description :
 */
class RegisterPushRequest(apiService: ApiService, tag: String) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val obj = JSONObject().apply{
            put("device_os", "aos")
            put("device_token", "312781285860")
        }

        val body = RequestBody.create(MediaType.parse("application/json"), obj.toString())

        call = apiService.putRegisterPush(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}