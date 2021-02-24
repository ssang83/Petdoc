package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.MyPetTalkListResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: MyPetTalkListRequest
 * Created by kimjoonsung on 2020/06/30.
 *
 * Description :
 */
class MyPetTalkListRequest(apiService: ApiService, tag: String) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<MyPetTalkListResponse>
    private lateinit var call: Call<MyPetTalkListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getMyPetTalkList()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}