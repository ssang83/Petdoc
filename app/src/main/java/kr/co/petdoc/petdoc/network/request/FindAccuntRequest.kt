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
 * Class: FindAccuntRequest
 * Created by kimjoonsung on 2020/07/30.
 *
 * Description :
 */
class AuthCodeSendRequest(
    apiService: ApiService,
    tag: String,
    var email: String
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val content = JSONObject().apply{
            put("email", email)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postAuthCodeSend(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class AuthCodeConfirmRequest(
    apiService: ApiService,
    tag: String,
    var email: String,
    var authCode:String
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val content = JSONObject().apply{
            put("email", email)
            put("confirmCode", authCode)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postAuthCodeConfirm(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class NewPasswordUploadRequest(
    apiService: ApiService,
    tag: String,
    var email: String,
    var password:String,
    var authCode:String
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val content = JSONObject().apply{
            put("email", email)
            put("password", password)
            put("confirmCode", authCode)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postNewPassword(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class FindIDRequest(
    apiService: ApiService,
    tag: String,
    var name: String,
    var phone:String
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val content = JSONObject().apply{
            put("name", name)
            put("phone", phone)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postFindID(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}