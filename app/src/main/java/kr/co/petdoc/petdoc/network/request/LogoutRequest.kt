package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: LogoutRequest
 * Created by kimjoonsung on 2020/06/22.
 *
 * Description :
 */
class LogoutRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.postLogout()
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}