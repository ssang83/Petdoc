package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.ChatCategoryListResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: ChatCategoryListRequest
 * Created by kimjoonsung on 2020/05/20.
 *
 * Description :
 */
class ChatCategoryListRequest(apiService: ApiService, tag: String) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<ChatCategoryListResponse>

    private lateinit var call: Call<ChatCategoryListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getChatCategoryList()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}