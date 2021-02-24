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

class EmailLoginRequest(apiService: ApiService, tag:String, var email:String, var password:String, var uuid:String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        var content = JSONObject().apply{
            put("email", email)
            put("social_type", 1)
            put("password", password)
            put("user_agent", uuid)
            put("login_route", 1)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.emailLogin(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class SNSLoginRequest(apiService: ApiService, tag:String, var socialKey:String, var socialType:Int, var token:String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val uuid = StorageUtils.loadValueFromPreference(
            context,
            AppConstants.PREF_KEY_APP_SETUP_UUID_KEY,
            ""
        )

        var content = JSONObject().apply{
            put("social_key", socialKey)
            put("social_type", socialType)
            put("token", token)
            put("user_agent", uuid)
            put("login_route", 1)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postSNSLogin(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class EmailSignUpRequest(apiService: ApiService, tag:String, var email:String, var passord:String, var nickName:String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val uuid = StorageUtils.loadValueFromPreference(
            context,
            AppConstants.PREF_KEY_APP_SETUP_UUID_KEY,
            ""
        )

        var content = JSONObject().apply{
            put("email", email)
            put("password", passord)
            put("nickname", nickName)
            put("user_agent", uuid)
            put("mobile_confirm", 0)
            put("marketing_email", 1)
            put("marketing_sms", 1)
            put("social_type", 1)
            put("reg_route", 1)
            put("push_agree", 1)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postCreateUser(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class SNSSignUpRequest(apiService: ApiService, tag: String, var email: String, var nickName: String, var socialKey: String, var socialType: Int, var token: String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val uuid = StorageUtils.loadValueFromPreference(
            context,
            AppConstants.PREF_KEY_APP_SETUP_UUID_KEY,
            ""
        )

        var content = JSONObject().apply{
            put("email", email)
            put("nickname", nickName)
            put("social_key", socialKey)
            put("social_type", socialType)
            put("token", token)
            put("user_agent", uuid)
            put("mobile_confirm", 0)
            put("marketing_email", 1)
            put("marketing_sms", 1)
            put("reg_route", 1)
            put("push_agree", 1)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postCreateUser(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class CombineUserRequest(apiService: ApiService, tag:String, var userId:Int, var phone: String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val content = JSONObject().apply{
            put("userId", userId)
            put("phone", phone)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postCombineUser(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class UpdateUserRequest(
    apiService: ApiService,
    tag: String,
    var userId: Int,
    var name: String,
    var birthDay: String,
    var gender: String,
    var phone: String
) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val authId = StorageUtils.loadIntValueFromPreference(context, AppConstants.PREF_KEY_AUTH_ID)
        val content = JSONObject().apply{
            put("user_id", userId)
            put("name", name)
            put("birthday", birthDay)
            put("gender", gender)
            put("phone", phone)
            put("mobile_confirm", 1)
            put("auth_id", authId.toString())
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postUpdateUser(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class UserCountByPhoneRequest(apiService: ApiService, tag:String, var userId:Int, var phone: String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getUserByPhone(phone, userId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class EmailConfirmRequest(apiService: ApiService, tag:String, var userEmail:String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val content = JSONObject().apply{
            put("email", userEmail)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postSendEmailForConfirm(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class EmailConfirmToServerRequest(apiService: ApiService, tag:String, var emailKey:String, var email: String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val content = JSONObject().apply{
            put("emailKey", emailKey)
            put("email", email)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postSendEmailForConfirmServer(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class UserCountByEmailRequest(apiService: ApiService, tag:String, var email: String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getEmailUserCount(email)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}