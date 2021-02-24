package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.ChatSearchResultListResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: ChatSearchResultListRequest
 * Created by kimjoonsung on 2020/06/01.
 *
 * Description :
 */
class ChatSearchResultListRequest(
    apiService: ApiService,
    tag: String,
    _keyword: String,
    _kind: String,
    _categoryId: String,
    _order: String,
    _limit: Int,
    _offset: Int
) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<ChatSearchResultListResponse>

    private lateinit var call: Call<ChatSearchResultListResponse>

    private val keyword = _keyword
    private val kind = _kind
    private val categoryId = _categoryId
    private val order = _order
    private val limit = _limit
    private val offset = _offset

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getChatSearchResultList(keyword, kind, categoryId, order, limit, offset)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}