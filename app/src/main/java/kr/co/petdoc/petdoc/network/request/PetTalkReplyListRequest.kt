package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: PetTalkReplyListRequest
 * Created by kimjoonsung on 2020/06/03.
 *
 * Description :
 */
class PetTalkReplyListRequest(apiService: ApiService, tag: String, _id: Int) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private val id = _id

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getPetTalkReplyList(id)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}