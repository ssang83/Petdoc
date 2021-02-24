package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.ChatDetailResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: ChatSearchDetailRequest
 * Created by kimjoonsung on 2020/05/29.
 *
 * Description :
 */
class ChatSearchDetailRequest(apiService: ApiService, tag: String, chatId: Int) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<ChatDetailResponse>

    private lateinit var call: Call<ChatDetailResponse>

    private var chatPk = chatId

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getChatSearchDetail(chatPk)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}