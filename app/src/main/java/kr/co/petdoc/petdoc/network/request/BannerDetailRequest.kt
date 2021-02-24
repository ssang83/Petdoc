package kr.co.petdoc.petdoc.network.request

import kr.co.petdoc.petdoc.R
import kr.co.petdoc.petdoc.network.ApiCallback
import kr.co.petdoc.petdoc.network.ApiService
import kr.co.petdoc.petdoc.network.response.BannerDetailResponse
import retrofit2.Call

/**
 * Petdoc
 * Class: BannerDetailRequest
 * Created by kimjoonsung on 2020/04/21.
 *
 * Description :
 */
class BannerDetailRequest(apiService: ApiService, tag: String, id: Int) :
    AbstractApiRequest(apiService, tag) {

    private val bannerId = id

    private lateinit var callback: ApiCallback<BannerDetailResponse>

    private lateinit var call: Call<BannerDetailResponse>

    override fun execute() {
        callback = ApiCallback(tag)

        if(!isInternetActive()){
            callback.postUnexpectedError(context.getString(R.string.error_no_internet))
            return
        }

        call = apiService.getBanner(bannerId)
        call.enqueue(callback)
    }

    override fun cancel() {
        callback.invalidate()
        call.cancel()
    }
}