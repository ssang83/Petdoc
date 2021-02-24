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
 * Class: SignOutRequest
 * Created by kimjoonsung on 2020/07/01.
 *
 * Description :
 */
class SignOutRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val userId = StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "")

        var content = JSONObject().apply{
            put("user_id", userId)
        }

        val body = RequestBody.create(MediaType.parse("application/json"), content.toString())

        call = apiService.deleteSignOut(body)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}