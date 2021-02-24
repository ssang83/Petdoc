package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.ChatListResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: ChatListRequest
 * Created by kimjoonsung on 2020/05/19.
 *
 * Description :
 */
class ChatListRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<ChatListResponse>

    private lateinit var call: Call<ChatListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getChatList()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}