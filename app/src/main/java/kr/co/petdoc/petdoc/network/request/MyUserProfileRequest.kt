package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.PetdocApplication
import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.MyUserProfileResponse
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import org.json.JSONObject
import retrofit2.Call
import java.io.File

/**
 * Petdoc
 * Class: MyUserProfileRequest
 * Created by kimjoonsung on 2020/07/01.
 *
 * Description :
 */
class MyUserProfileRequest(apiService: ApiService, tag: String) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<MyUserProfileResponse>
    private lateinit var call: Call<MyUserProfileResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMyUesrProfile()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class MyUserProfileModifyRequest(apiService: ApiService, tag: String, var nickName:String, var image:String, var password:String, var phone:String, var privacyPeriod:Int) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<MyUserProfileResponse>
    private lateinit var call: Call<MyUserProfileResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val userId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "")

        val builder = MultipartBody.Builder()
            .setType(MultipartBody.FORM)
            .addFormDataPart("user_id", userId)
            .addFormDataPart("push_agree", PetdocApplication.mUserInfo?.push_agree.toString())
            .addFormDataPart("marketing_email", PetdocApplication.mUserInfo?.marketing_email.toString())
            .addFormDataPart("marketing_sms", PetdocApplication.mUserInfo?.marketing_sms.toString())

        if (nickName.isNotEmpty()) {
            builder.addFormDataPart("nickname", nickName)
        }

        if (password.isNotEmpty()) {
            builder.addFormDataPart("password", password)
        }

        if (phone.isNotEmpty()) {
            builder.addFormDataPart("phone", phone)
        }

        if (privacyPeriod != -1) {
            builder.addFormDataPart("privacy_save_period", privacyPeriod.toString())
        }

        if (image.isNotEmpty()) {
            val file = File(image)
            val requestFile = RequestBody.create(MediaType.parse("image/jpeg"), file)
            val fileBody = MultipartBody.Part.createFormData("imageFile", file.name, requestFile)
            if (file.exists()) {
                builder.addPart(fileBody)
            }
        }

        call = apiService.postUserProfile(builder.build())
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class MyUserProfileEmailVerifyRequest(apiService: ApiService, tag: String, _email:String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val email = _email

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        var content = JSONObject().apply{
            put("email", email)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postEmailVerify(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}

class MyUserProfilePhoneVerifyRequest(apiService: ApiService, tag: String, _phoneNumber:String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val phoneNumber = _phoneNumber

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        var content = JSONObject().apply{
            put("phone_number", phoneNumber)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.postPhoneVerify(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}