package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.common.AppConstants
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import kr.co.petdoc.petdoc.network.response.UserPointLogListResponse
import kr.co.petdoc.petdoc.network.response.UserPointResponse
import kr.co.petdoc.petdoc.utils.image.StorageUtils
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: UserPointRequest
 * Created by kimjoonsung on 2020/07/22.
 *
 * Description :
 */
class PointRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: ApiCallback<UserPointResponse>

    private lateinit var call: Call<UserPointResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val userId = if(StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "").isNotEmpty()) {
            StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "").toInt()
        } else {
            0
        }

        call = apiService.getUserPoint(userId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}

class PointLogListRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: ApiCallback<UserPointLogListResponse>

    private lateinit var call: Call<UserPointLogListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        val userId = if(StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "").isNotEmpty()) {
            StorageUtils.loadValueFromPreference(context, AppConstants.PREF_KEY_USER_ID, "").toInt()
        } else {
            0
        }

        call = apiService.getUserPointLogList(userId)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}