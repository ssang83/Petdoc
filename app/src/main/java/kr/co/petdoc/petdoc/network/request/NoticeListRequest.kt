package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.NoticeListResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: NoticeListRequest
 * Created by kimjoonsung on 2020/06/30.
 *
 * Description :
 */
class NoticeListRequest(apiService: ApiService, tag: String) : AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<NoticeListResponse>
    private lateinit var call: Call<NoticeListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getNoticeList()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}