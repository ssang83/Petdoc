package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.RecommendProductListResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: RecommendProductListRequest
 * Created by kimjoonsung on 2020/05/26.
 *
 * Description :
 */
class RecommendProductListRequest(apiService: ApiService, tag: String) :
    AbstractApiRequest(apiService, tag) {

    private lateinit var callback: ApiCallback<RecommendProductListResponse>

    private lateinit var call: Call<RecommendProductListResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getRecommendProductList()
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}