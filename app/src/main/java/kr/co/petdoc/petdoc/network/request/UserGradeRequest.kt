package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: UserGradeRequest
 * Created by kimjoonsung on 2020/04/20.
 *
 * Description :
 */
class UserGradeRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag) {
    private lateinit var callback: RequestCallback

    private lateinit var call: Call<ResponseBody>

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getUserGrade()
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}