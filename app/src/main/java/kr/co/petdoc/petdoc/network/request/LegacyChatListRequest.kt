package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.RequestCallback
import okhttp3.ResponseBody
import retrofit2.Call

/**
 * Petdoc
 * Class: LegacyChatListRequest
 * Created by kimjoonsung on 2020/05/22.
 *
 * Description :
 */
class LegacyChatListRequest(
    apiService: ApiService,
    tag: String,
    kind: String,
    _categoryId: String,
    _order: String,
    _limit: Int,
    _offset: Int

) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: RequestCallback
    private lateinit var call: Call<ResponseBody>

    private var petKind = kind
    private var categoryId = _categoryId
    private var order = _order
    private var limit = _limit
    private var offset = _offset

    override fun execute() {
        callback = RequestCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getLegacyChatList(petKind, categoryId, order, limit, offset)
        call.enqueue(callback)
    }

    override fun cancel() {
        call.cancel()
    }
}