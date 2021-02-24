package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.ChatDetailResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: ChatDetailRequest
 * Created by kimjoonsung on 2020/05/19.
 *
 * Description :
 */
class ChatDetailRequest(apiService: ApiService, tag: String, pk: Int) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<ChatDetailResponse>

    private lateinit var call: Call<ChatDetailResponse>

    private var chatPk = pk

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getChatDetail(chatPk)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}